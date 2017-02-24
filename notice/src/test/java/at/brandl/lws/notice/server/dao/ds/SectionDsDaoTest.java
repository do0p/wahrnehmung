package at.brandl.lws.notice.server.dao.ds;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.TestUtils;
import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.shared.util.Constants.Section;

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
	public void nameOfSectionWithParent() {

		// create
		sectionDao.storeSection(section);
		final String key = section.getKey();

		// child section
		GwtSection childSection = TestUtils.createSection(null, SECTION_NAME,
				key);
		sectionDao.storeSection(childSection);
		final String childKey = childSection.getKey();

		Assert.assertEquals(SECTION_NAME, sectionDao.getSectionName(key));
		Assert.assertEquals(SECTION_NAME + SectionDsDao.SEPARATOR + SECTION_NAME,
				sectionDao.getSectionName(childKey));
	}

	@Test
	public void duplicateNamesOnWithDifferentParents() {

		// create
		sectionDao.storeSection(section);
		final String key = section.getKey();
		Assert.assertNotNull(key);

		// child section
		GwtSection childSection = TestUtils.createSection(null, SECTION_NAME,
				key);
		sectionDao.storeSection(childSection);
		final String childKey = childSection.getKey();
		Assert.assertNotNull(childKey);

		// child section
		GwtSection grandChildSection = TestUtils.createSection(null,
				SECTION_NAME, childKey);
		sectionDao.storeSection(grandChildSection);
		final String grandChildKey = grandChildSection.getKey();
		Assert.assertNotNull(grandChildKey);

		// store duplicate
		GwtSection childSection2 = TestUtils.createSection(null, SECTION_NAME,
				key);
		try {
			sectionDao.storeSection(childSection2);
			Assert.fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	public void crud() {
		// create
		sectionDao.storeSection(section);
		final String key = section.getKey();
		Assert.assertNotNull(key);

		// read
		String sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(SECTION_NAME, sectionName);

		GwtSection storedSection = sectionDao.getSection(key);
		Assert.assertEquals(SECTION_NAME, storedSection.getSectionName());
		Assert.assertEquals(Boolean.FALSE, storedSection.getArchived());

		// read all
		final List<GwtSection> allSections = sectionDao.getAllSections();
		Assert.assertEquals(1, allSections.size());

		// update
		final String updatedSectionName = "XYZ";
		section.setSectionName(updatedSectionName);
		section.setArchived(Boolean.TRUE);
		sectionDao.storeSection(section);
		sectionName = sectionDao.getSectionName(key);
		Assert.assertEquals(updatedSectionName, sectionName);

		GwtSection updatedSection = sectionDao.getSection(key);
		Assert.assertEquals(updatedSectionName, updatedSection.getSectionName());
		Assert.assertEquals(Boolean.TRUE, updatedSection.getArchived());


		// store duplicate
		section.setKey(null);
		try {
			sectionDao.storeSection(section);
			Assert.fail("should have thrown exception");
		} catch (IllegalArgumentException e) {
			// success
		}
		Assert.assertNotNull(sectionDao.getSection(key));
		section.setKey(key);

		// delete
		sectionDao.deleteSections(Arrays.asList(key));

		Assert.assertNull(sectionDao.getSection(key));

	}

	@Test
	public void worksWithCache() {
		sectionDao.storeSection(section);
		final String key = section.getKey();

		removeFromDatastore(key);

		Assert.assertNotNull(sectionDao.getSection(key));
		clearCache();

		Assert.assertNull(sectionDao.getSection(key));
	}

	@Test
	public void worksWithoutCache() {
		sectionDao.storeSection(section);
		final String key = section.getKey();

		clearCache();

		Assert.assertNotNull(sectionDao.getSection(key));
		clearCache();

		sectionDao.deleteSections(Arrays.asList(key));
		Assert.assertNull(sectionDao.getSection(key));
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

		childKeys = sectionDao.getAllChildKeys(key);
		Assert.assertTrue(childKeys.isEmpty());
	}

	@Override
	protected String getMemCacheServiceName() {
		return Section.Cache.NAME;
	}

}
