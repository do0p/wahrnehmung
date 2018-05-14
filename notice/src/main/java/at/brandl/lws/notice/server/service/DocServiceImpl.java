package at.brandl.lws.notice.server.service;

import static at.brandl.lws.notice.server.service.DriveServiceImpl.DOCUMENT_TYPE;
import static at.brandl.lws.notice.server.service.DriveServiceImpl.WRITER_ROLE;

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

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
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

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.BackendServiceException;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.DocumentationAlreadyExistsException;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.GwtDocumentation;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.model.GwtSummary;
import at.brandl.lws.notice.model.UserGrantRequiredException;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.server.dao.ds.SectionDsDao;
import at.brandl.lws.notice.shared.Config;
import at.brandl.lws.notice.shared.service.DocsService;
import at.brandl.lws.notice.shared.service.StateParser;
import at.brandl.lws.notice.shared.util.Constants;

public class DocServiceImpl extends RemoteServiceServlet implements DocsService {

	private static final Config CONFIG = Config.getInstance();

	private static final String APPLICATION_NAME = CONFIG.getApplicationName();

	private static final String ROOT_KEY = "root";

	private static final long serialVersionUID = 6254413733240094242L;

	private static final String TEMPLATE_FILE = "template.docx";
	private static final String BUCKET = CONFIG.getBucketName();
	private static final String SCRIPT_PROJECT_KEY = "MG_5zR_lIyT2fsbjXj3xcFocrZYMzalMr";
	private static final String SCRIPT_NAME = "updateDocument";
	private static final String TEXT_NAME = "_text_";
	private static final String ORDER_NAME = "_order_";
	private static final Range ALL = new Range(0, Integer.MAX_VALUE);

	private final BeobachtungDsDao noticeDao;
	private final ChildDsDao childDao;
	private final AuthorizationServiceImpl authorizationService;
	private final DriveServiceImpl driveService;
	private final SectionDsDao sectionDao;

	private Storage storageService;

