package at.brandl.lws.notice.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;

import at.brandl.lws.notice.model.BackendServiceException;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.model.GwtSummary;
import at.brandl.lws.notice.model.UserGrantRequiredException;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.server.dao.ds.SectionDsDao;
import at.brandl.lws.notice.shared.service.DocsService;
import at.brandl.lws.notice.shared.service.StateParser;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Get;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

public class DocServiceImpl extends RemoteServiceServlet implements DocsService {

	private static final String ROOT_KEY = "root";

	private static final long serialVersionUID = 6254413733240094242L;

	private static final String APPLICATION_NAME = "wahrnehmung-test";
	private static final String GROUP = "lws-test@googlegroups.com";

	private static final String TEMPLATE_FILE = "template.docx";
	private static final String BUCKET = APPLICATION_NAME + ".appspot.com";
	private static final String SCRIPT_PROJECT_KEY = "MG_5zR_lIyT2fsbjXj3xcFocrZYMzalMr";
	private static final String SCRIPT_NAME = "updateDocument";

	private static final String TEXT_NAME = "_text_";
	private static final String ORDER_NAME = "_order_";
	private static final String READER_ROLE = "reader";
	private static final String GROUP_TYPE = "group";
	private static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	private static final Range ALL = new Range(0, Integer.MAX_VALUE);

	private final BeobachtungDsDao noticeDao;
	private final ChildDsDao childDao;
	private final AuthorizationServiceImpl authorizationService;

	private Drive driveService;
	private Storage storageService;

	private SectionDsDao sectionDao;

