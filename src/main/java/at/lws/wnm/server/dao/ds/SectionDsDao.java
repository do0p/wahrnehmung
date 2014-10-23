package at.lws.wnm.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.GwtSection;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Transaction;

public class SectionDsDao extends AbstractDsDao {

	public static final String SECTION_KIND = "SectionDs";
	public static final String SECTION_NAME_FIELD = "sectionName";
	public static final String SECTION_MEMCACHE = "section";
	
	private Map<String, List<String>> sectionChildCache;
	private volatile boolean sectionChildCacheUpdateNeeded = true;

	public GwtSection getSection(String sectionKey) {
		final Entity section = getCachedEntity(toKey(sectionKey));
		return toGwt(section);
	}
	
	public String getSectionName(String sectionKey) {
		final Entity section = getCachedEntity(toKey(sectionKey));
		return (String) section.getProperty(SECTION_NAME_FIELD);
	}

	public List<GwtSection> getAllSections() {
		final Query query = new Query(SECTION_KIND);
		final Iterable<Entity> result = execute(query, withDefaults());
		return mapToGwtSections(result);
	}

	public Set<String> getAllChildKeys(String key) {
		updateChildKeys();
		final List<String> children = sectionChildCache.get(key);
		if (children == null) {
			return new HashSet<String>();
		}
		return new HashSet<String>(children);
	}

	public void storeSection(GwtSection gwtSection) {

		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {

			final Entity section = toEntity(gwtSection);

			if (!section.getKey().isComplete()) {
				if (exists(section, datastoreService)) {
					transaction.rollback();
					throw new IllegalArgumentException(
							section.getProperty(SECTION_NAME_FIELD)
									+ " existiert bereits!");
				}
			}

			datastoreService.put(section);
			transaction.commit();
			sectionChildCacheUpdateNeeded = true;
			gwtSection.setKey(toString(section.getKey()));
			insertIntoCache(section);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void deleteSections(Collection<String> sectionKeys) {
		if (sectionKeys == null || sectionKeys.isEmpty()) {
			return;
		}

		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {
			if (getBeobachtungDao().beobachtungenExist(sectionKeys, datastoreService)) {
				throw new IllegalStateException(
						"Es noch Wahrnehmungen in den Bereichen.");
			}
			for(String key : sectionKeys)
			{
				deleteEntity(toKey(key), datastoreService);
			}
			transaction.commit();
			sectionChildCacheUpdateNeeded = true;
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	@Override
	protected String getMemcacheServiceName() {
		return SECTION_MEMCACHE;
	}

	private void updateChildKeys() {
		if (sectionChildCacheUpdateNeeded) {
			synchronized (this) {
				if (sectionChildCacheUpdateNeeded) {
					final Map<String, List<String>> tmpChildMap = new HashMap<String, List<String>>();

					addChildKeys(null, tmpChildMap);
					sectionChildCache = tmpChildMap;
					sectionChildCacheUpdateNeeded = false;
				}
			}
		}
	}

	private List<String> addChildKeys(Key parentKey,
			Map<String, List<String>> result) {
		final List<String> childKeys = new ArrayList<String>();
		result.put(parentKey == null ? null : toString(parentKey), childKeys);
		for (Entity section : getAllSectionsInternal(parentKey)) {
			childKeys.add(toString(section.getKey()));
			childKeys.addAll(addChildKeys(section.getKey(), result));
		}
		return childKeys;
	}

	private Iterable<Entity> getAllSectionsInternal(Key parentKey) {
		final Query query = new Query(SECTION_KIND, parentKey).setKeysOnly();
		return removeKey(execute(query, withDefaults()), parentKey);
	}

	private Iterable<Entity> removeKey(Iterable<Entity> entities, Key parentKey) {
		final Collection<Entity> result = new LinkedList<Entity>();
		for(Entity entity : entities)
		{
			if(!entity.getKey().equals(parentKey))
			{
				result.add(entity);
			}
		}
		return result;
	}

	private List<GwtSection> mapToGwtSections(Iterable<Entity> entities) {
		final List<GwtSection> result = new ArrayList<GwtSection>();
		for (Entity section : entities) {
			result.add(toGwt(section));
		}

		return result;
	}

	private boolean exists(Entity section, DatastoreService datastoreService) {
		final Filter filter = createEqualsPredicate(SECTION_NAME_FIELD, section);
		final Query query = new Query(SECTION_KIND).setFilter(filter);
		return count(query, withDefaults(), datastoreService) > 0;
	}

	private Entity toEntity(GwtSection gwtSection) {
		final String key = gwtSection.getKey();
		final Entity entity;
		if (key == null) {
			final String parentKey = gwtSection.getParentKey();
			if (parentKey == null) {
				entity = new Entity(SECTION_KIND);
			} else {
				entity = new Entity(SECTION_KIND, toKey(parentKey));
			}
		} else {
			entity = new Entity(toKey(key));
		}
		entity.setProperty(SECTION_NAME_FIELD, gwtSection.getSectionName());
		return entity;
	}

	private GwtSection toGwt(Entity entity) {
		final GwtSection section = new GwtSection();
		section.setKey(toString(entity.getKey()));
		section.setSectionName((String) entity.getProperty(SECTION_NAME_FIELD));
		final Key parentKey = entity.getParent();
		if (parentKey != null) {
			section.setParentKey(toString(parentKey));
		}
		return section;
	}

	private BeobachtungDsDao getBeobachtungDao() {
		return DaoRegistry.get(BeobachtungDsDao.class);
	}

}