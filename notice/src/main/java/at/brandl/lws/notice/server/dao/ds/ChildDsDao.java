package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.common.base.Predicate;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.CacheUtil;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.Child.Cache;

public class ChildDsDao extends AbstractDsDao {

	private static final Object LOCK = new Object();

	public List<GwtChild> getAllChildren() {

		List<GwtChild> allChildren = getChildrenListFromCache();
		if (allChildren == null) {
			synchronized (LOCK) {

				allChildren = getChildrenListFromCache();

				if (allChildren == null) {
					allChildren = getAllChildrenFromDatastore();
					Collections.sort(allChildren);
					addChildrenListToCache(allChildren);
				}
			}
		}
		return allChildren;
	}

	public void storeChild(GwtChild gwtChild) throws IllegalArgumentException {

		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {
			final String keyForUpdate = gwtChild.getKey();
			final Entity child = toEntity(gwtChild);

			if (!child.getKey().isComplete()) {
				if (exists(child, datastoreService)) {
					transaction.rollback();
					throw new IllegalArgumentException(formatChildName(child) + " existiert bereits!");
				}
			}

			datastoreService.put(child);
			transaction.commit();
			gwtChild.setKey(DsUtil.toString(child.getKey()));
			insertIntoCache(child, Cache.NAME);
			CacheUtil.updateCachedResult(Cache.ALL_CHILDREN, gwtChild, new Predicate<GwtChild>() {
				@Override
				public boolean apply(GwtChild gwtChild) {
					return keyForUpdate != null && keyForUpdate.equals(gwtChild.getKey());
				}
			}, getCache());

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void deleteChild(String childKey) {
		deleteEntity(DsUtil.toKey(childKey), Cache.NAME);
		boolean success = false;
		while (!success) {
			IdentifiableValue value = getCache().getIdentifiable(Cache.ALL_CHILDREN);
			if (value == null) {
				break;
			}

			@SuppressWarnings("unchecked")
			List<GwtChild> allChildren = (List<GwtChild>) value.getValue();
			Iterator<GwtChild> iterator = allChildren.iterator();
			while (iterator.hasNext()) {
				GwtChild child = iterator.next();
				if (child.getKey().equals(childKey)) {
					iterator.remove();
					break;
				}
			}
			success = getCache().putIfUntouched(Cache.ALL_CHILDREN, value, allChildren);
		}
	}

	public GwtChild getChild(String key) {
		return toGwt(getCachedEntity(DsUtil.toKey(key), Cache.NAME));
	}

	public String getChildName(String childKey) {
		return formatChildName(getCachedEntity(DsUtil.toKey(childKey), Cache.NAME));
	}

	private boolean exists(Entity child, DatastoreService datastoreService) {
		final Filter filter = createChildFilter(child);
		final Query query = new Query(Child.KIND).setFilter(filter);
		return count(query, withDefaults(), datastoreService) > 0;
	}

	private Filter createChildFilter(Entity child) {
		final Filter firstnamePredicate = createEqualsPredicate(Child.FIRSTNAME, child);
		final Filter lastnamePredicate = createEqualsPredicate(Child.LASTNAME, child);
		final Filter birthdayPredicate = createEqualsPredicate(Child.BIRTHDAY, child);
		return new Query.CompositeFilter(CompositeFilterOperator.AND,
				Arrays.asList(firstnamePredicate, lastnamePredicate, birthdayPredicate));
	}

	private List<GwtChild> mapToGwtChildren(Iterable<Entity> resultList) {
		final List<GwtChild> result = new ArrayList<GwtChild>();
		for (Entity child : resultList) {
			result.add(toGwt(child));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private GwtChild toGwt(Entity child) {
		final GwtChild gwtChild = new GwtChild();
		gwtChild.setKey(DsUtil.toString(child.getKey()));
		gwtChild.setFirstName((String) child.getProperty(Child.FIRSTNAME));
		gwtChild.setLastName((String) child.getProperty(Child.LASTNAME));
		gwtChild.setBirthDay((Date) child.getProperty(Child.BIRTHDAY));
		gwtChild.setBeginYear((Long) child.getProperty(Child.BEGIN_YEAR));
		gwtChild.setBeginGrade((Long) child.getProperty(Child.BEGIN_GRADE));
		gwtChild.setArchived((Boolean) child.getProperty(Child.ARCHIVED));
		gwtChild.setDevelopementDialogueDates((List<Date>) child.getProperty(Child.DEVELOPEMENT_DIALOGUE_DATES));
		return gwtChild;
	}

	private Entity toEntity(GwtChild gwtChild) {
		final String key = gwtChild.getKey();
		final Entity child;
		if (key == null) {
			child = new Entity(Child.KIND);
		} else {
			child = new Entity(DsUtil.toKey(key));
		}
		child.setProperty(Child.FIRSTNAME, gwtChild.getFirstName());
		child.setProperty(Child.LASTNAME, gwtChild.getLastName());
		child.setProperty(Child.BIRTHDAY, gwtChild.getBirthDay());
		child.setProperty(Child.BEGIN_YEAR, gwtChild.getBeginYear());
		child.setProperty(Child.BEGIN_GRADE, gwtChild.getBeginGrade());
		child.setProperty(Child.ARCHIVED, gwtChild.getArchived());
		List<Date> developementDialogueDates = gwtChild.getDevelopementDialogueDates();
		if (developementDialogueDates != null && !developementDialogueDates.isEmpty()) {
			Collections.sort(developementDialogueDates);
			child.setProperty(Child.DEVELOPEMENT_DIALOGUE_DATES, developementDialogueDates);
			child.setProperty(Child.LAST_DEVELOPEMENT_DIALOGUE_DATE,
					developementDialogueDates.get(developementDialogueDates.size() - 1));
		}
		return child;
	}

	public static String formatChildName(Entity child) {
		return child.getProperty(Child.FIRSTNAME) + " " + child.getProperty(Child.LASTNAME);
	}

	public Collection<GwtChild> getAllChildrenOver12() {
		Collection<GwtChild> result = new ArrayList<GwtChild>();
		Date twelveYearsAgo = getDateTwelveYearsAgo();
		for (GwtChild child : getAllChildren()) {
			Date birthDay = child.getBirthDay();
			if (birthDay != null && !birthDay.after(twelveYearsAgo)) {
				result.add(child);
			}
		}
		return result;
	}

	private Date getDateTwelveYearsAgo() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		calendar.roll(Calendar.YEAR, -12);
		Date twelveYearsAgo = calendar.getTime();
		return twelveYearsAgo;
	}

	public Collection<GwtChild> getAllChildrenUnder12() {
		Collection<GwtChild> result = new ArrayList<GwtChild>();
		Date twelveYearsAgo = getDateTwelveYearsAgo();
		for (GwtChild child : getAllChildren()) {
			Date birthDay = child.getBirthDay();
			if (birthDay != null && birthDay.after(twelveYearsAgo)) {
				result.add(child);
			}
		}
		return result;
	}

	private void addChildrenListToCache(List<GwtChild> allChildren) {
		getCache().put(Cache.ALL_CHILDREN, allChildren);
	}

	@SuppressWarnings("unchecked")
	private List<GwtChild> getChildrenListFromCache() {
		return (List<GwtChild>) getCache().get(Cache.ALL_CHILDREN);
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

	private List<GwtChild> getAllChildrenFromDatastore() {
		final Query query = new Query(Child.KIND);
		return mapToGwtChildren(execute(query, withDefaults()));
	}

}
