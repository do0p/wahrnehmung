package at.brandl.lws.notice.service.servlet;


import static at.brandl.lws.notice.service.TestUtil.entity;
import static at.brandl.lws.notice.service.TestUtil.key;
import static at.brandl.lws.notice.service.TestUtil.pair;
import static at.brandl.lws.notice.service.TestUtil.values;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.shared.util.Constants.NoticeGroup;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class RemoveDuplicateGroupsTest {

	
	private RemoveDuplicateGroups servlet;
	private LocalServiceTestHelper helper;

	@Before
	public void setUp() {

		helper = new LocalServiceTestHelper(
				new LocalDatastoreServiceTestConfig()
						.setApplyAllHighRepJobPolicy(),
				new LocalMemcacheServiceTestConfig());
		helper.setUp();

		servlet = new RemoveDuplicateGroups();
	}
	
	@Test
	public void findDuples() {
		
		Entity duple1 = entity(values(pair(NoticeGroup.BEOBACHTUNG, key("a"))));
		Entity duple2 = entity(values(pair(NoticeGroup.BEOBACHTUNG, key("a"))));
		Entity duple3 = entity(values(pair(NoticeGroup.BEOBACHTUNG, key("a"))));
		Entity other = entity(values(pair(NoticeGroup.BEOBACHTUNG, key("b"))));
		
		Iterable<Entity> allGroups = Arrays.asList(duple1, duple2, duple3, other);
		Map<Key, Collection<Key>> duples = servlet.findDuples(allGroups, true);
		

		Assert.assertEquals(1, duples.size());
		Collection<Key> foundDuples = duples.get(duple1.getKey());
		Assert.assertEquals(2, foundDuples.size());
		Assert.assertTrue(foundDuples.contains(duple2.getKey()));
		Assert.assertTrue(foundDuples.contains(duple3.getKey()));
		
		
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}

	
}
