package at.brandl.lws.notice.server.dao.ds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.brandl.lws.notice.model.GwtFileInfo;
import at.brandl.lws.notice.shared.Config;

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
	private static final String CONTENT_TYPE_FIELD = "contentType";
	private static final String IMAGE_URL_FIELD = "imageUrl";
	private static final String BEOBACHTUNGS_KEYS_FIELD = "beobachtungsKeys";

	private GcsService gcsService = GcsServiceFactory.createGcsService();
	private Map<String, Date> dirty = new ConcurrentHashMap<String, Date>();

	public boolean fileExists(String filename) {
		return getFileAttachement(filename) != null;
	}

	public List<GwtFileInfo> getFileInfos(String beobachtungsKey) {

		List<GwtFileInfo> filenames = new ArrayList<GwtFileInfo>();

		Map<String, Entity> fileAttachments = findFileAttachements(beobachtungsKey);

		for (Entity fileAttachement : fileAttachments.values()) {
			GwtFileInfo fileInfo = toGwt(fileAttachement);
			filenames.add(fileInfo);
		}

		return filenames;
	}

	private GwtFileInfo toGwt(Entity fileAttachement) {
		GwtFileInfo fileInfo = new GwtFileInfo();
		fileInfo.setFilename((String) fileAttachement
				.getProperty(FILENAME_FIELD));
		fileInfo.setStorageFilename((String) fileAttachement
				.getProperty(STORAGE_FILENAME_FIELD));
		fileInfo.setContentType((String) fileAttachement
				.getProperty(CONTENT_TYPE_FIELD));
		fileInfo.setImageUrl((String) fileAttachement
				.getProperty(IMAGE_URL_FIELD));
		return fileInfo;
	}

	public void deleteFiles(String beobachtungsKey) {

		Map<String, Entity> fileAttachments = findFileAttachements(beobachtungsKey);

		for (Entity fileAttachement : fileAttachments.values()) {
			deleteFileAttachement(beobachtungsKey, fileAttachement);
		}

		setUpdateNeeded(beobachtungsKey);

	}

	private void deleteFileAttachement(String beobachtungsKey,
			Entity fileAttachement) {
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

	public void storeFiles(String beobachtungsKey, List<GwtFileInfo> fileInfos) {

		Map<String, Entity> fileAttachements = findFileAttachements(beobachtungsKey);

		if (fileInfos != null) {

			for (GwtFileInfo fileInfo : fileInfos) {

				fileAttachements.remove(fileInfo.getFilename());
				storeFile(beobachtungsKey, fileInfo);
			}

			for (Entity fileAttachement : fileAttachements.values()) {
				deleteFileAttachement(beobachtungsKey, fileAttachement);
			}
		}

		setUpdateNeeded(beobachtungsKey);

	}

	private void storeFile(String beobachtungsKey, GwtFileInfo fileInfo) {

		Entity fileAttachement = getFileAttachement(fileInfo.getFilename());
		if (fileAttachement == null) {
			fileAttachement = toEntity(fileInfo);
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

	private Entity toEntity(GwtFileInfo fileInfo) {
		String filename = fileInfo.getFilename();
		Key key = KeyFactory.createKey(FILE_KIND, filename);
		Entity fileAttachement = new Entity(key);
		fileAttachement.setProperty(FILENAME_FIELD, filename);
		fileAttachement.setProperty(STORAGE_FILENAME_FIELD,
				fileInfo.getStorageFilename());
		fileAttachement.setProperty(CONTENT_TYPE_FIELD,
				fileInfo.getContentType());
		fileAttachement.setProperty(IMAGE_URL_FIELD, fileInfo.getImageUrl());
		return fileAttachement;
	}

	private void deleteFileAttachement(Entity fileAttachement) {
		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {
			Key key = fileAttachement.getKey();
			deleteFromCache(key, FILE_DAO_MEMCACHE);
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
		GcsFilename gcsFilename = new GcsFilename(Config.getInstance().getBucketName(),
				storageFilename);
		try {
			gcsService.delete(gcsFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Entity> findFileAttachements(String beobachtungsKey) {

		MemcacheService cache = getCache(FILE_DAO_MEMCACHE);
		if (!cache.contains(beobachtungsKey) || updateNeeded(beobachtungsKey)) {
			synchronized (this) {
				if (!cache.contains(beobachtungsKey)
						|| updateNeeded(beobachtungsKey)) {
					setUpdated(beobachtungsKey);
					Collection<Key> cachedEntities = getAllFileAttachements(beobachtungsKey);
					cache.put(beobachtungsKey, cachedEntities);
				}
			}
		}
		@SuppressWarnings("unchecked")
		Collection<Key> keys = (Collection<Key>) cache.get(beobachtungsKey);
		return getFileAttachements(keys);
	}

	private Map<String, Entity> getFileAttachements(Collection<Key> keys) {
		final Map<String, Entity> fileAttachements = new HashMap<String, Entity>();
		for (Key key : keys) {
			Entity fileAttachement = getCachedEntity(key, FILE_DAO_MEMCACHE);
			String filename = (String) fileAttachement
					.getProperty(FILENAME_FIELD);
			fileAttachements.put(filename, fileAttachement);
		}
		return fileAttachements;
	}

	private Collection<Key> getAllFileAttachements(String beobachtungsKey) {

		Query query = new Query(FILE_KIND).setFilter(
				new FilterPredicate(BEOBACHTUNGS_KEYS_FIELD,
						FilterOperator.EQUAL, beobachtungsKey)).setKeysOnly();
		PreparedQuery preparedQuery = getDatastoreService().prepare(query);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		return convertToKeyList(preparedQuery.asList(fetchOptions));
	}

	private Collection<Key> convertToKeyList(List<Entity> entities) {
		Collection<Key> keys = new ArrayList<Key>();
		for (Entity entity : entities) {
			keys.add(entity.getKey());
		}
		return keys;
	}

	private void storeFileAttachement(Entity fileAttachement) {

		DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {
			datastoreService.put(fileAttachement);
			transaction.commit();
			insertIntoCache(fileAttachement, FILE_DAO_MEMCACHE);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}

	}

	private Entity getFileAttachement(String filename) {
		try {
			return getCachedEntity(KeyFactory.createKey(FILE_KIND, filename), FILE_DAO_MEMCACHE);
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
