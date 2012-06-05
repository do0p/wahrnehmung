package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Child;

public class ChildDao {

	@SuppressWarnings("unchecked")
	public List<Child> getAllChildren() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from "
					+ Child.class.getName());
			return new ArrayList<Child>(query.getResultList());
		} finally {
			em.close();
		}
	}
}
