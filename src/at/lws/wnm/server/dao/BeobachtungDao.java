package at.lws.wnm.server.dao;

import javax.persistence.EntityManager;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.datastore.Text;

public class BeobachtungDao {

	public void storeBeobachtung(GwtBeobachtung gwtBeobachtung) {
		final EntityManager em = EMF.get().createEntityManager();
		try {

			em.persist(Beobachtung.valueOf(gwtBeobachtung));
		} finally {
			em.close();
		}
	}
	
}