	public DocServiceImpl() {

		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
		childDao = DaoRegistry.get(ChildDsDao.class);
		sectionDao = DaoRegistry.get(SectionDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	}

	@Override
	public String printDocumentation(String childKey, int year)
			throws UserGrantRequiredException, BackendServiceException {

		authorizationService.assertCurrentUserIsTeacher();

		// this must be the first call, because it might trigger an
		// authorization roundtrip
		Credential userCredential = getUserCredentials(childKey, year,
				getUserId());

		GwtChild child = getChild(childKey);
		ParentReference folder = getFolder(getFolderName(year));
		File file = createDocument(createTitle(child), folder);
		updatePermissions(file, "writer", true);

		Collection<GwtBeobachtung> childNotices = fetchNotices(childKey);
		Map<String, Object> notices = createMap();
		Date oldest = new Date(Long.MAX_VALUE);
		Date newest = new Date(Long.MIN_VALUE);
		for (GwtBeobachtung notice : childNotices) {

			if (!(notice instanceof GwtSummary)) {
				Date date = notice.getDate();
				if (date.before(oldest)) {
					oldest = date;
				}
				if (date.after(newest)) {
					newest = date;
				}
			}
			addNotice(notices, notice);
		}

		Map<String, String> replacements = new HashMap<>();
		replacements.put("%%NAME%%", getName(child));
		replacements.put("%%SCHULJAHR%%", year + " / " + (year + 1));
		replacements.put("%%DATUM%%", format(new Date()));
		replacements.put("%%VON%%", format(oldest));
		replacements.put("%%BIS%%", format(newest));

		updateDocument(SCRIPT_NAME, file, userCredential, replacements, notices);

		return file.getDefaultOpenWithLink();
	}

	private Map<String, Object> createMap() {

		return createMap(ROOT_KEY, groupChildSections());
	}

	private Multimap<String, GwtSection> groupChildSections() {

		Multimap<String, GwtSection> childSections = ArrayListMultimap.create();
		for (GwtSection section : getAllSections()) {
			if (section.getSectionName().contains(SectionDsDao.SEPARATOR)) {
				throw new IllegalArgumentException(
						"section name may not contain '"
								+ SectionDsDao.SEPARATOR + "'");
			}

			String parentKey = section.getParentKey();
			parentKey = parentKey == null ? ROOT_KEY : parentKey;
			childSections.put(parentKey, section);
		}
		return childSections;
	}

	private List<GwtSection> getAllSections() {
		List<GwtSection> allSections = new ArrayList<>(
				sectionDao.getAllSections());
		Collections.sort(allSections);

		return allSections;
	}

	private Map<String, Object> createMap(String parentKey,
			Multimap<String, GwtSection> childSections) {

		Collection<GwtSection> sections = childSections.get(parentKey);
		if (sections == null || sections.isEmpty()) {
			return Collections.emptyMap();
		}

		LinkedHashMap<String, Object> parentMap = new LinkedHashMap<>();
		ArrayList<Object> sectionOrder = new ArrayList<>();
		parentMap.put(ORDER_NAME, sectionOrder);
		for (GwtSection section : sections) {
			LinkedHashMap<String, Object> childMap = new LinkedHashMap<>();
			childMap.put(TEXT_NAME, new ArrayList<>());
			childMap.putAll(createMap(section.getKey(), childSections));
			String sectionName = section.getSectionName();
			parentMap.put(sectionName, childMap);
			sectionOrder.add(sectionName);
		}

		return parentMap;
	}

	private String getFolderName(int year) {
		return String.format("Berichte %d/%d", year, year + 1);
	}

	@Override
	public void deleteAll() {
		try {
			FileList files = getDrive().files().list().execute();
			for (File file : files.getItems()) {
				getDrive().files().delete(file.getId()).execute();
			}
		} catch (IOException | BackendServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addNotice(Map<String, Object> notices, GwtBeobachtung notice) {

		String[] parts = notice.getSectionName().split(SectionDsDao.SEPARATOR);
		try {
			Map<String, Object> sections = getOrCreate(notices, parts[0]);

			if (parts.length == 1) {
				addText(sections, TEXT_NAME, notice);
			} else {
				Map<String, Object> subsections = getOrCreate(sections,
						parts[1]);
				if (parts.length == 2) {
					addText(subsections, TEXT_NAME, notice);
				} else {
					Map<String, Object> subsectionsText = getOrCreate(
							subsections, parts[2]);
					addText(subsectionsText, TEXT_NAME, notice);
				}
			}
		} catch (IllegalArgumentException e) {
			System.err.println("error with " + Arrays.toString(parts));
		}
	}

	private String format(Date date) {
		return new SimpleDateFormat("d.M.yy").format(date);
	}

	private Map<String, Object> getOrCreate(Map<String, Object> sections,
			String sectionName) {

		@SuppressWarnings("unchecked")
		Map<String, Object> subsections = (Map<String, Object>) sections
				.get(sectionName);
		if (subsections == null) {
			System.err.println("no section found for section name "
					+ sectionName);
			System.err.println("sectionnames: " + sections.keySet());
			throw new IllegalArgumentException(
					"no section found for section name " + sectionName);
		}
		return subsections;
	}

	private void addText(Map<String, Object> sections, String sectionName,
			GwtBeobachtung notice) {

		@SuppressWarnings("unchecked")
		List<String> texts = (List<String>) sections.get(sectionName);
		if (texts == null) {
			texts = new ArrayList<>();
			sections.put(sectionName, texts);
		}
		texts.add(getText(notice));
	}

	private String getText(GwtBeobachtung notice) {
		StringBuilder text = new StringBuilder();

		if (notice instanceof GwtSummary) {
			text.append(notice.getUser());
		} else {
			text.append("am: " + format(notice.getDate()));
			if (at.brandl.lws.notice.shared.Utils.isNotEmpty(notice.getUser())) {
				text.append("\nvon: " + notice.getUser());
			}
			if (notice.getDuration() != null) {
				text.append("\nDauer: " + notice.getDuration().getText());
			}
			if (notice.getSocial() != null) {
				text.append("\nSozialform: " + notice.getSocial().getText());
			}
		}
		text.append("\n\n" + parse(notice.getText()));
		return text.toString();
	}

	private String parse(String htmlText) {

		String cleaned = Jsoup.clean(htmlText, Whitelist.relaxed());
		Document document = Jsoup.parseBodyFragment(cleaned);
		Element body = document.body();
		FormattingVisitor formattingVisitor = new FormattingVisitor();
		NodeTraversor nodeTraversor = new NodeTraversor(formattingVisitor);

		nodeTraversor.traverse(body);

		return formattingVisitor.toString();
	}

	private String getName(GwtChild child) {
		return child.getFirstName() + " " + child.getLastName().toUpperCase();
	}

	private void updatePermissions(File file, String role, boolean retry)
			throws BackendServiceException {

		Permission permission = new Permission();
		permission.setValue(GROUP);
		permission.setRole(role);
		permission.setType(GROUP_TYPE);
		try {
			getDrive().permissions().insert(file.getId(), permission)
					.setSendNotificationEmails(false).execute();
		} catch (IOException e) {
			if (retry) {
				updatePermissions(file, role, false);
			} else {
				throw new BackendServiceException(
						"Got exception setting permission " + role
								+ " for file " + file.getDefaultOpenWithLink(),
						e);
			}
		}

	}

	private String createTitle(GwtChild child) {

		return "Bericht " + child.getFirstName();
	}

	private void updateDocument(String scriptName, File file,
			Credential userCredential, Map<String, String> replacements,
			Map<String, Object> notices) throws BackendServiceException {

		ExecutionRequest request = new ExecutionRequest();

		request.setFunction(scriptName);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file.getId());
		parameters.add(replacements);
		parameters.add(notices);

		request.setParameters(parameters);
		try {
			getScript(userCredential).scripts()
					.run(SCRIPT_PROJECT_KEY, request).execute();
		} catch (IOException e) {
			throw new BackendServiceException("Got exception updating file "
					+ file.getDefaultOpenWithLink() + " with "
					+ Utils.createJsonString(parameters), e);
		}
	}

	private GwtChild getChild(String childKey) {

		return childDao.getChild(childKey);
	}

	private File createDocument(String title, ParentReference parent) throws BackendServiceException {

		ByteArrayContent template = getTemplateFile();

		File file = createFile(title, template.getType(), parent);
		return uploadNewFile(file, template);
	}

	private ParentReference getFolder(String folderName)
			throws BackendServiceException {

		File file = getOrCreateFolder(folderName);
		ParentReference parentReference = new ParentReference();
		parentReference.setId(file.getId());
		return parentReference;

	}

	private File getOrCreateFolder(String folderName)
			throws BackendServiceException {

		FileList files;
		try {
			files = getDrive().files().list()
					.setQ(createQuery(folderName, FOLDER_TYPE)).execute();
		} catch (IOException e) {
			throw new BackendServiceException(
					"Got exception retrieving root folders", e);
		}

		File file;
		int numFiles = files.getItems().size();
		if (numFiles == 0) {
			file = createFile(folderName, FOLDER_TYPE, null);
			file = uploadNewFile(file, null);
			updatePermissions(file, READER_ROLE, true);
		} else if (numFiles == 1) {
			file = files.getItems().get(0);

		} else {
			String folderlist;
			try {
				folderlist = files.toPrettyString();
			} catch (IOException e) {
				folderlist = "";
			}
			throw new IllegalStateException("more than one folder with name "
					+ folderName + " in root: " + folderlist);
		}
		return file;
	}

	private String createQuery(String title, String type) {
		return "title = '" + title + "' and mimeType = '" + type + "'";
	}

	private File createFile(String title, String mimeType,
			ParentReference parent) {

		File file = new File();
		file.setEditable(true);
		file.setTitle(title);
		file.setMimeType(mimeType);
		if (parent != null) {
			file.setParents(Arrays.asList(parent));
		}
		return file;
	}

	private File uploadNewFile(File file, ByteArrayContent content)
			throws BackendServiceException {

		try {
			Insert insert;
			if (content != null) {

				insert = getDrive().files().insert(file, content);
				insert.setConvert(true);
			} else {
				insert = getDrive().files().insert(file);
			}

			return insert.execute();

		} catch (IOException e) {
			throw new BackendServiceException("Got exception creating file "
					+ file.getTitle(), e);
		}
	}

	private ByteArrayContent getTemplateFile() throws BackendServiceException {

		byte[] contet = getContent();
		String type = getType();
		return new ByteArrayContent(type, contet);

	}

	private String getType() throws BackendServiceException {

		try {
			Get get = getStorage().objects().get(BUCKET, TEMPLATE_FILE);
			StorageObject object = get.execute();
			return object.getContentType();
		} catch (IOException e) {
			throw new BackendServiceException(
					"Got exception retrieving content type of " + TEMPLATE_FILE,
					e);
		}
	}

	private byte[] getContent() throws BackendServiceException {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Get get = getStorage().objects().get(BUCKET, TEMPLATE_FILE);
			get.executeMediaAndDownloadTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new BackendServiceException(
					"Got exception retrieving content from " + TEMPLATE_FILE, e);
		}
	}

	private List<GwtBeobachtung> fetchNotices(String childKey) {

		return noticeDao.getBeobachtungen(createFilter(childKey), ALL);
	}

	private BeobachtungsFilter createFilter(String childKey) {

		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(childKey);
		filter.setSinceLastDevelopmementDialogue(true);
		filter.setShowSummaries(true);
		return filter;
	}

	private Drive getDrive() throws BackendServiceException {

		if (null == driveService) {
			GoogleCredential credential = createApplicationCredentials(DriveScopes
					.all());
			driveService = new Drive.Builder(Utils.HTTP_TRANSPORT,
					Utils.JSON_FACTORY, credential).setApplicationName(
					APPLICATION_NAME).build();
		}
		return driveService;
	}

	private GoogleCredential createApplicationCredentials(Set<String> scopes) throws BackendServiceException {
		try {
			GoogleCredential credential = GoogleCredential
					.getApplicationDefault();
			if (credential.createScopedRequired()) {
				credential = credential.createScoped(scopes);
			}
			return credential;
		} catch (IOException e) {
			throw new BackendServiceException("Got exception creating app credentials with scopes " + scopes, e);
		}
	}

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 */
	private Storage getStorage() throws BackendServiceException {

		if (null == storageService) {

			GoogleCredential credential = createApplicationCredentials(StorageScopes
					.all());
			storageService = new Storage.Builder(Utils.HTTP_TRANSPORT,
					Utils.JSON_FACTORY, credential).setApplicationName(
					APPLICATION_NAME).build();
		}
		return storageService;
	}

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 * 
	 * @param credential
	 * @throws UserGrantRequiredException
	 */
	private Script getScript(Credential credential) {

		return new Script.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();
	}

	private Credential getUserCredentials(String childKey, int year,
			String userId) throws UserGrantRequiredException, BackendServiceException {

		AuthorizationCodeFlow flow = Utils.newFlow();
		Credential credential = null;
		try {
			credential = flow.loadCredential(userId);
			if (credential != null) {

				if (credential.getExpiresInSeconds() < 60
						&& !credential.refreshToken()) {
					credential = null;
					flow.getCredentialDataStore().delete(userId);
				}
			}
		} catch (IOException e) {
			throw new BackendServiceException("could not get credentials for user "
					+ userId, e);
		}

		if (credential == null) {
			throw new UserGrantRequiredException(buildAuthorizationUrl(flow,
					childKey, year));
		}

		return credential;
	}

	private String buildAuthorizationUrl(AuthorizationCodeFlow flow,
			String childKey, int year) {

		HttpServletRequest request = getThreadLocalRequest();
		String redirectUri = Utils.getRedirectUri(request);
		String state = new StateParser(childKey, year).getState();
		return flow.newAuthorizationUrl().setRedirectUri(redirectUri)
				.setState(state).build();
	}

	private static String getUserId() {

		return UserServiceFactory.getUserService().getCurrentUser().getUserId();
	}

}
