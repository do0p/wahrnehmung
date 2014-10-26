package at.lws.wnm.server.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import at.lws.wnm.server.dao.ds.FileDsDao;

public class FileDsDaoTest extends AbstractDsDaoTest {

	private static final String STORAGE_FILENAME = "storageFilename";
	private static final String FILENAME = "filename";
	private static final String BEOBACHTUNGS_KEY = "beobachtungsKey";
	private static final String BEOBACHTUNGS_KEY_2 = "beobachtungsKey2";
	private FileDsDao fileDao;
	private Map<String, String> filenames;

	@Before
	public void setUp() {

		fileDao = DaoRegistry.get(FileDsDao.class);
		filenames = new HashMap<String, String>();
		filenames.put(FILENAME, STORAGE_FILENAME);

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

		assertFilenames(BEOBACHTUNGS_KEY);
		assertFilenames(BEOBACHTUNGS_KEY_2);

		fileDao.deleteFiles(BEOBACHTUNGS_KEY);

		Assert.assertTrue(fileDao.fileExists(FILENAME));
		assertFilenames(BEOBACHTUNGS_KEY_2);
		Map<String, String> result = fileDao.getFilenames(BEOBACHTUNGS_KEY);
		Assert.assertTrue(result.isEmpty());

		fileDao.deleteFiles(BEOBACHTUNGS_KEY_2);
		Assert.assertFalse(fileDao.fileExists(FILENAME));
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
		Map<String, String> result = fileDao.getFilenames(beobachtungsKey);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(STORAGE_FILENAME, result.get(FILENAME));
	}

}
