package at.brandl.lws.notice.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;

import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
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
import com.google.api.services.script.Script.Scripts.Run;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Get;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

public class DocServiceImpl extends RemoteServiceServlet implements DocsService {

	private static final long serialVersionUID = 6254413733240094242L;

	private static final String APPLICATION_NAME = "wahrnehmung-test";
	private static final String GROUP = "lws-test@googlegroups.com";

	private static final String TEMPLATE_FILE = "template.docx";
	private static final String BUCKET = APPLICATION_NAME + ".appspot.com";
	private static final String SCRIPT_PROJECT_KEY = "MG_5zR_lIyT2fsbjXj3xcFocrZYMzalMr";
	private static final String SCRIPT_NAME = "updateDocument";

	private static final String TEXT_NAME = "_text_";
	private static final String READER_ROLE = "reader";
	private static final String GROUP_TYPE = "group";
	private static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	private static final Range ALL = new Range(0, Integer.MAX_VALUE);

	private final BeobachtungDsDao noticeDao;
	private final ChildDsDao childDao;
	private final AuthorizationServiceImpl authorizationService;

	private Drive driveService;
	private Storage storageService;

	public DocServiceImpl() {

		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
		childDao = DaoRegistry.get(ChildDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	}

	@Override
	public String printDocumentation(String childKey, int year)
			throws UserGrantRequiredException {

		authorizationService.assertCurrentUserIsTeacher();

		// this must be the first call, because it might trigger an
		// authorization roundtrip
		Credential userCredential = getUserCredentials(childKey, year,
				getUserId());

		GwtChild child = getChild(childKey);
		ParentReference folder = getFolder(getFolderName(year));
		File file = createDocument(createTitle(child), folder);
		updatePermissions(file, "writer");

		Collection<GwtBeobachtung> childNotices = fetchNotices(childKey);
		Map<String, Object> notices = new HashMap<>();
		Date oldest = new Date(Long.MIN_VALUE);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addNotice(Map<String, Object> notices, GwtBeobachtung notice) {

		String[] parts = notice.getSectionName().split(SectionDsDao.SEPARATOR);
		Map<String, Object> sections = getOrCreate(notices, parts[0]);

		if (parts.length == 1) {
			addText(sections, TEXT_NAME, notice);
		} else {
			Map<String, Object> subsections = getOrCreate(sections, parts[1]);
			if (parts.length == 2) {
				addText(subsections, TEXT_NAME, notice);
			} else {
				addText(subsections, parts[2], notice);
			}
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
			subsections = new HashMap<>();
			sections.put(sectionName, subsections);
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

	private void updatePermissions(File file, String role) {

		Permission permission = new Permission();
		permission.setValue(GROUP);
		permission.setRole(role);
		permission.setType(GROUP_TYPE);
		try {
			getDrive().permissions().insert(file.getId(), permission)
					.setSendNotificationEmails(false).execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private String createTitle(GwtChild child) {

		return "Bericht " + child.getFirstName();
	}

	private void updateDocument(String scriptName, File file,
			Credential userCredential, Map<String, String> replacements,
			Map<String, Object> notices) {

		ExecutionRequest request = new ExecutionRequest();

		request.setFunction(scriptName);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file.getId());
		parameters.add(replacements);
		parameters.add(notices);

		request.setParameters(parameters);
		try {
			Run run = getScript(userCredential).scripts().run(
					SCRIPT_PROJECT_KEY, request);
			System.err.println(run.buildHttpRequestUrl().toString());
			run.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private GwtChild getChild(String childKey) {

		return childDao.getChild(childKey);
	}

	private File createDocument(String title, ParentReference parent) {

		ByteArrayContent template = getTemplateFile();

		File file = createFile(title, template.getType(), parent);
		return uploadNewFile(file, template);
	}

	private ParentReference getFolder(String folderName) {

		try {
			File file = getOrCreateFolder(folderName);
			ParentReference parentReference = new ParentReference();
			parentReference.setId(file.getId());
			return parentReference;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File getOrCreateFolder(String folderName) throws IOException {

		FileList files = getDrive().files().list()
				.setQ(createQuery(folderName, FOLDER_TYPE)).execute();

		File file;
		int numFiles = files.getItems().size();
		if (numFiles == 0) {
			file = createFile(folderName, FOLDER_TYPE, null);
			file = uploadNewFile(file, null);
			updatePermissions(file, READER_ROLE);
		} else if (numFiles == 1) {
			file = files.getItems().get(0);

		} else {
			throw new IllegalStateException("more than one folder with name "
					+ folderName + " in root" + files.toPrettyString());
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

	private File uploadNewFile(File file, ByteArrayContent content) {

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
			throw new RuntimeException(e);
		}
	}

	private ByteArrayContent getTemplateFile() {

		try {

			byte[] contet = getContent();
			String type = getType();
			return new ByteArrayContent(type, contet);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getType() throws IOException {

		Get get = getStorage().objects().get(BUCKET, TEMPLATE_FILE);
		StorageObject object = get.execute();
		return object.getContentType();
	}

	private byte[] getContent() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Get get = getStorage().objects().get(BUCKET, TEMPLATE_FILE);
		long start = System.currentTimeMillis();
		get.executeMediaAndDownloadTo(out);
		long duration = System.currentTimeMillis() - start;
		System.err.println("get content of template took " + duration + "ms");
		return out.toByteArray();
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

	private Drive getDrive() {

		if (null == driveService) {
			GoogleCredential credential = createApplicationCredentials(DriveScopes
					.all());
			driveService = new Drive.Builder(Utils.HTTP_TRANSPORT,
					Utils.JSON_FACTORY, credential).setApplicationName(
					APPLICATION_NAME).build();
		}
		return driveService;
	}

	private GoogleCredential createApplicationCredentials(Set<String> scopes) {
		try {
			GoogleCredential credential = GoogleCredential
					.getApplicationDefault();
			if (credential.createScopedRequired()) {
				credential = credential.createScoped(scopes);
			}
			return credential;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 */
	private Storage getStorage() {

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

		// GoogleCredential googleCredential;
		// try {
		// googleCredential = GoogleCredential
		// .getApplicationDefault();
		// if (googleCredential.createScopedRequired()) {
		// googleCredential =
		// googleCredential.createScoped(Arrays.asList("https://www.googleapis.com/auth/documents"));
		// }
		return new Script.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }

	}

	private Credential getUserCredentials(String childKey, int year,
			String userId) throws UserGrantRequiredException {

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
			throw new RuntimeException("could not get credentials for user "
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
