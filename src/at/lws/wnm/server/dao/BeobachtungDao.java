package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.server.model.Child;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.users.User;
import com.google.gwt.view.client.Range;

public class BeobachtungDao extends AbstractDao {

	BeobachtungDao() {

	}

	@SuppressWarnings("unchecked")
	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range) {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select b from Beobachtung b");
		queryBuilder.append(createWhereQuery(filter));
		queryBuilder.append(" order by b.date desc");

		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());
			query.setFirstResult(range.getStart());
			query.setMaxResults(range.getLength());

			return mapToGwtBeobachtung(query.getResultList(), em);
		} finally {
			em.close();
		}
	}

	public int getRowCount(BeobachtungsFilter filter) {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select count(b) from Beobachtung b");
		queryBuilder.append(createWhereQuery(filter));

		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());

			return ((Integer) query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	private String createWhereQuery(BeobachtungsFilter filter) {
		final List<String> subQueries = new ArrayList<String>();

		if (filter.getChildKey() != null) {
			subQueries.add("b.childKey = " + filter.getChildKey());
		}

		final Long sectionKey = filter.getSectionKey();
		if (sectionKey != null) {
			final StringBuilder query = new StringBuilder();
			query.append(" b.sectionKey in ( ").append(sectionKey);
			for (Long subSectionKey : getSectionDao().getAllChildKeys(
					sectionKey)) {
				query.append(", ").append(subSectionKey);
			}
			query.append(" )");
			subQueries.add(query.toString());
		}

		if (subQueries.size() > 0) {
			return " where " + AbstractDao.join(subQueries, " and ");
		}
		return "";
	}

	private List<GwtBeobachtung> mapToGwtBeobachtung(
			List<Beobachtung> resultList, EntityManager em) {
		final List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();
		for (Beobachtung beobachtung : resultList) {
			final GwtBeobachtung gwtBeobachtung = beobachtung.toGwt();
			// gwtBeobachtung.setText("");
			gwtBeobachtung.setChildName(getChildName(beobachtung.getChildKey(),
					em));
			gwtBeobachtung.setSectionName(getSectionDao().getSectionName(
					beobachtung.getSectionKey(), em));
			result.add(gwtBeobachtung);
		}
		return result;
	}

	private String getChildName(Long childKey, EntityManager em) {
		final Child child = em.find(Child.class, childKey);
		return child.getFirstName() + " " + child.getLastName();
	}

	public void storeBeobachtung(GwtBeobachtung gwtBeobachtung, User user) {
		final EntityManager em = EMF.get().createEntityManager();
		try {

			final Beobachtung beobachtung = Beobachtung.valueOf(gwtBeobachtung);
			beobachtung.setUser(user);
			em.persist(beobachtung);
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

	public List<GwtBeobachtung> getBeobachtungen(List<Long> sectionNos) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return getBeobachtungen(sectionNos, em);
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<GwtBeobachtung> getBeobachtungen(List<Long> sectionNos,
			final EntityManager em) {
		if (sectionNos == null || sectionNos.isEmpty()) {
			return new ArrayList<GwtBeobachtung>();
		}

		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select b from Beobachtung b where ");
		queryBuilder.append("b.sectionKey in ( ?");
		for (int i = 1; i < sectionNos.size(); i++) {
			queryBuilder.append(i);
			queryBuilder.append(", ?");
		}
		queryBuilder.append(sectionNos.size());
		queryBuilder.append(" )");

		final Query query = em.createQuery(queryBuilder.toString());
		for (int i = 0; i < sectionNos.size(); i++) {
			query.setParameter(i + 1, sectionNos.get(i));

		}
		return mapToGwtBeobachtung(query.getResultList(), em);
	}

	public void deleteAllFromChild(Long key) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em
					.createQuery("delete from Beobachtung b where b.childKey = :childKey");
			query.setParameter("childKey", key);
			query.executeUpdate();
		} finally {
			em.close();
		}
	}

	private SectionDao getSectionDao() {
		return DaoRegistry.get(SectionDao.class);
	}

}
