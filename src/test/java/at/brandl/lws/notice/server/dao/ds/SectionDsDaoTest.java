package at.brandl.lws.notice.server.dao.ds;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.TestUtils;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.shared.model.GwtSection;

public class SectionDsDaoTest extends AbstractDsDaoTest {

	private static final String SECTION_NAME = "Bereich";
	private SectionDsDao sectionDao;
	private GwtSection section;

	@Before
	public void setUp() {
		sectionDao = DaoRegistry.get(SectionDsDao.class);
		section = TestUtils.createSection(null, SECTION_NAME, null);
	}

	@Test
	public void crud() {
		// create
		sectionDao.storeSection(section);
		final String key = section.getKey();
		Assert.assertNotNull(key);
		assertServicesContains(key);

		// read
		String sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(SECTION_NAME, sectionName);

		// read all
		final List<GwtSection> allSections = sectionDao.getAllSections();
		Assert.assertEquals(1, allSections.size());

		// update
		final String updatedSectionName = "XYZ";
		section.setSectionName(updatedSectionName);
		sectionDao.storeSection(section);
		sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(updatedSectionName, sectionName);
		assertServicesContains(key);

		// store duplicate
		section.setKey(null);
		try {
			sectionDao.storeSection(section);
			Assert.fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// success
		}
		assertServicesContains(key);
		section.setKey(key);

		// delete
		sectionDao.deleteSections(Arrays.asList(key));

		assertServicesContainsNot(key);

	}

	@Test
	public void worksWithCache() {
		sectionDao.storeSection(section);
		final String key = section.getKey();

		assertServicesContains(key);

		removeFromDatastore(key);

		final String sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(SECTION_NAME, sectionName);
		assertCacheContains(key);

		removeFromCache(key);

		assertServicesContainsNot(key);
	}

	@Test
	public void worksWithoutCache() {
		sectionDao.storeSection(section);
		final String key = section.getKey();

		assertServicesContains(key);

		removeFromCache(key);

		final String sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(SECTION_NAME, sectionName);
		assertServicesContains(key);

		removeFromCache(key);

		sectionDao.deleteSections(Arrays.asList(key));
		assertServicesContainsNot(key);
	}

	@Test
	public void childKeys() {
		sectionDao.storeSection(section);
		final String key = section.getKey();

		Collection<String> childKeys = sectionDao.getAllChildKeys(key);
		Assert.assertTrue(childKeys.isEmpty());

		final GwtSection subSection1 = TestUtils.createSection(null,
				"subsection1", key);
		sectionDao.storeSection(subSection1);
		final String child1Key = subSection1.getKey();

		childKeys = sectionDao.getAllChildKeys(key);
		Assert.assertEquals(new HashSet<String>(Arrays.asList(child1Key)),
				childKeys);

		childKeys = sectionDao.getAllChildKeys(child1Key);
		Assert.assertTrue(childKeys.isEmpty());

		final GwtSection subSection2 = TestUtils.createSection(null,
				"subsection2", child1Key);
		sectionDao.storeSection(subSection2);
		final String child2Key = subSection2.getKey();

		childKeys = sectionDao.getAllChildKeys(key);
		Assert.assertEquals(
				new HashSet<String>(Arrays.asList(child1Key, child2Key)),
				childKeys);

		childKeys = sectionDao.getAllChildKeys(child1Key);
		Assert.assertEquals(new HashSet<String>(Arrays.asList(child2Key)),
				childKeys);

		childKeys = sectionDao.getAllChildKeys(child2Key);
		Assert.assertTrue(childKeys.isEmpty());

		sectionDao.deleteSections(Arrays.asList(child1Key, child2Key));
		assertServicesContains(key);
		assertServicesContainsNot(child1Key);
		assertServicesContainsNot(child2Key);

		childKeys = sectionDao.getAllChildKeys(key);
		Assert.assertTrue(childKeys.isEmpty());
	}

	@Override
	protected String getMemCacheServiceName() {
		return SectionDsDao.SECTION_MEMCACHE;
	}

}
