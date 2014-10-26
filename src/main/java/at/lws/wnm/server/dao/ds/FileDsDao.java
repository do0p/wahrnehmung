package at.lws.wnm.server.dao.ds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import at.lws.wnm.shared.model.Utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class FileDsDao extends AbstractDsDao {

	public static final String FILE_DAO_MEMCACHE = "fileDsDao";
	public static final String FILE_KIND = "FileDs";
	private static final String FILENAME_FIELD = "filename";
	private static final String STORAGE_FILENAME_FIELD = "storageFilename";
	private static final String BEOBACHTUNGS_KEYS_FIELD = "beobachtungsKeys";

	private GcsService gcsService = GcsServiceFactory.createGcsService();

	@Override
	protected String getMemcacheServiceName() {
		return FILE_DAO_MEMCACHE;
	}

	public boolean fileExists(String filename) {
		return getFileAttachement(filename) != null;
	}

	public Map<String, String> getFilenames(String beobachtungsKey) {
		Map<String, String> filenames = new HashMap<String, String>();
		Collection<Entity> fileAttachments = findFileAttachements(beobachtungsKey);
		for (Entity fileAttachement : fileAttachments) {
			String filename = (String) fileAttachement
					.getProperty(FILENAME_FIELD);
			String storgeFilename = (String) fileAttachement
					.getProperty(STORAGE_FILENAME_FIELD);
			filenames.put(filename, storgeFilename);
		}
		return filenames;
	}

	public void deleteFiles(String beobachtungsKey) {
		Collection<Entity> fileAttachments = findFileAttachements(beobachtungsKey);
		for (Entity fileAttachement : fileAttachments) {
			@SuppressWarnings("unchecked")
			Collection<String> beobachtungsKeys = (Collection<String>) fileAttachement
					.getProperty(BEOBACHTUNGS_KEYS_FIELD);

			beobachtungsKeys.remove(beobachtungsKey);

			if (beobachtungsKeys.isEmpty()) {
				deleteInBlobstore(fileAttachement);
				deleteFileAttachement(fileAttachement);
			} else {
				fileAttachement.setProperty(BEOBACHTUNGS_KEYS_FIELD,
						beobachtungsKeys);
				storeFileAttachement(fileAttachement);
			}
		}
	}

	public void storeFiles(String beobachtungsKey, Map<String, String> filenames) {
		if (filenames == null) {
			return;
		}
		for (Entry<String, String> entry : filenames.entrySet()) {
			String filename = entry.getKey();
			String storageFilename = entry.getValue();

			storeFile(beobachtungsKey, filename, storageFilename);
		}

	}

	private void storeFile(String beobachtungsKey, String filename,
			String storageFilename) {
		Entity fileAttachement = getFileAttachement(filename);
		if (fileAttachement == null) {
			Key key = KeyFactory.createKey(FILE_KIND, filename);
			fileAttachement = new Entity(key);
			fileAttachement.setProperty(FILENAME_FIELD, filename);
			fileAttachement
					.setProperty(STORAGE_FILENAME_FIELD, storageFilename);
		}

		@SuppressWarnings("unchecked")
		Collection<String> beobachtungsKeys = (Collection<String>) fileAttachement
				.getProperty(BEOBACHTUNGS_KEYS_FIELD);
		if (beobachtungsKeys == null) {
			beobachtungsKeys = new ArrayList<String>();
		}
		if (!beobachtungsKeys.contains(beobachtungsKey)) {
			beobachtungsKeys.add(beobachtungsKey);
			fileAttachement.setProperty(BEOBACHTUNGS_KEYS_FIELD,
					beobachtungsKeys);
			storeFileAttachement(fileAttachement);
		}
	}

	private void deleteFileAttachement(Entity fileAttachement) {
		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {

			Key key = fileAttachement.getKey();
			deleteFromCache(key);
			datastoreService.delete(key);
			transaction.commit();

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void deleteInBlobstore(Entity fileAttachement) {
		String storageFilename = (String) fileAttachement
				.getProperty(STORAGE_FILENAME_FIELD);
		GcsFilename gcsFilename = new GcsFilename(Utils.GS_BUCKET_NAME,
				storageFilename);
		try {
			gcsService.delete(gcsFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Collection<Entity> findFileAttachements(String beobachtungsKey) {
		Query query = new Query(FILE_KIND)
				.setFilter(new FilterPredicate(BEOBACHTUNGS_KEYS_FIELD,
						FilterOperator.EQUAL, beobachtungsKey));
		PreparedQuery preparedQuery = getDatastoreService().prepare(query);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		return preparedQuery.asList(fetchOptions);
	}

	private void storeFileAttachement(Entity fileAttachement) {
		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {

			datastoreService.put(fileAttachement);
			transaction.commit();
			insertIntoCache(fileAttachement);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}

	}

	private Entity getFileAttachement(String filename) {
		try {
			return getCachedEntity(KeyFactory.createKey(FILE_KIND, filename));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
