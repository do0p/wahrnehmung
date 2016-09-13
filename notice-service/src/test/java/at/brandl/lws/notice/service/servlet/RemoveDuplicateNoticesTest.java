package at.brandl.lws.notice.service.servlet;

import static at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum.LONG;
import static at.brandl.lws.notice.model.GwtBeobachtung.DurationEnum.SHORT;
import static at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum.ALONE;
import static at.brandl.lws.notice.model.GwtBeobachtung.SocialEnum.IN_GROUP;
import static at.brandl.lws.notice.service.TestUtil.entity;
import static at.brandl.lws.notice.service.TestUtil.key;
import static at.brandl.lws.notice.service.TestUtil.pair;
import static at.brandl.lws.notice.service.TestUtil.values;
import static at.brandl.lws.notice.shared.util.Constants.Notice.DATE;
import static at.brandl.lws.notice.shared.util.Constants.Notice.DURATION;
import static at.brandl.lws.notice.shared.util.Constants.Notice.SECTION;
import static at.brandl.lws.notice.shared.util.Constants.Notice.SOCIAL;
import static at.brandl.lws.notice.shared.util.Constants.Notice.TEXT;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class RemoveDuplicateNoticesTest {

	private static final int TIME = 1000000;
	private static final Date BEFORE = new Date(System.currentTimeMillis()
			- TIME);
	private static final Date AFTER = new Date(System.currentTimeMillis()
			+ TIME);
	private Comparator<Entity> comparator;
	private LocalServiceTestHelper helper;
	private RemoveDuplicateNotices servlet;

	@Before
	public void setUp() {

		helper = new LocalServiceTestHelper(
				new LocalDatastoreServiceTestConfig()
						.setApplyAllHighRepJobPolicy(),
				new LocalMemcacheServiceTestConfig());
		helper.setUp();

		comparator = new RemoveDuplicateNotices.EntityComparator();
		servlet = new RemoveDuplicateNotices();
	}

	@Test
	public void comparator() {

		Assert.assertEquals(0,
				comparator.compare(entity(values()), entity(values())));

		assertCompare(TEXT, new Text("a"), new Text("b"));
		assertCompare(DATE, BEFORE, AFTER);
		assertCompare(SECTION, key("a"), key("b"));
		assertCompare(DURATION, LONG.name(), SHORT.name());
		assertCompare(SOCIAL, ALONE.name(), IN_GROUP.name());
	}

	@Test
	public void findDuples() {

		Entity duple1 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Entity duple2 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Entity duple3 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Entity other = entity(values(pair(TEXT, new Text("b")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));

		Iterable<Entity> allNotices = Arrays.asList(duple1, duple2, duple3,
				other);

		Map<Key, Collection<Key>> duples = servlet.findDuples(allNotices, true);

		Assert.assertEquals(1, duples.size());
		Collection<Key> foundDuples = duples.get(duple1.getKey());
		Assert.assertEquals(2, foundDuples.size());
		Assert.assertTrue(foundDuples.contains(duple2.getKey()));
		Assert.assertTrue(foundDuples.contains(duple3.getKey()));
	}

	@Test
	public void findDuplesOneInGroup() {

		Entity duple1 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Entity duple2 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Entity duple3 = entity(values(pair(TEXT, new Text("a")),
				pair(DATE, BEFORE), pair(SECTION, key("a")),
				pair(DURATION, LONG.name()), pair(SOCIAL, ALONE.name())));
		Iterable<Entity> allNotices = Arrays.asList(duple1, duple2, duple3);

		servlet.setAllGroupedKeys(Arrays.asList(duple2.getKey()));
		
		Map<Key, Collection<Key>> duples = servlet.findDuples(allNotices, true);

		Assert.assertEquals(1, duples.size());
		Collection<Key> foundDuples = duples.get(duple2.getKey());
		Assert.assertEquals(2, foundDuples.size());
		Assert.assertTrue(foundDuples.contains(duple1.getKey()));
		Assert.assertTrue(foundDuples.contains(duple3.getKey()));
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	private void assertCompare(String key, Object smallValue, Object bigValue) {

		Object[] small = pair(key, smallValue);
		Object[] big = pair(key, bigValue);
		Assert.assertTrue(comparator.compare(entity(values(small)),
				entity(values())) < 0);
		Assert.assertTrue(comparator.compare(entity(values(small)),
				entity(values(big))) < 0);
		Assert.assertEquals(0, comparator.compare(entity(values(small)),
				entity(values(small))));
		Assert.assertEquals(0,
				comparator.compare(entity(values(big)), entity(values(big))));
		Assert.assertTrue(comparator.compare(entity(values(big)),
				entity(values(small))) > 0);
		Assert.assertTrue(comparator.compare(entity(values()),
				entity(values(small))) > 0);
	}
}
