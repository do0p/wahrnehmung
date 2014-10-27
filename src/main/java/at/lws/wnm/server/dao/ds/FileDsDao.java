package at.lws.wnm.server.dao.ds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
import com.google.appengine.api.memcache.MemcacheService;
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
	private Map<String, Date> dirty = new ConcurrentHashMap<String, Date>();

	@Override
	protected String getMemcacheServiceName() {
		return FILE_DAO_MEMCACHE;
	}

	public boolean fileExists(String filename) {
		return getFileAttachement(filename) != null;
	}

	public Map<String, String> getFilenames(String beobachtungsKey) {
		Map<String, String> filenames = new HashMap<String, String>();
		Map<String, Entity> fileAttachments = findFileAttachements(
				beobachtungsKey, getDatastoreService());
		for (Entity fileAttachement : fileAttachments.values()) {
			String filename = (String) fileAttachement
					.getProperty(FILENAME_FIELD);
			String storgeFilename = (String) fileAttachement
					.getProperty(STORAGE_FILENAME_FIELD);
			filenames.put(filename, storgeFilename);
		}
		return filenames;
	}

	public void deleteFiles(String beobachtungsKey) {

		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {

			Map<String, Entity> fileAttachments = findFileAttachements(
					beobachtungsKey, datastoreService);

			for (Entity fileAttachement : fileAttachments.values()) {
				deleteFileAttachement(beobachtungsKey, fileAttachement,
						datastoreService);
			}

			transaction.commit();
			setUpdateNeeded(beobachtungsKey);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void deleteFileAttachement(String beobachtungsKey,
			Entity fileAttachement, DatastoreService datastoreService) {
		@SuppressWarnings("unchecked")
		Collection<String> beobachtungsKeys = (Collection<String>) fileAttachement
				.getProperty(BEOBACHTUNGS_KEYS_FIELD);

		beobachtungsKeys.remove(beobachtungsKey);

		if (beobachtungsKeys.isEmpty()) {
			deleteInBlobstore(fileAttachement);
			deleteFileAttachement(fileAttachement, datastoreService);
		} else {
			fileAttachement.setProperty(BEOBACHTUNGS_KEYS_FIELD,
					beobachtungsKeys);
			storeFileAttachement(fileAttachement, datastoreService);
		}
	}

	public void storeFiles(String beobachtungsKey, Map<String, String> filenames) {
		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {

			Map<String, Entity> fileAttachements = findFileAttachements(
					beobachtungsKey, datastoreService);

			if (filenames != null) {

				for (Entry<String, String> entry : filenames.entrySet()) {
					String filename = entry.getKey();
					String storageFilename = entry.getValue();

					fileAttachements.remove(filename);
					storeFile(beobachtungsKey, filename, storageFilename,
							datastoreService);
				}

				for (Entity fileAttachement : fileAttachements.values()) {
					deleteFileAttachement(beobachtungsKey, fileAttachement,
							datastoreService);
				}
			}

			transaction.commit();
			setUpdateNeeded(beobachtungsKey);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void storeFile(String beobachtungsKey, String filename,
			String storageFilename, DatastoreService datastoreService) {

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
			storeFileAttachement(fileAttachement, datastoreService);
		}
	}

	private void deleteFileAttachement(Entity fileAttachement,
			DatastoreService datastoreService) {

		Key key = fileAttachement.getKey();
		deleteFromCache(key);
		datastoreService.delete(key);

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

	private Map<String, Entity> findFileAttachements(String beobachtungsKey,
			DatastoreService datastoreService) {

		MemcacheService cache = getCache();
		if (!cache.contains(beobachtungsKey) || updateNeeded(beobachtungsKey)) {
			synchronized (this) {
				if (!cache.contains(beobachtungsKey)
						|| updateNeeded(beobachtungsKey)) {
					setUpdated(beobachtungsKey);
					Collection<Key> cachedEntities = getAllFileAttachements(
							beobachtungsKey, datastoreService);
					cache.put(beobachtungsKey, cachedEntities);
				}
			}
		}
		@SuppressWarnings("unchecked")
		Collection<Key> keys = (Collection<Key>)cache.get(beobachtungsKey);
		return getFileAttachements(keys);
	}

	private Map<String, Entity> getFileAttachements(Collection<Key> keys) {
		final Map<String, Entity> fileAttachements = new HashMap<String, Entity>();
		for(Key key : keys) {
			Entity fileAttachement = getCachedEntity(key);
			String filename = (String) fileAttachement.getProperty(FILENAME_FIELD);
			fileAttachements.put(filename, fileAttachement);
		}
		return fileAttachements;
	}

	private Collection<Key> getAllFileAttachements(String beobachtungsKey,
			DatastoreService datastoreService) {

		Query query = new Query(FILE_KIND)
				.setFilter(new FilterPredicate(BEOBACHTUNGS_KEYS_FIELD,
						FilterOperator.EQUAL, beobachtungsKey)).setKeysOnly();
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		return convertToKeyList(preparedQuery.asList(fetchOptions));
	}

	private Collection<Key> convertToKeyList(List<Entity> entities) {
		Collection<Key> keys = new ArrayList<Key>();
		for(Entity entity : entities) {
			keys.add(entity.getKey());
		}
		return keys;
	}


	private void storeFileAttachement(Entity fileAttachement,
			DatastoreService datastoreService) {

		datastoreService.put(fileAttachement);
		insertIntoCache(fileAttachement);

	}

	private Entity getFileAttachement(String filename) {
		try {
			return getCachedEntity(KeyFactory.createKey(FILE_KIND, filename));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private void setUpdated(String beobachtungsKey) {
		dirty.remove(beobachtungsKey);
	}

	private boolean updateNeeded(String beobachtungsKey) {
		return dirty.get(beobachtungsKey) != null;
	}

	private void setUpdateNeeded(String beobachtungsKey) {
		dirty.put(beobachtungsKey, new Date());
	}
}
