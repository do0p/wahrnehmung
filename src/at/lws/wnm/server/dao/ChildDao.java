package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import at.lws.wnm.server.model.Child;
import at.lws.wnm.shared.model.GwtChild;

public class ChildDao extends AbstractDao {

	private final MemcacheService cache = MemcacheServiceFactory
			.getMemcacheService("childDao");

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
		Collections.sort(result);
		return result;
	}

	public void storeChild(GwtChild gwtChild) throws IllegalArgumentException {
		final Child child = Child.valueOf(gwtChild);
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
					throw new IllegalArgumentException(gwtChild.getFirstName()
							+ " " + gwtChild.getLastName()
							+ " existiert bereits!");
				}
			}
			em.persist(child);
		} finally {
			em.close();
		}
		cache.put(child.getKey(), child);
	}

	public void deleteChild(GwtChild child) {
		cache.delete(child.getKey());
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em
					.createQuery("delete from Child c where c.key = :key");
			query.setParameter("key", child.getKey());
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

	public GwtChild getChild(Long key) {
		Child child = (Child) cache.get(key);
		if (child == null) {
			final EntityManager em = EMF.get().createEntityManager();
			try {
				child = getChildInternal(key, em);
			} finally {
				em.close();
			}
		}
		return child.toGwt();
	}

	public String getChildName(Long childKey, EntityManager em) {
		Child child = (Child) cache.get(childKey);
		if (child == null) {
			child = getChildInternal(childKey, em);
		}
		return child.getFirstName() + " " + child.getLastName();
	}

	private Child getChildInternal(Long key, final EntityManager em) {
		final Child child = em.find(Child.class, key);
		if (child == null) {
			throw new IllegalArgumentException("no child with key " + key);
		}
		return child;
	}
}
