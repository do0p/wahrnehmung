package at.brandl.lws.notice.service.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum;
import at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNotice;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNoticeGroup;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.MigrationKeyMapping;
import at.brandl.lws.notice.shared.util.Constants.MigrationKeyMapping.KeyMappingType;
import at.brandl.lws.notice.shared.util.Constants.Notice;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class NoticeArchiveDsDaoTest {

	private static final long NOW = System.currentTimeMillis() / 1000 * 1000;
	private static final long HOUR = 60 * 60 * 1000;
	private static final Date FIRST_DATE = new Date(NOW - 48 * HOUR);
	private static final Date DEFAULT_DATE = new Date();
	private static final String FIRST_TEXT = "a";
	private static final String DEFAULT_TEXT = "b";
	private static final SocialEnum FIRST_SOCIAL = SocialEnum.ALONE;
	private static final SocialEnum DEFAULT_SOCIAL = SocialEnum.IN_GROUP;
	private static final DurationEnum FIRST_DURATION = DurationEnum.MEDIUM;
	private static final DurationEnum DEFAULT_DURATION = DurationEnum.SHORT;

	private NoticeArchiveDsDao archiveDao;
	private LocalServiceTestHelper helper;
	private DatastoreService datastore;
	private Key childKey;

	@Before
	public void setUp() {
		helper = new LocalServiceTestHelper(
				new LocalDatastoreServiceTestConfig()
						.setApplyAllHighRepJobPolicy(),
				new LocalMemcacheServiceTestConfig());
		helper.setUp();
		datastore = DatastoreServiceFactory.getDatastoreService();

		archiveDao = new NoticeArchiveDsDao();
		childKey = datastore.put(new Entity(Child.KIND));
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testArchiveNotice() {

		Key noticeKey = datastore.put(createNotice(new Date(NOW)));

		archiveDao.moveNoticeToArchive(noticeKey);

		assertCountInDatastore(0, Notice.KIND);
		assertCountInDatastore(1, ArchiveNotice.KIND);
		assertCountInDatastore(1, MigrationKeyMapping.KIND);

		List<Entity> archivedList = query(new Query(ArchiveNotice.KIND));
		Assert.assertEquals(1, archivedList.size());
		Entity archived = archivedList.get(0);

		List<Entity> keyMappingList = query(archiveDao.createQuery(noticeKey,
				KeyMappingType.ARCHIVE_NOTICE));
		Assert.assertEquals(1, keyMappingList.size());
		Entity keyMapping = keyMappingList.get(0);

		Assert.assertEquals(archived.getKey(),
				keyMapping.getProperty(MigrationKeyMapping.NEW_KEY));

	}

	@Test(expected = IllegalStateException.class)
	public void testArchiveGroupNoticeMissingFirst() {

		Key first = KeyFactory.createKey(Notice.KIND, "fake");
		Key second = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));

		Iterable<Entity> groups = createGroup(first, second);
		datastore.put(groups);

		archiveDao.moveGroupsToArchive(first);
	}

	@Test(expected = IllegalStateException.class)
	public void testArchiveGroupNoticeMissingSecond() {

		Key first = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Key second = KeyFactory.createKey(Notice.KIND, "fake");

		Iterable<Entity> groups = createGroup(first, second);
		datastore.put(groups);

		archiveDao.moveGroupsToArchive(first);
	}

	@Test
	public void testArchiveGroupNoticeMovedFirst() {

		Key first = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Key second = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));

		archiveDao.moveNoticeToArchive(first, datastore);
		archiveDao.moveNoticeToArchive(second, datastore);

		assertCountInDatastore(0, Notice.KIND);
		assertCountInDatastore(2, ArchiveNotice.KIND);
		assertCountInDatastore(2, MigrationKeyMapping.KIND);

		Iterable<Entity> groups = createGroup(first, second);
		datastore.put(groups);

		int count = archiveDao.moveGroupsToArchive(first);

		Assert.assertEquals(1, count);

		assertCountInDatastore(0, NoticeGroup.KIND);
		assertCountInDatastore(1, ArchiveNoticeGroup.KIND);
		assertCountInDatastore(3, MigrationKeyMapping.KIND);

		List<Entity> archivedGroupList = query(new Query(
				ArchiveNoticeGroup.KIND));
		Assert.assertEquals(1, archivedGroupList.size());
		Entity archivedGroup = archivedGroupList.get(0);

		List<Entity> keyMappingList = query(archiveDao.createQuery(groups
				.iterator().next().getKey(), KeyMappingType.ARCHIVE_GROUP));
		Assert.assertEquals(1, keyMappingList.size());
		Entity keyMappingGroup = keyMappingList.get(0);

		Assert.assertEquals(archivedGroup.getKey(),
				keyMappingGroup.getProperty(MigrationKeyMapping.NEW_KEY));
	}

	@Test
	public void testGetAllNotices() {

		Key key1 = datastore.put(createNotice(new Date(NOW)));
		Key key2 = datastore.put(createNotice(new Date(NOW - HOUR)));
		Key key3 = datastore.put(createNotice(new Date(NOW - 2 * HOUR)));

		Set<Key> keys = archiveDao.getAllNoticeKeysBefore(new Date(NOW - HOUR));

		Assert.assertEquals(1, keys.size());
		Assert.assertFalse(keys.contains(key1));
		Assert.assertFalse(keys.contains(key2));
		Assert.assertTrue(keys.contains(key3));
	}

	@Test
	public void testGetAllNoticeGroupKeys() {

		Key first = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Key second = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Iterable<Entity> groups = createGroup(first, second);
		datastore.put(groups);

		Set<Key> parentKeys = archiveDao
				.getAllGroupParentKeys(new HashSet<Key>(Arrays.asList(first,
						second)));

		Assert.assertEquals(1, parentKeys.size());
		Assert.assertTrue(parentKeys.contains(first));
	}

	@Test
	public void testGetSortedNotices() {

		Key firstSection = KeyFactory.createKey("section", "a");
		Key defaultSection = KeyFactory.createKey("section", "b");
		Entity firstNotice = createNotice(DEFAULT_DATE, FIRST_TEXT,
				defaultSection, DEFAULT_SOCIAL, DEFAULT_DURATION);
		Entity secondNotice = createNotice(FIRST_DATE, DEFAULT_TEXT,
				defaultSection, DEFAULT_SOCIAL, DEFAULT_DURATION);
		Entity thirdNotice = createNotice(DEFAULT_DATE, DEFAULT_TEXT,
				firstSection, DEFAULT_SOCIAL, DEFAULT_DURATION);
		Entity fourthNotice = createNotice(DEFAULT_DATE, DEFAULT_TEXT,
				defaultSection, DEFAULT_SOCIAL, FIRST_DURATION);
		Entity fifthNotice = createNotice(DEFAULT_DATE, DEFAULT_TEXT,
				defaultSection, FIRST_SOCIAL, DEFAULT_DURATION);

		datastore.put(thirdNotice);
		datastore.put(fourthNotice);
		datastore.put(fifthNotice);
		datastore.put(firstNotice);
		datastore.put(secondNotice);

		Iterator<Entity> notices = archiveDao.getAllNoticesSorted(childKey,
				false).iterator();
		Assert.assertTrue(notices.hasNext());
		Assert.assertEquals(firstNotice, notices.next());
		Assert.assertEquals(secondNotice, notices.next());
		Assert.assertEquals(thirdNotice, notices.next());
		Assert.assertEquals(fourthNotice, notices.next());
		Assert.assertEquals(fifthNotice, notices.next());
	}

	
	@Test
	public void testAllGroupKeys() {
		Key first = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Key second = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Iterable<Entity> groups = createGroup(first, second);
		
		datastore.put(groups);
		
		Set<Key> allGroupedKeys = archiveDao.getAllGroupedKeys(false);
		Assert.assertTrue(allGroupedKeys.contains(first));
		Assert.assertTrue(allGroupedKeys.contains(second));
	}
	
	private List<Entity> query(Query query) {
		return datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
	}

	private Iterable<Entity> createGroup(Key... keys) {
		List<Entity> groups = new ArrayList<Entity>();
		Key parent = keys[0];
		for (int i = 1; i < keys.length; i++) {
			Entity group = new Entity(NoticeGroup.KIND, parent);
			group.setProperty(NoticeGroup.BEOBACHTUNG, keys[i]);
			groups.add(group);
		}
		return groups;
	}

	private void assertCountInDatastore(int expected, String kind) {
		Assert.assertEquals(expected, datastore.prepare(new Query(kind))
				.countEntities(FetchOptions.Builder.withDefaults()));
	}

	private Entity createNotice(Date date) {
		Entity notice = new Entity(Notice.KIND, childKey);
		notice.setProperty(Notice.DATE, date);
		return notice;
	}

	private Entity createNotice(Date date, String text, Key section,
			SocialEnum social, DurationEnum duration) {
		Entity notice = createNotice(date);
		notice.setProperty(Notice.TEXT, new Text(text));
		notice.setProperty(Notice.SECTION, section);
		notice.setProperty(Notice.SOCIAL, social.name());
		notice.setProperty(Notice.DURATION, duration.name());
		return notice;
	}

}
