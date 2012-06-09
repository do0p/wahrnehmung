package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Child;
import at.lws.wnm.shared.model.GwtChild;

public class ChildDao {

	@SuppressWarnings("unchecked")
	public List<GwtChild> getAllChildren() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from "
					+ Child.class.getName());
			return mapToGwtChildren(query.getResultList());
		} finally {
			em.close();
		}
	}

	private List<GwtChild> mapToGwtChildren(List<Child> resultList) {
		final List<GwtChild> result = new ArrayList<GwtChild>();
		for(Child child : resultList)
		{
			result.add(child.toGwt());
		}
		return result;
	}

	public void storeChild(GwtChild gwtChild) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(Child.valueOf(gwtChild));
		} finally {
			em.close();
		}
	}


}
