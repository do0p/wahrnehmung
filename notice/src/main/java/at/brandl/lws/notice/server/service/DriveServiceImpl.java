package at.brandl.lws.notice.server.service;

import static at.brandl.lws.notice.server.service.Utils.createApplicationCredentials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;

import at.brandl.lws.notice.model.BackendServiceException;
import at.brandl.lws.notice.shared.Config;

public class DriveServiceImpl {

	private static final Config CONFIG = Config.getInstance();
	public static final String READER_ROLE = "reader";
	public static final String WRITER_ROLE = "writer";

	private static final String ROOT_PARENT = "root";
	private static final String GROUP_TYPE = "group";
	public static final String FOLDER_TYPE = "application/vnd.google-apps.folder";
	public static final String DOCUMENT_TYPE = "application/vnd.google-apps.document";

	private Drive driveService;

	public File uploadFolder(String folderName, ParentReference parent)
			throws BackendServiceException {

		File file = createFile(folderName, FOLDER_TYPE, parent);

		try {

			return getDrive().files().insert(file).execute();

		} catch (IOException e) {
			e.printStackTrace(System.err);
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
			e.printStackTrace(System.err);
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
			e.printStackTrace(System.err);
			throw new BackendServiceException(
					"Got exception retrieving file with name " + title, e);
		}
	}

	public void updatePermissions(File file, String role, boolean retry)
			throws BackendServiceException {

		Permission permission = new Permission();
		permission.setValue(CONFIG.getDocumentationGroupName());
		permission.setRole(role);
		permission.setType(GROUP_TYPE);
		try {
			getDrive().permissions().insert(file.getId(), permission)
					.setSendNotificationEmails(false).execute();
		} catch (IOException e) {
			if (retry) {
				updatePermissions(file, role, false);
			} else {
				e.printStackTrace(System.err);
				throw new BackendServiceException(
						"Got exception setting permission " + role
								+ " for file " + file.getDefaultOpenWithLink(),
						e);
			}
		}

	}

	public ParentReference getOrCreateFolder(String folderName,
			ParentReference parent) throws BackendServiceException {

		String parentId = parent == null ? ROOT_PARENT : parent.getId();
		FileList fileList = getFiles(folderName, FOLDER_TYPE, parentId);

		File file;
		List<File> files = new ArrayList<File>();
//		List<File> files = fileList.getItems().stream().filter(f -> f.getTitle().equals(folderName)).collect(Collectors.toList());
		for(File f : fileList.getItems()) {
			if(f.getTitle().equals(folderName)) {
				files.add(f);
			}
		}
		int numFiles = files.size();
		if (numFiles == 0) {
			file = uploadFile(folderName, FOLDER_TYPE, parent, null);
			updatePermissions(file, READER_ROLE, true);

			// check for race condition, if another folder has been created at
			// the same time
			if (getFiles(folderName, FOLDER_TYPE, parentId).getItems().size() > 1) {

				// some other thread has created a folder, so delete this one
				// and start all over
				deleteFile(file.getId());
				return getOrCreateFolder(folderName, parent);
			}

		} else if (numFiles == 1) {
			file = files.get(0);

		} else {

			// delete duplicate empty folders
			for (File duplicate : files) {
				if(deleteEmptyFolder(duplicate) && (--numFiles == 1)) {
						break;
				}
			}

			if (numFiles > 1) {
				String folderlist;
				try {
					folderlist = fileList.toPrettyString();
				} catch (IOException e) {
					folderlist = "";
				}
				throw new IllegalStateException(
						"more than one folder with name " + folderName
								+ " in root: " + folderlist);
			}
			
			return getOrCreateFolder(folderName, parent);
		}
		return createParentReference(file);
	}

	private boolean deleteEmptyFolder(File folder)
			throws BackendServiceException {

		if(!FOLDER_TYPE.equals(folder.getMimeType())) {
			// only deleting empty folders
			return false;
		}
		
		String folderId = folder.getId();
		List<File> children = getFiles(null, null, folderId).getItems();
		boolean noChildren = true;
		for(File file : children) {
			noChildren &= deleteEmptyFolder(file);
		}
		
		if (noChildren) {
			System.err.println("deleting empty duplicate folder: " + folderId);
			deleteFile(folderId);
			return true;
		}
		
		return false;
	}

	public void deleteFile(String fileId) throws BackendServiceException {

		try {
			getDrive().files().delete(fileId).execute();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new BackendServiceException("Got exception deleting file "
					+ fileId, e);
		}
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
		List<String> queries = new ArrayList<String>();
		if (title != null) {
			queries.add("title = '" + title + "'");
		}
		if (type != null) {
			queries.add("mimeType = '" + type + "'");
		}
		if (parent != null) {
			queries.add("'" + parent + "' in parents");
		}
		return join(queries, " and ");
	}

	private String join(Collection<String> parts, String separator) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = parts.iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next());
			if (iterator.hasNext()) {
				builder.append(separator);
			}
		}
		return builder.toString();
	}

	private Drive getDrive() throws BackendServiceException {

		if (null == driveService) {
			GoogleCredential credential = createApplicationCredentials(DriveScopes
					.all());
			driveService = new Drive.Builder(Utils.HTTP_TRANSPORT,
					Utils.JSON_FACTORY, credential).setApplicationName(
					CONFIG.getApplicationName()).build();
		}
		return driveService;
	}
}
