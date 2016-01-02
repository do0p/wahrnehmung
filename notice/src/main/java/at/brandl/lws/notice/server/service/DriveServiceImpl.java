package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.util.Arrays;

import at.brandl.lws.notice.model.BackendServiceException;
import at.brandl.lws.notice.shared.util.Constants;
import static at.brandl.lws.notice.shared.util.Constants.*;
import static at.brandl.lws.notice.server.service.Utils.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;

public class DriveServiceImpl {

	public static final String READER_ROLE = "reader";
	public static final String WRITER_ROLE = "writer";
	
	private static final String ROOT_PARENT = "root";
	private static final String GROUP_TYPE = "group";
	private static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	
	private Drive driveService;

	public void deleteAll() {
		try {
			FileList files = getDrive().files().list().execute();
			for (File file : files.getItems()) {
				getDrive().files().delete(file.getId()).execute();
			}
		} catch (IOException | BackendServiceException e) {
			e.printStackTrace();
		}
	}

	public File uploadFolder(String folderName, ParentReference parent)
			throws BackendServiceException {

		File file = createFile(folderName, FOLDER_TYPE, parent);

		try {

			return getDrive().files().insert(file).execute();

		} catch (IOException e) {
			throw new BackendServiceException("Got exception creating file "
					+ file.getTitle(), e);
		}
	}

	public File uploadFile(String title, String mimeType,
			ParentReference parent, ByteArrayContent content)
			throws BackendServiceException {

		File file = createFile(title, mimeType, parent);

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

	public FileList getFiles(String title, String mimeType, String parentId)
			throws BackendServiceException {
		try {
			return getDrive().files().list()
					.setQ(createQuery(title, mimeType, parentId)).execute();
		} catch (IOException e) {
			throw new BackendServiceException(
					"Got exception retrieving file with name " + title, e);
		}
	}

	public void updatePermissions(File file, String role, boolean retry)
			throws BackendServiceException {

		Permission permission = new Permission();
		permission.setValue(Constants.GROUP);
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

	public ParentReference getOrCreateFolder(String folderName, ParentReference parent)
			throws BackendServiceException {

		String parentId = parent == null ? ROOT_PARENT : parent.getId();
		FileList files = getFiles(folderName, FOLDER_TYPE, parentId);

		File file;
		int numFiles = files.getItems().size();
		if (numFiles == 0) {
			file = uploadFile(folderName, FOLDER_TYPE, parent, null);
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
		return createParentReference(file);
	}

	private ParentReference createParentReference(File file) {
		ParentReference parentReference = new ParentReference();
		parentReference.setId(file.getId());
		return parentReference;
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

	private String createQuery(String title, String type, String parent) {
		StringBuilder query = new StringBuilder();
		query.append("title = '" + title + "'");
		if (type != null) {
			query.append(" and ");
			query.append("mimeType = '" + type + "'");
		}
		if (parent != null) {
			query.append(" and ");
			query.append("'" + parent + "' in parents");
		}
		return query.toString();
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

}
