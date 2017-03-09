package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.converter.GwtSectionConverter.getEntityConverter;
import static at.brandl.lws.notice.shared.util.Constants.Section.KIND;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.CacheUtil;
import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.server.dao.ds.converter.GwtSectionConverter;
import at.brandl.lws.notice.server.dao.ds.converter.GwtSectionConverter.KeySelector;
import at.brandl.lws.notice.server.dao.ds.converter.GwtSectionConverter.SectionSelector;
import at.brandl.lws.notice.shared.util.Constants.Section;
import at.brandl.lws.notice.shared.util.Constants.Section.Cache;

public class SectionDsDao extends AbstractDsDao {

	public static final String SEPARATOR = " / ";
	private static final Function<Entity, GwtSection> ENTITY_CONVERTER = getEntityConverter();
	private static final EntityListSupplier<GwtSection> SECTIONLIST_SUPPLIER = new EntityListSupplier<GwtSection>(
			new Query(KIND), ENTITY_CONVERTER);

	private Map<String, List<String>> sectionChildCache;
	private volatile boolean sectionChildCacheUpdateNeeded = true;

	public List<GwtSection> getAllSections() {

		List<GwtSection> sections = CacheUtil.getCached(Cache.ALL_SECTIONS, SECTIONLIST_SUPPLIER, Section.class,
				getCache());
		// System.out.println("all " + sections);
		return sections;
	}

	public GwtSection getSection(String sectionKey) {

		GwtSection section = CacheUtil.getFirstFromCachedList(new KeySelector(sectionKey),
				new EntitySupplier<GwtSection>(DsUtil.toKey(sectionKey), ENTITY_CONVERTER), Cache.ALL_SECTIONS,
				SECTIONLIST_SUPPLIER, Section.class, getCache());
		// System.out.println("key " + sectionKey + ": " + section);
		return section;
	}

	public String getSectionName(String sectionKey) {

		return getSectionName(getSection(sectionKey));
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

		// System.out.println("store " + gwtSection);

		if (gwtSection.getSectionName().contains(SEPARATOR)) {
			throw new IllegalArgumentException("section name may not contain '" + SEPARATOR + "'");
		}

		assertCacheIsLoaded();
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();

		try {

			String keyForUpdate = gwtSection.getKey();
			if (keyForUpdate == null && exists(gwtSection, datastoreService)) {
				transaction.rollback();
				throw new IllegalArgumentException(gwtSection.getSectionName() + " existiert bereits!");

			}

			Entity section = GwtSectionConverter.toEntity(gwtSection);
			datastoreService.put(section);
			transaction.commit();
			sectionChildCacheUpdateNeeded = true;
			gwtSection.setKey(DsUtil.toString(section.getKey()));
			CacheUtil.updateCachedResult(Cache.ALL_SECTIONS, gwtSection, new KeySelector(keyForUpdate), getCache());

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}

	}

	public void deleteSections(Collection<String> sectionKeys) {

		// System.out.println("delete " + sectionKeys);

		if (sectionKeys == null || sectionKeys.isEmpty()) {
			return;
		}

		assertCacheIsLoaded();
		DatastoreService datastoreService = getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();

		try {
			if (getBeobachtungDao().beobachtungenExist(sectionKeys, datastoreService)) {
				throw new IllegalStateException("Es noch Wahrnehmungen in den Bereichen.");
			}

			for (String sectionKey : sectionKeys) {
				datastoreService.delete(DsUtil.toKey(sectionKey));
			}
			transaction.commit();
			sectionChildCacheUpdateNeeded = true;
			for (String sectionKey : sectionKeys) {
				CacheUtil.removeFromCachedResult(Cache.ALL_SECTIONS, new KeySelector(sectionKey), getCache());
			}
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}

	}

	private String getSectionName(GwtSection section) {
		String name = "";
		if (section.getParentKey() != null) {
			name = getSectionName(getSection(section.getParentKey())) + SEPARATOR;
		}
		return name + section.getSectionName();
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

	private void assertCacheIsLoaded() {
		getAllSections();
	}

	private void updateChildKeys() {
		if (sectionChildCacheUpdateNeeded) {
			synchronized (this) {
				if (sectionChildCacheUpdateNeeded) {
					sectionChildCache = createChildMap();
					sectionChildCacheUpdateNeeded = false;
				}
			}
		}
	}

	private boolean exists(GwtSection gwtSection, DatastoreService datastoreService) {
		return CacheUtil.getFirstFromCachedList(new SectionSelector(gwtSection), null, Cache.ALL_SECTIONS,
				SECTIONLIST_SUPPLIER, Section.class, getCache()) != null;
	}

	private BeobachtungDsDao getBeobachtungDao() {
		return DaoRegistry.get(BeobachtungDsDao.class);
	}

	private Map<String, List<String>> createChildMap() {
		Map<String, List<GwtSection>> relations = new HashMap<>();
		Map<String, GwtSection> sections = new HashMap<>();
		List<GwtSection> allSections = getAllSections();
		for (GwtSection section : allSections) {
			sections.put(section.getKey(), section);
			List<GwtSection> siblings = relations.get(section.getParentKey());
			if (siblings == null) {
				siblings = new ArrayList<>();
				relations.put(section.getParentKey(), siblings);
			}
			siblings.add(section);
		}

		Map<String, List<String>> deepRelations = new HashMap<>();
		for (Entry<String, List<GwtSection>> entry : relations.entrySet()) {
			List<String> tree = deepRelations.get(entry.getKey());
			if (tree == null) {
				tree = new ArrayList<>();
				deepRelations.put(entry.getKey(), tree);
			}
			addAll(relations, tree, entry.getValue());
		}
		return deepRelations;
	}

	private void addAll(Map<String, List<GwtSection>> relations, List<String> tree, List<GwtSection> sections) {
		if (sections == null || sections.isEmpty()) {
			return;
		}
		for (GwtSection section : sections) {
			tree.add(section.getKey());
			addAll(relations, tree, relations.get(section.getKey()));
		}
	}

}
