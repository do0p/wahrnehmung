package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Section;

public class SectionDao {

	@SuppressWarnings("unchecked")
	public List<String> getAllSections() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select s.sectionName from "
					+ Section.class.getName() + " s");
			return new ArrayList<String>(query.getResultList());
		} finally {
			em.close();
		}
	}
}
