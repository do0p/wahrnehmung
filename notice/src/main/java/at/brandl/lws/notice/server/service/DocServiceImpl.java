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
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

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
import com.google.apphosting.api.ApiProxy.CancelledException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

public class DocServiceImpl extends RemoteServiceServlet implements DocsService {

	private static final String TEXT_NAME = "_text_";
	private static final String REPLACEMENT_SCRIPT_NAME = "searchAndReplace";
	private static final String SECTIONS_SCRIPT_NAME = "insertSections";
	private static final String REMOVE_TEMPLATE_SCRIPT_NAME = "removeTemplates";
	// private static final String SCRIPT_TYPE =
	// "application/vnd.google-apps.script";
	private static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	private static final String APPLICATION_NAME = "wahrnehmung-test";
	private static final String BUCKET = APPLICATION_NAME + ".appspot.com";
	private static final long serialVersionUID = 6254413733240094242L;
	private static final String TEMPLATE_FILE = "template.docx";
	private static final Range ALL = new Range(0, Integer.MAX_VALUE);
	private static final String SCRIPT_PROJECT_KEY = "MG_5zR_lIyT2fsbjXj3xcFocrZYMzalMr";

	private static Storage storageService;

	private final BeobachtungDsDao noticeDao;
	private final ChildDsDao childDao;
	private Drive driveService;

	public DocServiceImpl() {

		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
		childDao = DaoRegistry.get(ChildDsDao.class);
	}