	public DocServiceImpl() {

		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
		childDao = DaoRegistry.get(ChildDsDao.class);
		sectionDao = DaoRegistry.get(SectionDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
		driveService = new DriveServiceImpl();
	}

	@Override
	public List<GwtDocumentation> getDocumentations(String childKey) throws BackendServiceException {

		authorizationService.assertCurrentUserIsTeacher();

		GwtChild child = getChild(childKey);
		ParentReference folder = getOrCreateDocumentationFolder(child);

		FileList files = driveService.getFiles(null, DOCUMENT_TYPE, folder.getId());
		List<GwtDocumentation> documentations = new ArrayList<GwtDocumentation>();
		for (File file : files.getItems()) {
			GwtDocumentation documentation = map(file, childKey, -1);
			documentations.add(documentation);
		}
		Collections.sort(documentations);
		return documentations;
	}

	@Override
	public GwtDocumentation createDocumentation(String childKey, int year)
			throws DocumentationAlreadyExistsException, UserGrantRequiredException, BackendServiceException {

		authorizationService.assertCurrentUserIsTeacher();

		// this must be the first call, because it might trigger an
		// authorization roundtrip
		Credential userCredential = getUserCredentials(childKey, year, getUserId());

		GwtChild child = getChild(childKey);

		ParentReference folder = getOrCreateDocumentationFolder(child);

		File file = createDocument(child, year, folder);

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
		replacements.put("%%SCHULSTUFE%%", calcGrade(child, year));
		replacements.put("%%DATUM%%", format(new Date()));
		replacements.put("%%VON%%", format(oldest));
		replacements.put("%%BIS%%", format(newest));

		updateDocument(SCRIPT_NAME, file, userCredential, replacements, notices);

		return map(file, childKey, year);
	}

	private String calcGrade(GwtChild child, int year) {

		Number beginYear = child.getBeginYear();
		Number beginGrade = child.getBeginGrade();
		if (beginYear != null && beginGrade != null) {
			return Integer.toString(year - beginYear.intValue() + beginGrade.intValue());
		}
		return "";
	}

	@Override
	public void deleteDocumentation(String fileId) throws BackendServiceException {

		driveService.deleteFile(fileId);
	}

	private GwtDocumentation map(File file, String childKey, int year) {

		GwtDocumentation documentation = new GwtDocumentation();
		documentation.setId(file.getId());
		documentation.setCreateDate(new Date(file.getCreatedDate().getValue()));
		documentation.setTitle(file.getTitle());
		documentation.setUrl(file.getDefaultOpenWithLink());
		documentation.setChildKey(childKey);
		documentation.setYear(year);
		return documentation;
	}

	private ParentReference getOrCreateDocumentationFolder(GwtChild child) throws BackendServiceException {

		ParentReference docRootFolder = driveService.getOrCreateFolder(Constants.NOTICE_ROOT_FOLDER_NAME, null);

		String fullChildName = getFullChildName(child);
		ParentReference childFolder = driveService.getOrCreateFolder(fullChildName, docRootFolder);

		return driveService.getOrCreateFolder(Constants.DOCUMENTATION_FOLDER_NAME, childFolder);
	}

	private String getFullChildName(GwtChild child) {

		return String.format("%1$s %2$s (%3$td.%3$tm.%3$ty)", child.getFirstName(), child.getLastName(),
				child.getBirthDay());
	}

	private Map<String, Object> createMap() {

		return createMap(ROOT_KEY, groupChildSections());
	}

	private Multimap<String, GwtSection> groupChildSections() {

		Multimap<String, GwtSection> childSections = ArrayListMultimap.create();
		for (GwtSection section : getAllSections()) {
			if (section.getSectionName().contains(SectionDsDao.SEPARATOR)) {
				throw new IllegalArgumentException("section name may not contain '" + SectionDsDao.SEPARATOR + "'");
			}

			String parentKey = section.getParentKey();
			parentKey = parentKey == null ? ROOT_KEY : parentKey;
			childSections.put(parentKey, section);
		}
		return childSections;
	}

	private List<GwtSection> getAllSections() {
		List<GwtSection> allSections = new ArrayList<>(sectionDao.getAllSections());
		Collections.sort(allSections);

		return allSections;
	}

	private Map<String, Object> createMap(String parentKey, Multimap<String, GwtSection> childSections) {

		Collection<GwtSection> sections = childSections.get(parentKey);
		if (sections == null || sections.isEmpty()) {
			return Collections.emptyMap();
		}

		LinkedHashMap<String, Object> parentMap = new LinkedHashMap<>();
		ArrayList<Object> sectionOrder = new ArrayList<>();
		parentMap.put(ORDER_NAME, sectionOrder);
		for (GwtSection section : sections) {
			if (isArchived(section)) {
				continue;
			}
			LinkedHashMap<String, Object> childMap = new LinkedHashMap<>();
			childMap.put(TEXT_NAME, new ArrayList<>());
			childMap.putAll(createMap(section.getKey(), childSections));
			String sectionName = section.getSectionName();
			parentMap.put(sectionName, childMap);
			sectionOrder.add(sectionName);
		}

		return parentMap;
	}

	private boolean isArchived(GwtSection section) {
		return Boolean.TRUE.equals(section.getArchived());
	}

	private void addNotice(Map<String, Object> notices, GwtBeobachtung notice) {

		String[] parts = notice.getSectionName().split(SectionDsDao.SEPARATOR);
		try {
			Map<String, Object> sections = getOrCreate(notices, parts[0]);

			if (parts.length == 1) {
				addText(sections, TEXT_NAME, notice);
			} else {
				Map<String, Object> subsections = getOrCreate(sections, parts[1]);
				if (parts.length == 2) {
					addText(subsections, TEXT_NAME, notice);
				} else {
					Map<String, Object> subsectionsText = getOrCreate(subsections, parts[2]);
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

	private Map<String, Object> getOrCreate(Map<String, Object> sections, String sectionName) {

		@SuppressWarnings("unchecked")
		Map<String, Object> subsections = (Map<String, Object>) sections.get(sectionName);
		if (subsections == null) {
			System.err.println("no section found for section name " + sectionName);
			System.err.println("sectionnames: " + sections.keySet());
			throw new IllegalArgumentException("no section found for section name " + sectionName);
		}
		return subsections;
	}

	private void addText(Map<String, Object> sections, String sectionName, GwtBeobachtung notice) {

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

	private String createTitle(GwtChild child, int year) {

		return String.format("Bericht %s SJ %s-%s", getFullChildName(child), year, year + 1);
	}

	private void updateDocument(String scriptName, File file, Credential userCredential,
			Map<String, String> replacements, Map<String, Object> notices) throws BackendServiceException {

		ExecutionRequest request = new ExecutionRequest();

		request.setFunction(scriptName);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file.getId());
		parameters.add(replacements);
		parameters.add(notices);

		request.setParameters(parameters);
		try {
			getScript(userCredential).scripts().run(SCRIPT_PROJECT_KEY, request).execute();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new BackendServiceException("Got exception updating file " + file.getDefaultOpenWithLink() + " with "
					+ Utils.createJsonString(parameters), e);
		}
	}

	private GwtChild getChild(String childKey) {

		return childDao.getChild(childKey);
	}

	private File createDocument(GwtChild child, int year, ParentReference parent)
			throws BackendServiceException, DocumentationAlreadyExistsException {

		ByteArrayContent template = getTemplateFile();

		String title = createTitle(child, year);
		assertFileNotExists(title, parent);
		File file = driveService.uploadFile(title, template.getType(), parent, template);
		driveService.updatePermissions(file, WRITER_ROLE, true);
		return file;
	}

	private void assertFileNotExists(String title, ParentReference parent)
			throws DocumentationAlreadyExistsException, BackendServiceException {

		FileList files = driveService.getFiles(title, DOCUMENT_TYPE, parent.getId());

		int numFiles = files.getItems().size();
		if (numFiles > 0) {
			File file = files.getItems().iterator().next();
			throw new DocumentationAlreadyExistsException(file.getDefaultOpenWithLink());
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
			e.printStackTrace(System.err);
			throw new BackendServiceException("Got exception retrieving content type of " + TEMPLATE_FILE, e);
		}
	}

	private byte[] getContent() throws BackendServiceException {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Get get = getStorage().objects().get(BUCKET, TEMPLATE_FILE);
			get.executeMediaAndDownloadTo(out);
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new BackendServiceException("Got exception retrieving content from " + TEMPLATE_FILE, e);
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

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 */
	private Storage getStorage() throws BackendServiceException {

		if (null == storageService) {

			GoogleCredential credential = Utils.createApplicationCredentials(StorageScopes.all());
			storageService = new Storage.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();
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

		return new Script.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	private Credential getUserCredentials(String childKey, int year, String userId)
			throws UserGrantRequiredException, BackendServiceException {

		AuthorizationCodeFlow flow = Utils.newFlow();
		Credential credential = null;
		try {
			credential = flow.loadCredential(userId);
			if (credential != null) {

				Long expiresInSeconds = credential.getExpiresInSeconds();
				if (expiresInSeconds == null || expiresInSeconds < 60) {
					boolean refreshSuccess = false;
					try {
						refreshSuccess = credential.refreshToken();
					} catch (TokenResponseException e) {
						System.err.println("Error refreshing token for user " + userId + ". Message: " + e.getMessage());
					}
					if (!refreshSuccess) {
						credential = null;
						flow.getCredentialDataStore().delete(userId);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error getting token for user " + userId + ". Message: " + e.getMessage());
			e.printStackTrace(System.err);
			throw new BackendServiceException(
					"Could not get credentials for user " + userId + ". Cause: " + e.getMessage(), e);
		}

		if (credential == null) {
			throw new UserGrantRequiredException(buildAuthorizationUrl(flow, childKey, year));
		}

		return credential;
	}

	private String buildAuthorizationUrl(AuthorizationCodeFlow flow, String childKey, int year) {

		HttpServletRequest request = getThreadLocalRequest();
		String redirectUri = Utils.getRedirectUri(request);
		String state = new StateParser(childKey, year).getState();
		return flow.newAuthorizationUrl().setRedirectUri(redirectUri).setState(state).build();
	}

	private static String getUserId() {

		return UserServiceFactory.getUserService().getCurrentUser().getUserId();
	}

}
