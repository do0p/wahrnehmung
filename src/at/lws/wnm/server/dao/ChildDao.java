package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Child;
import at.lws.wnm.shared.model.GwtChild;

public class ChildDao extends AbstractDao{

	ChildDao() {
	}
	
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
		for (Child child : resultList) {
			result.add(child.toGwt());
		}
		return result;
	}

	public void storeChild(GwtChild gwtChild) throws IllegalArgumentException {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			if (gwtChild.getKey() == null) {
				final Query query = em
						.createQuery("select from "
								+ Child.class.getName()
								+ " c where c.firstName = :firstName and c.lastName = :lastName and c.birthDay = :birthDay");
				query.setParameter("firstName", gwtChild.getFirstName());
				query.setParameter("lastName", gwtChild.getLastName());
				query.setParameter("birthDay", gwtChild.getBirthDay());
				if (!query.getResultList().isEmpty()) {
					throw new IllegalArgumentException(gwtChild.getFirstName() + " " + gwtChild.getLastName() + " existiert bereits!");
				}
			}
			em.persist(Child.valueOf(gwtChild));
		} finally {
			em.close();
		}
	}

	public void deleteChild(GwtChild child) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("delete from Child c where c.key = :key");
				query.setParameter("key", child.getKey());
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

	public GwtChild getChild(Long key) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Child child = em.find(Child.class, key);
			if(child == null)
			{
				throw new IllegalArgumentException("no child with key " + key);
			}
			return child.toGwt();
		} finally {
			em.close();
		}
	}
}
