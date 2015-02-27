package at.brandl.lws.notice.server.dao.ds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.service.dao.ds.NoticeArchiveDsDao;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNotice;
import at.brandl.lws.notice.shared.util.Constants.ArchiveNoticeGroup;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.Notice;
import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class NoticeArchiveDsDaoTest {

	private static final long NOW = System.currentTimeMillis() / 1000 * 1000;
	private static final long HOUR = 60 * 60 * 1000;

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
	public void testArchiv()  {

		datastore.put(createNotice(new Date(NOW)));
		datastore.put(createNotice(new Date(NOW - HOUR)));
		datastore.put(createNotice(new Date(NOW - 2 * HOUR)));
		Key first = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Key second = datastore.put(createNotice(new Date(NOW - 3 * HOUR)));
		Iterable<Entity> groups = createGroup(first, second);
		datastore.put(groups);
		
		int count = archiveDao.moveAllToArchiveBefore(new Date(NOW - HOUR));

		Assert.assertEquals(3, count);
		assertCountInDatastore(2, Notice.KIND);
		assertCountInDatastore(count, ArchiveNotice.KIND);
		assertCountInDatastore(0, NoticeGroup.KIND);
		assertCountInDatastore(1, ArchiveNoticeGroup.KIND);
	}

	private Iterable<Entity> createGroup(Key ...keys) {
		List<Entity> groups = new ArrayList<Entity>();
		Key parent = keys[0];
		for(int i = 1; i < keys.length; i++) {
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

}
