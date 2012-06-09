package at.lws.wnm.server.dao;

import javax.persistence.EntityManager;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.datastore.Text;

public class BeobachtungDao {

	public void storeBeobachtung(GwtBeobachtung gwtBeobachtung) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Beobachtung beobachtung = new Beobachtung();
			beobachtung.setChildKey(gwtBeobachtung.getChildKey());
			beobachtung.setSectionKey(gwtBeobachtung.getSectionKey());
			beobachtung.setDate(gwtBeobachtung.getDate());
			beobachtung.setText(new Text(gwtBeobachtung.getText()));
			beobachtung.setKey(gwtBeobachtung.getKey());
			em.persist(beobachtung);
		} finally {
			em.close();
		}
	}
	
}
