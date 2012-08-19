package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.shared.model.Authorization;

import com.google.appengine.api.users.User;

public class AuthorizationDao extends AbstractDao{

	private static final Set<String> SUPER_USER_IDS = new HashSet<String>();
	static {
		SUPER_USER_IDS.add("dbrandl72@gmail.com");
	}
	
	AuthorizationDao()
	{
		
	}
	
	public Authorization getAuthorization(User user)
	{
		final String lowerUserEmail = user.getEmail().toLowerCase();
		if(SUPER_USER_IDS.contains(lowerUserEmail))
		{
			return createSuperUser(lowerUserEmail);
		}
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return em.find(Authorization.class, lowerUserEmail);
		} finally {
			em.close();
		}
	}
	
	public boolean isAuthorized(User user) {
		final String lowerUserEmail = user.getEmail().toLowerCase();
		if(SUPER_USER_IDS.contains(lowerUserEmail))
		{
			return true;
		}
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return em.find(Authorization.class, lowerUserEmail) != null;
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


	private Authorization createSuperUser(String email) {
		final Authorization superUser = new Authorization();
		superUser.setUserId(email);
		superUser.setEmail(email);
		superUser.setAdmin(true);
		superUser.setSeeAll(false);
		return superUser;
	}

	
}
