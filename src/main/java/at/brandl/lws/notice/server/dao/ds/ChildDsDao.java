package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.validator.GwtChildValidator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Transaction;

public class ChildDsDao extends AbstractDsDao {

	public static final String CHILD_DAO_MEMCACHE = "childDao";
	public static final String CHILD_KIND = "ChildDs";
	public static final String FIRSTNAME_FIELD = "firstname";
	public static final String LASTNAME_FIELD = "lastname";
	public static final String BIRTHDAY_FIELD = "birthday";
	public static final String LAST_DEVELOPEMENT_DIALOGUE_DATE = "lastDialogueDate";
	public static final String DEVELOPEMENT_DIALOGUE_DATES = "dialogueDates";
	private static final String ALL_CHILDREN = "allChildren";
	
	private boolean dirty = true;

	@SuppressWarnings("unchecked")
	public List<GwtChild> getAllChildren() {
		
		List<GwtChild> allChildren;
		
		if(dirty)
		{
			allChildren = updateCacheFromDatastore();
		} else {
			allChildren = (List<GwtChild>) getCache().get(ALL_CHILDREN);
			if(allChildren == null) {
				allChildren = updateCacheFromDatastore();
			}
		}
		
		return allChildren;
	}

	private List<GwtChild> updateCacheFromDatastore() {
		List<GwtChild> allChildren = getAllChildrenFromDatastore();
		getCache().put(ALL_CHILDREN, allChildren);
		dirty = false;
		return allChildren;
	}

	private List<GwtChild> getAllChildrenFromDatastore() {
		final Query query = new Query(CHILD_KIND).addSort(LASTNAME_FIELD)
				.addSort(FIRSTNAME_FIELD).addSort(BIRTHDAY_FIELD);
		return mapToGwtChildren(execute(query, withDefaults()));
	}

	public void storeChild(GwtChild gwtChild) throws IllegalArgumentException {
		if(!GwtChildValidator.validate(gwtChild)) {
			throw new IllegalArgumentException("child is not valid");
		}
		
		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {

			final Entity child = toEntity(gwtChild);

			if (!child.getKey().isComplete()) {
				if (exists(child, datastoreService)) {
					transaction.rollback();
					throw new IllegalArgumentException(formatChildName(child)
							+ " existiert bereits!");
				}
			}

			datastoreService.put(child);
			transaction.commit();
			gwtChild.setKey(toString(child.getKey()));
			insertIntoCache(child);
			dirty = true;
			
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void deleteChild(GwtChild child) {
		deleteEntity(toKey(child.getKey()));
		dirty = true;
	}

	public GwtChild getChild(String key) {
		return toGwt(getCachedEntity(toKey(key)));
	}

	public String getChildName(String childKey) {
		return formatChildName(getCachedEntity(toKey(childKey)));
	}

	@Override
	protected String getMemcacheServiceName() {
		return CHILD_DAO_MEMCACHE;
	}

	private boolean exists(Entity child, DatastoreService datastoreService) {
		final Filter filter = createChildFilter(child);
		final Query query = new Query(CHILD_KIND).setFilter(filter);
		return count(query, withDefaults(), datastoreService) > 0;
	}

	private Filter createChildFilter(Entity child) {
		final Filter firstnamePredicate = createEqualsPredicate(
				FIRSTNAME_FIELD, child);
		final Filter lastnamePredicate = createEqualsPredicate(LASTNAME_FIELD,
				child);
		final Filter birthdayPredicate = createEqualsPredicate(BIRTHDAY_FIELD,
				child);
		return new Query.CompositeFilter(CompositeFilterOperator.AND,
				Arrays.asList(firstnamePredicate, lastnamePredicate,
						birthdayPredicate));
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
		gwtChild.setKey(toString(child.getKey()));
		gwtChild.setFirstName((String) child.getProperty(FIRSTNAME_FIELD));
		gwtChild.setLastName((String) child.getProperty(LASTNAME_FIELD));
		gwtChild.setBirthDay((Date) child.getProperty(BIRTHDAY_FIELD));
		gwtChild.setDevelopementDialogueDates((List<Date>) child.getProperty(DEVELOPEMENT_DIALOGUE_DATES));
		return gwtChild;
	}

	private Entity toEntity(GwtChild gwtChild) {
		final String key = gwtChild.getKey();
		final Entity child;
		if (key == null) {
			child = new Entity(CHILD_KIND);
		} else {
			child = new Entity(toKey(key));
		}
		child.setProperty(FIRSTNAME_FIELD, gwtChild.getFirstName());
		child.setProperty(LASTNAME_FIELD, gwtChild.getLastName());
		child.setProperty(BIRTHDAY_FIELD, gwtChild.getBirthDay());
		List<Date> developementDialogueDates = gwtChild
				.getDevelopementDialogueDates();
		if (developementDialogueDates != null
				&& !developementDialogueDates.isEmpty()) {
			Collections.sort(developementDialogueDates);
			child.setProperty(DEVELOPEMENT_DIALOGUE_DATES, developementDialogueDates);
			child.setProperty(LAST_DEVELOPEMENT_DIALOGUE_DATE, developementDialogueDates.get(developementDialogueDates.size() - 1));
		}
		return child;
	}

	public static String formatChildName(Entity child) {
		return child.getProperty(FIRSTNAME_FIELD) + " "
				+ child.getProperty(LASTNAME_FIELD);
	}

	public Collection<GwtChild> getAllChildrenOver12() {
		Collection<GwtChild> result = new ArrayList<GwtChild>();
		Date twelveYearsAgo = getDateTwelveYearsAgo();
		for(GwtChild child : getAllChildren()) {
			Date birthDay = child.getBirthDay();
			if(birthDay != null && !birthDay.after(twelveYearsAgo)) {
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
		for(GwtChild child : getAllChildren()) {
			Date birthDay = child.getBirthDay();
			if(birthDay != null && birthDay.after(twelveYearsAgo)) {
				result.add(child);
			}
		}
		return result;
	}

}