	@Override
	public String printDocumentation(String childKey, boolean overwrite,
			int year) throws UserGrantRequiredException {

		// this must be the first call, because it might trigger an
		// authorization roundtrip
		Credential userCredential = getUserCredentials(childKey, overwrite,
				year);

		GwtChild child = getChild(childKey);
		File file = createDocument(createTitle(child));
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

		updateDocument(replacements, REPLACEMENT_SCRIPT_NAME, file,
				userCredential);

//		Map<String, Object> sectionNotices = new HashMap<>();
//		for (Entry<String, Object> entry : notices.entrySet()) {
//			sectionNotices.put(entry.getKey(), entry.getValue());
//			updateDocument(sectionNotices, SECTIONS_SCRIPT_NAME, file,
//					userCredential);
//			sectionNotices.clear();
//		}

		updateDocument(notices, SECTIONS_SCRIPT_NAME, file, userCredential);
		updateDocument(null, REMOVE_TEMPLATE_SCRIPT_NAME, file, userCredential);

		return file.getDefaultOpenWithLink();
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
		permission.setValue("lws-test@googlegroups.com");
		permission.setRole(role);
		permission.setType("group");
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

	private void updateDocument(Map<?, ?> data, String scriptName, File file,
			Credential userCredential) {

		ExecutionRequest request = new ExecutionRequest();

		request.setFunction(scriptName);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file.getId());
		if (data != null) {
			parameters.add(data);
		}
		request.setParameters(parameters);
		long start = System.currentTimeMillis();
		try {
			Run run = getScript(userCredential).scripts().run(
					SCRIPT_PROJECT_KEY, request);
			System.err.println(run.buildHttpRequestUrl().toString());
			run.execute();
			long duration = System.currentTimeMillis() - start;
			System.err.println("call took " + duration + "ms.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CancelledException e) {
			long duration = System.currentTimeMillis() - start;
			System.err.println("call cancelled after " + duration + "ms.");
		}

	}

	private GwtChild getChild(String childKey) {

		return childDao.getChild(childKey);
	}

	private File createDocument(String title) {

		ByteArrayContent template = getTemplateFile();
		ParentReference parent = getParent();
		File file = createFile(title, template.getType(), parent);
		return uploadNewFile(file, template);
	}

	private ParentReference getParent() {

		try {
			File file;
			String folderName = "Berichte 2015/2016";
			FileList files = getDrive().files().list()
					.setQ(createQuery(folderName, FOLDER_TYPE)).execute();

			int numFiles = files.getItems().size();
			if (numFiles == 0) {
				file = createFile(folderName, FOLDER_TYPE, null);
				file = uploadNewFolder(file);
				updatePermissions(file, "reader");

			} else if (numFiles == 1) {
				file = files.getItems().get(0);

			} else {
				throw new IllegalStateException("more than one folder in root"
						+ files.toPrettyString());
			}

			ParentReference parentReference = new ParentReference();
			parentReference.setId(file.getId());
			return parentReference;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			Insert insert = getDrive().files().insert(file, content);
			insert.setConvert(true);
			return insert.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File uploadNewFolder(File file) {

		try {
			return getDrive().files().insert(file).execute();
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
		get.executeMediaAndDownloadTo(out);
		return out.toByteArray();
	}

	private List<GwtBeobachtung> fetchNotices(String childKey) {

		BeobachtungsFilter filter = createFilter(childKey);
		return noticeDao.getBeobachtungen(filter, ALL);
	}

	private BeobachtungsFilter createFilter(String childKey) {

		BeobachtungsFilter filter = new BeobachtungsFilter();
		filter.setChildKey(childKey);
		filter.setSinceLastDevelopmementDialogue(true);
		filter.setShowEmptyEntries(true);
		filter.setShowSummaries(true);
		return filter;
	}

	private Drive getDrive() {

		if (null == driveService) {
			try {
				GoogleCredential credential = GoogleCredential
						.getApplicationDefault();
				if (credential.createScopedRequired()) {
					credential = credential.createScoped(DriveScopes.all());
				}
				driveService = new Drive.Builder(Utils.HTTP_TRANSPORT,
						Utils.JSON_FACTORY, credential).setApplicationName(
						APPLICATION_NAME).build();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return driveService;
	}

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 */
	private Storage getStorage() {

		if (null == storageService) {
			try {
				GoogleCredential credential = GoogleCredential
						.getApplicationDefault();
				if (credential.createScopedRequired()) {
					credential = credential.createScoped(StorageScopes.all());
				}
				storageService = new Storage.Builder(Utils.HTTP_TRANSPORT,
						Utils.JSON_FACTORY, credential).setApplicationName(
						APPLICATION_NAME).build();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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

	private Credential getUserCredentials(String childKey, boolean overwrite,
			int year) throws UserGrantRequiredException {

		AuthorizationCodeFlow flow = Utils.newFlow();
		Credential credential = null;
		try {
			credential = flow.loadCredential(getUserId());
			if (credential != null) {

				if (credential.getExpiresInSeconds() < 60) {
					if (!credential.refreshToken()) {
						credential = null;
						flow.getCredentialDataStore().delete(getUserId());
					}
				}

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (credential == null) {
			throw new UserGrantRequiredException(buildAuthorizationUrl(flow,
					childKey, overwrite, year));
		}

		return credential;
	}

	private String buildAuthorizationUrl(AuthorizationCodeFlow flow,
			String childKey, boolean overwrite, int year) {

		HttpServletRequest request = getThreadLocalRequest();
		String redirectUri = Utils.getRedirectUri(request);
		String state = new StateParser(childKey, overwrite, year).getState();
		return flow.newAuthorizationUrl().setRedirectUri(redirectUri)
				.setState(state).build();
	}

	private static String getUserId() {

		return UserServiceFactory.getUserService().getCurrentUser().getUserId();
	}

	private class FormattingVisitor implements NodeVisitor {
		private StringBuilder accum = new StringBuilder();

		public void head(Node node, int depth) {

			String name = node.nodeName();
			if (node instanceof TextNode) {
				accum.append(((TextNode) node).text());
			} else if (name.equals("li")) {
				accum.append("\n * ");
			} else if (name.equals("dt")) {
				accum.append("  ");
			} else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5",
					"tr")) {
				accum.append("\n");
			}
		}

		// hit when all of the node's children (if any) have been visited
		public void tail(Node node, int depth) {

			String name = node.nodeName();
			if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3",
					"h4", "h5", "div")) {
				accum.append("\n");
			}
		}

		@Override
		public String toString() {
			return accum.toString();
		}
	}
}
