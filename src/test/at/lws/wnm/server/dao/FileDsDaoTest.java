package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.lws.wnm.server.dao.ds.FileDsDao;
import at.lws.wnm.shared.model.GwtFileInfo;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class FileDsDaoTest extends AbstractDsDaoTest {

	private static final String BEOBACHTUNGS_KEY = "beobachtungsKey";
	private static final String BEOBACHTUNGS_KEY_2 = "beobachtungsKey2";
	private static final String FILENAME = "filename";
	private static final String FILENAME_2 = "filename2";
	private static final String STORAGE_FILENAME = "storageFilename";
	private static final String STORAGE_FILENAME_2 = "storageFilename2";
	private static final String CONTENT_TYPE = "contentType";
	private FileDsDao fileDao;
	private List<GwtFileInfo> filenames;

	@Before
	public void setUp() {

		fileDao = DaoRegistry.get(FileDsDao.class);
		filenames = new ArrayList<GwtFileInfo>();
		filenames.add(createFileInfo(FILENAME, STORAGE_FILENAME));

	}

	@Test
	public void crud() {
		Assert.assertFalse(fileDao.fileExists(FILENAME));

		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		Assert.assertTrue(fileDao.fileExists(FILENAME));
		assertFilenames(BEOBACHTUNGS_KEY);

		fileDao.deleteFiles(BEOBACHTUNGS_KEY);
		Assert.assertFalse(fileDao.fileExists(FILENAME));

	}

	@Test
	public void crudTwoFiles() {
		Assert.assertFalse(fileDao.fileExists(FILENAME));

		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);
		fileDao.storeFiles(BEOBACHTUNGS_KEY_2, filenames);

		Assert.assertTrue(fileDao.fileExists(FILENAME));

		assertFilenames(BEOBACHTUNGS_KEY_2);
		assertFilenames(BEOBACHTUNGS_KEY);

		fileDao.deleteFiles(BEOBACHTUNGS_KEY);

		Assert.assertTrue(fileDao.fileExists(FILENAME));
		assertFilenames(BEOBACHTUNGS_KEY_2);
		List<GwtFileInfo> result = fileDao.getFileInfos(BEOBACHTUNGS_KEY);
		Assert.assertTrue(result.isEmpty());

		fileDao.deleteFiles(BEOBACHTUNGS_KEY_2);
		Assert.assertFalse(fileDao.fileExists(FILENAME));
	}

	@Test
	public void safeDifferences() {
		Assert.assertFalse(fileDao.fileExists(FILENAME));
		Assert.assertFalse(fileDao.fileExists(FILENAME_2));

		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		Assert.assertTrue(fileDao.fileExists(FILENAME));
		Assert.assertFalse(fileDao.fileExists(FILENAME_2));

		filenames.add(createFileInfo(FILENAME_2, STORAGE_FILENAME_2));
		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		Assert.assertTrue(fileDao.fileExists(FILENAME));
		Assert.assertTrue(fileDao.fileExists(FILENAME_2));

		filenames.remove(createFileInfo(FILENAME, STORAGE_FILENAME));
		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		Assert.assertFalse(fileDao.fileExists(FILENAME));
		Assert.assertTrue(fileDao.fileExists(FILENAME_2));

		fileDao.storeFiles(BEOBACHTUNGS_KEY, Collections.EMPTY_LIST);

		Assert.assertFalse(fileDao.fileExists(FILENAME));
		Assert.assertFalse(fileDao.fileExists(FILENAME_2));

	}

	@Test
	public void worksWithCache() {
		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		final String key = FILENAME;

		assertServicesContains(key);

		removeFromDatastore(key);

		assertCacheContains(key);
		Assert.assertTrue(fileDao.fileExists(FILENAME));

		removeFromCache(key);

		assertServicesContainsNot(key);
		Assert.assertFalse(fileDao.fileExists(FILENAME));
	}

	@Test
	public void worksWithoutCache() {
		fileDao.storeFiles(BEOBACHTUNGS_KEY, filenames);

		final String key = FILENAME;

		assertServicesContains(key);

		removeFromCache(key);

		Assert.assertTrue(fileDao.fileExists(FILENAME));

		assertServicesContains(key);
		Assert.assertTrue(fileDao.fileExists(FILENAME));

		removeFromCache(key);

		fileDao.deleteFiles(BEOBACHTUNGS_KEY);
		assertServicesContainsNot(key);
	}

	@Override
	protected Key toKey(String key) {
		return KeyFactory.createKey(FileDsDao.FILE_KIND, key);
	}

	@Override
	protected String getMemCacheServiceName() {
		return FileDsDao.FILE_DAO_MEMCACHE;
	}

	private void assertFilenames(String beobachtungsKey) {
		List<GwtFileInfo> result = fileDao.getFileInfos(beobachtungsKey);
		Assert.assertEquals(1, result.size());
		GwtFileInfo fileInfo = getFileInfo(result, FILENAME);
		Assert.assertNotNull(fileInfo);
		Assert.assertEquals(STORAGE_FILENAME, fileInfo.getStorageFilename());
		Assert.assertEquals(CONTENT_TYPE, fileInfo.getContentType());
	}

	private GwtFileInfo getFileInfo(List<GwtFileInfo> result, String filename) {
		for (GwtFileInfo fileInfo : result) {
			if (fileInfo.getFilename().equals(filename)) {
				return fileInfo;
			}
		}
		return null;
	}

	private GwtFileInfo createFileInfo(String filename, String storageFilename) {
		GwtFileInfo fileInfo = new GwtFileInfo();
		fileInfo.setFilename(filename);
		fileInfo.setStorageFilename(storageFilename);
		fileInfo.setContentType(CONTENT_TYPE);
		return fileInfo;
	}

}
