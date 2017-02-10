package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.converter.GwtChildConverter.getEntityConverter;
import static at.brandl.lws.notice.shared.util.Constants.Child.KIND;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.CacheUtil;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.server.dao.ds.converter.GwtChildConverter;
import at.brandl.lws.notice.server.dao.ds.converter.GwtChildConverter.ChildSelector;
import at.brandl.lws.notice.server.dao.ds.converter.GwtChildConverter.KeySelector;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.Child.Cache;

public class ChildDsDao extends AbstractDsDao {

	private static final Function<Entity, GwtChild> ENTITY_CONVERTER = getEntityConverter();
	private static final EntityListSupplier<GwtChild> CHILDRENLIST_SUPPLIER = new EntityListSupplier<GwtChild>(
			new Query(KIND), ENTITY_CONVERTER);

	public GwtChild getChild(String key) {

		return getCachedChild(key);
	}

	public List<GwtChild> getAllChildren() {

		return getCachedChildrenList();
	}

	public void storeChild(GwtChild gwtChild) throws IllegalArgumentException {

		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();

		try {
			assertCacheIsLoaded();

			String keyForUpdate = gwtChild.getKey();

			if (keyForUpdate == null && exists(gwtChild)) {
				transaction.rollback();
				throw new IllegalArgumentException(formatChildName(gwtChild) + " existiert bereits!");

			}

			Entity child = GwtChildConverter.toEntity(gwtChild);
			datastoreService.put(child);
			transaction.commit();
			gwtChild.setKey(DsUtil.toString(child.getKey()));
			CacheUtil.updateCachedResult(Cache.ALL_CHILDREN, gwtChild, new KeySelector(keyForUpdate), getCache());

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void deleteChild(String childKey) {

		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();

		try {
			datastoreService.delete(DsUtil.toKey(childKey));
			transaction.commit();
			assertCacheIsLoaded();
			CacheUtil.removeFromCachedResult(Cache.ALL_CHILDREN, new KeySelector(childKey), getCache());
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public String getChildName(String childKey) {
		return formatChildName(getCachedChild(childKey));
	}

	public static String formatChildName(GwtChild child) {
		return child.getFirstName() + " " + child.getLastName();
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

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

	private GwtChild getCachedChild(String key) {
		return CacheUtil.getFromCachedList(new KeySelector(key),
				new EntitySupplier<GwtChild>(DsUtil.toKey(key), ENTITY_CONVERTER), Cache.ALL_CHILDREN,
				CHILDRENLIST_SUPPLIER, Child.class, getCache());
	}

	private List<GwtChild> getCachedChildrenList() {
		return CacheUtil.getCached(Cache.ALL_CHILDREN, CHILDRENLIST_SUPPLIER, Child.class, getCache());
	}

	private void assertCacheIsLoaded() {
		getCachedChildrenList();
	}

	private boolean exists(GwtChild child) {

		return CacheUtil.getFromCachedList(new ChildSelector(child), null, Cache.ALL_CHILDREN, CHILDRENLIST_SUPPLIER,
				Child.class, getCache()) != null;
	}

}
