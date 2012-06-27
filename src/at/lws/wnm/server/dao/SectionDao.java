package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Section;
import at.lws.wnm.shared.model.GwtSection;

public class SectionDao {


	private Map<Long, List<Long>> buildChildKeys() {
		final Map<Long, List<Long>> result = new HashMap<Long, List<Long>>();
		final EntityManager em = EMF.get().createEntityManager();
		try {
			addChildKeys(null, result, em);
		} finally {
			em.close();
		}
		return result;
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

	public List<GwtSection> getAllSections(Long parentKey) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return mapToGwtSections(getAllSectionsInternal(parentKey, em));
		} finally {
			em.close();
		}
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
			em.persist(Section.valueOf(gwtSection));
		} finally {
			em.close();
		}
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

	public List<Long> getAllChildKeys(Long key) {
		final List<Long> children = buildChildKeys().get(key);
		if(children == null)
		{
			return new ArrayList<Long>();
		}
		return new ArrayList<Long>(children);
	}

	public void deleteSections(List<Long> sectionNos) {
		if (sectionNos == null || sectionNos.isEmpty()) {
			return;
		}

		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("delete from Section s where s.key in ( ?");
		for (int i = 1; i < sectionNos.size(); i++) {
			queryBuilder.append(i);
			queryBuilder.append(", ?");
		}
		queryBuilder.append(sectionNos.size());
		queryBuilder.append(")");

		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());
			for (int i = 0; i < sectionNos.size(); i++) {
				query.setParameter(i + 1, sectionNos.get(i));
			}
			query.executeUpdate();
		} finally {
			em.close();
		}
	}
}
