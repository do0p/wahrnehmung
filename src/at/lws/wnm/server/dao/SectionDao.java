package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Section;

public class SectionDao {

	@SuppressWarnings("unchecked")
	public List<Section> getAllSections() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from "
					+ Section.class.getName() );
			return new ArrayList<Section>(query.getResultList());
		} finally {
			em.close();
		}
	}
	
	public void storeSection(Section section) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(section);
		} finally {
			em.close();
		}
	}
}
