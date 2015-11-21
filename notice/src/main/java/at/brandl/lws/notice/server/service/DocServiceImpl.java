package at.brandl.lws.notice.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.ChildDsDao;
import at.brandl.lws.notice.shared.service.DocsService;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Get;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

public class DocServiceImpl extends RemoteServiceServlet implements DocsService {

	private static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	private static final String APPLICATION_NAME = "wahrnehmung-test";
	private static final String BUCKET = APPLICATION_NAME + ".appspot.com";
	private static final long serialVersionUID = 6254413733240094242L;
	private static final String TEMPLATE_FILE = "template.docx";
	private static final Range ALL = new Range(0, Integer.MAX_VALUE);
	private static final JacksonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	private static Storage storageService;

	private final BeobachtungDsDao noticeDao;
	private final ChildDsDao childDao;
	private Drive driveService;

	public DocServiceImpl() {

		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
		childDao = DaoRegistry.get(ChildDsDao.class);
	}

	@Override
	public String printDocumentation(String childKey, boolean overwrite, int year) {

		GwtChild child = getChild(childKey);
		File file = createDocument(createTitle(child));
		updateDocument(child, file);

		Collection<GwtBeobachtung> childNotices = fetchNotices(childKey);
		Multimap<String, GwtBeobachtung> sectionNotices = groupNoticesPerSection(childNotices);

		for (String sectionKey : sectionNotices.keySet()) {

			Collection<GwtBeobachtung> notices = sectionNotices.get(sectionKey);
			updateDocument(sectionKey, notices);
		}

		updatePermissions(file, "writer");

		return file.getDefaultOpenWithLink();
	}

	private void updatePermissions(File file, String role) {

		Permission permission = new Permission();
		permission.setValue("lws-test@googlegroups.com");
		permission.setRole(role);
		permission.setType("group");
		try {
			getDrive().permissions().insert(file.getId(), permission)
					.setSendNotificationEmails(true).execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private String createTitle(GwtChild child) {

		return "Bericht " + child.getFirstName();
	}

	private void updateDocument(String sectionName,
			Collection<GwtBeobachtung> notices) {
		// TODO Auto-generated method stub

	}

	private Multimap<String, GwtBeobachtung> groupNoticesPerSection(
			Collection<GwtBeobachtung> notices) {

		ArrayListMultimap<String, GwtBeobachtung> noticesPerSection = ArrayListMultimap
				.create();
		for (GwtBeobachtung notice : notices) {
			noticesPerSection.put(notice.getSectionKey(), notice);
		}
		return noticesPerSection;
	}

	private void updateDocument(GwtChild child, File file) {
		// TODO Auto-generated method stub

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
					.setQ(createFolderQuery(folderName)).execute();

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

	private String createFolderQuery(String folderName) {
		return "title = '" + folderName + "' and mimeType = '" + FOLDER_TYPE
				+ "' and 'root' in parents";
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

	public String uploadFile(File file) throws IOException {

		File newFile = new File();
		Drive drive = getDrive();

		AbstractInputStreamContent content = null;// new ByteArrayContent(type,
													// array);
		file = drive.files().insert(file, content).execute();
		return file.getId();
	}

	private String listFiles() throws IOException {

		Drive drive = getDrive();

		FileList fileList = drive.files().list().execute();
		return fileList.toPrettyString();
	}

	private Drive getDrive() {

		// HttpRequestInitializer httpRequestInitializer = new
		// AppIdentityCredential(
		// SCOPES);

		if (null == driveService) {
			try {
				GoogleCredential credential = GoogleCredential
						.getApplicationDefault();
				// Depending on the environment that provides the default
				// credentials (e.g. Compute Engine,
				// App Engine), the credentials may require us to specify the
				// scopes we need explicitly.
				// Check for this case, and inject the Cloud Storage scope if
				// required.
				if (credential.createScopedRequired()) {
					credential = credential.createScoped(DriveScopes.all());
				}
				HttpTransport httpTransport = GoogleNetHttpTransport
						.newTrustedTransport();
				driveService = new Drive.Builder(httpTransport, JSON_FACTORY,
						credential).setApplicationName(APPLICATION_NAME)
						.build();
			} catch (IOException | GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return driveService;
	}

	/**
	 * Returns an authenticated Storage object used to make service calls to
	 * Cloud Storage.
	 */
	private static Storage getStorage() {
		if (null == storageService) {
			try {
				GoogleCredential credential = GoogleCredential
						.getApplicationDefault();
				// Depending on the environment that provides the default
				// credentials (e.g. Compute Engine,
				// App Engine), the credentials may require us to specify the
				// scopes
				// we need explicitly.
				// Check for this case, and inject the Cloud Storage scope if
				// required.
				if (credential.createScopedRequired()) {
					credential = credential.createScoped(StorageScopes.all());
				}
				HttpTransport httpTransport = GoogleNetHttpTransport
						.newTrustedTransport();
				storageService = new Storage.Builder(httpTransport,
						JSON_FACTORY, credential).setApplicationName(
						APPLICATION_NAME).build();
			} catch (IOException | GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return storageService;
	}

}
