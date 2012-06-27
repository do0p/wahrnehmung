package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.server.model.Child;
import at.lws.wnm.server.model.Section;
import at.lws.wnm.shared.model.GwtBeobachtung;

public class BeobachtungDao {

	@SuppressWarnings("unchecked")
	public List<GwtBeobachtung> getBeobachtungen(Long childNo, Long sectionNo) {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select b from Beobachtung b");
		final boolean childKeyExists = childNo != null;
		final boolean sectionKeyExists = sectionNo != null;
		if (childKeyExists || sectionKeyExists) {
			queryBuilder.append(" where");
			if (childKeyExists) {
				queryBuilder.append(" b.childKey = :childKey");
				if (sectionKeyExists) {
					queryBuilder.append(" and");
				}
			}
			if (sectionKeyExists) {
				queryBuilder.append(" b.sectionKey = :sectionKey");
			}
		}
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());
			if (childKeyExists) {
				query.setParameter("childKey", childNo);
			}
			if (sectionKeyExists) {
				query.setParameter("sectionKey", sectionNo);
			}
			return mapToGwtBeobachtung(query.getResultList(), em);
		} finally {
			em.close();
		}
	}

	private List<GwtBeobachtung> mapToGwtBeobachtung(
			List<Beobachtung> resultList, EntityManager em) {
		final List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();
		for (Beobachtung beobachtung : resultList) {
			final GwtBeobachtung gwtBeobachtung = beobachtung.toGwt();
			gwtBeobachtung.setText("");
			gwtBeobachtung.setChildName(getChildName(beobachtung.getChildKey(),
					em));
			gwtBeobachtung.setSectionName(getSectionName(
					beobachtung.getSectionKey(), em));
			result.add(gwtBeobachtung);
		}
		return result;
	}

	private String getSectionName(Long sectionKey, EntityManager em) {
		final Section section = em.find(Section.class, sectionKey);
		return section.getSectionName();
	}

	private String getChildName(Long childKey, EntityManager em) {
		final Child child = em.find(Child.class, childKey);
		return child.getFirstName() + " " + child.getLastName();
	}

	public void storeBeobachtung(GwtBeobachtung gwtBeobachtung) {
		final EntityManager em = EMF.get().createEntityManager();
		try {

			em.persist(Beobachtung.valueOf(gwtBeobachtung));
		} finally {
			em.close();
		}
	}

	public GwtBeobachtung getBeobachtung(Long beobachtungsKey) {
		final EntityManager em = EMF.get().createEntityManager();
		try {

			return em.find(Beobachtung.class, beobachtungsKey).toGwt();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<GwtBeobachtung> getBeobachtungen(List<Long> sectionNos) {
		if(sectionNos == null || sectionNos.isEmpty())
		{
			return new ArrayList<GwtBeobachtung>();
		}
		
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select b from Beobachtung b where b.sectionKey in ( ?");
		for(int i = 1; i < sectionNos.size(); i++)
		{
			queryBuilder.append(i);
			queryBuilder.append(", ?");
		}
		queryBuilder.append(sectionNos.size());
		queryBuilder.append(" )");
		
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());
			for(int i = 0; i <sectionNos.size(); i++)
			{
				query.setParameter(i + 1, sectionNos.get(i));
				
			}
			return mapToGwtBeobachtung(query.getResultList(), em);
		} finally {
			em.close();
		}
	}

	public void deleteAllFromChild(Long key) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery("delete from Beobachtung b where b.childKey = :childKey");
				query.setParameter("childKey", key);
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

}
