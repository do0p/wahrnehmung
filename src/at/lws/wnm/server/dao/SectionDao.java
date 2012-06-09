package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Section;
import at.lws.wnm.shared.model.GwtSection;

public class SectionDao {

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

	public void storeSection(GwtSection gwtSection) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(Section.valueOf(gwtSection));
		} finally {
			em.close();
		}
	}

	private List<GwtSection> mapToGwtSections(List<Section> resultList) {
		final List<GwtSection> result = new ArrayList<GwtSection>();
		for (Section section : resultList) {
			result.add(section.toGwt());
		}
		return result;
	}

}
