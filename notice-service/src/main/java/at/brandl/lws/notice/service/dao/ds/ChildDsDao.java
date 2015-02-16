package at.brandl.lws.notice.service.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.model.GwtChild;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

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

		if (dirty) {
			allChildren = updateCacheFromDatastore();
		} else {
			allChildren = (List<GwtChild>) getCache(CHILD_DAO_MEMCACHE).get(
					ALL_CHILDREN);
			if (allChildren == null) {
				allChildren = updateCacheFromDatastore();
			}
		}

		return allChildren;
	}

	private List<GwtChild> updateCacheFromDatastore() {
		List<GwtChild> allChildren = getAllChildrenFromDatastore();
		getCache(CHILD_DAO_MEMCACHE).put(ALL_CHILDREN, allChildren);
		dirty = false;
		return allChildren;
	}

	private List<GwtChild> getAllChildrenFromDatastore() {
		final Query query = new Query(CHILD_KIND).addSort(LASTNAME_FIELD)
				.addSort(FIRSTNAME_FIELD).addSort(BIRTHDAY_FIELD);
		return mapToGwtChildren(execute(query, withDefaults()));
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
		gwtChild.setDevelopementDialogueDates((List<Date>) child
				.getProperty(DEVELOPEMENT_DIALOGUE_DATES));
		return gwtChild;
	}

}
