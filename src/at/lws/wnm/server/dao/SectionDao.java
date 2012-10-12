package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import at.lws.wnm.server.model.Section;
import at.lws.wnm.shared.model.GwtSection;

public class SectionDao extends AbstractDao {

	private static final int FIVE_MINUTES = 300;
	private Map<Long, List<Long>> sectionChildCache;
	private volatile boolean sectionChildCacheUpdateNeeded = true;
	private final MemcacheService syncCache;

	SectionDao() {
		syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
	}

	public String getSectionName(Long sectionKey, final EntityManager em) {

		String name = (String) syncCache.get(sectionKey); 
		if (name == null) {
		
			try {

				final Section section = em.find(Section.class, sectionKey);
				name = section.getSectionName();
			} finally {
				em.close();
			}
			syncCache.put(sectionKey, name, Expiration.byDeltaSeconds(FIVE_MINUTES)); 
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	public List<GwtSection> getAllSections() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from "
					+ Section.class.getName());
			return mapToGwtSections(query.getResultList());
		} finally {
			em.close();
		}
	}

	public List<Long> getAllChildKeys(Long key) {
		updateChildKeys();
		final List<Long> children = sectionChildCache.get(key);
		if (children == null) {
			return new ArrayList<Long>();
		}
		return new ArrayList<Long>(children);
	}

	public void storeSection(GwtSection gwtSection) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			if (gwtSection.getKey() == null) {
				final Query query = em.createQuery("select from "
						+ Section.class.getName()
						+ " s where s.sectionName = :sectionName");
				query.setParameter("sectionName", gwtSection.getSectionName());
				if (!query.getResultList().isEmpty()) {
					throw new IllegalArgumentException(
							gwtSection.getSectionName() + " existiert bereits!");
				}
			}
			synchronized (this) {
				em.persist(Section.valueOf(gwtSection));
				sectionChildCacheUpdateNeeded = true;
			}
		} finally {
			em.close();
		}
	}

	public void deleteSections(List<Long> sectionNos) {
		if (sectionNos == null || sectionNos.isEmpty()) {
			return;
		}
		final EntityManager em = EMF.get().createEntityManager();
		try {
			if (!getBeobachtungDao().getBeobachtungen(sectionNos, em).isEmpty()) {
				throw new IllegalStateException(
						"Es noch Wahrnehmungen in den Bereichen.");
			}

			final StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("delete from Section s where s.key in ( ?");
			for (int i = 1; i < sectionNos.size(); i++) {
				queryBuilder.append(i);
				queryBuilder.append(", ?");
			}
			queryBuilder.append(sectionNos.size());
			queryBuilder.append(")");

			final Query query = em.createQuery(queryBuilder.toString());
			for (int i = 0; i < sectionNos.size(); i++) {
				query.setParameter(i + 1, sectionNos.get(i));
			}
			synchronized (this) {
				query.executeUpdate();
				sectionChildCacheUpdateNeeded = true;
			}
		} finally {
			em.close();
		}
	}

	private void updateChildKeys() {
		if (sectionChildCacheUpdateNeeded) {
			synchronized (this) {
				if (sectionChildCacheUpdateNeeded) {
					final Map<Long, List<Long>> tmpChildMap = new HashMap<Long, List<Long>>();
					final EntityManager em = EMF.get().createEntityManager();
					try {
						addChildKeys(null, tmpChildMap, em);
					} finally {
						em.close();
					}
					sectionChildCache = tmpChildMap;
					sectionChildCacheUpdateNeeded = false;
				}
			}
		}
	}

	private List<Long> addChildKeys(Long parentKey,
			Map<Long, List<Long>> result, EntityManager em) {
		final List<Long> childKeys = new ArrayList<Long>();
		result.put(parentKey, childKeys);
		for (Section section : getAllSectionsInternal(parentKey, em)) {
			childKeys.add(section.getKey());
			childKeys.addAll(addChildKeys(section.getKey(), result, em));
		}
		return childKeys;
	}

	@SuppressWarnings("unchecked")
	private List<Section> getAllSectionsInternal(Long parentKey,
			EntityManager em) {

		final Query query = em
				.createQuery("select from " + Section.class.getName()
						+ " s where s.parentKey = :parentKey");
		query.setParameter("parentKey", parentKey);
		return query.getResultList();

	}

	private List<GwtSection> mapToGwtSections(List<Section> resultList) {
		final List<GwtSection> result = new ArrayList<GwtSection>();
		if (resultList != null) {
			for (Section section : resultList) {
				result.add(section.toGwt());
			}
		}
		return result;
	}

	private BeobachtungDao getBeobachtungDao() {
		return DaoRegistry.get(BeobachtungDao.class);
	}

}
