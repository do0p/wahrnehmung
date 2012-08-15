package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Authorization;

public class AuthorizationDao extends AbstractDao{

	AuthorizationDao()
	{
		
	}
	
	public boolean isAuthorized(String user) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return em.find(Authorization.class, user.toLowerCase()) != null;
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Authorization> queryAuthorizations() {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("select from Authorization");
			return new ArrayList<Authorization>(query.getResultList());
		} finally {
			em.close();
		}
	}

	public void storeAuthorization(Authorization aut) {
		aut.setUserId(aut.getEmail().toLowerCase());
		final EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(aut);
		} finally {
			em.close();
		}
	}

	public void deleteAuthorization(String email) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("delete from Authorization a where a.userId = :userId");
			query.setParameter("userId", email.toLowerCase());
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

}
