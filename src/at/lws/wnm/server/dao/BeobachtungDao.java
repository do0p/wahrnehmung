package at.lws.wnm.server.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import at.lws.wnm.server.model.Beobachtung;
import at.lws.wnm.server.model.BeobachtungGroup;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.gwt.view.client.Range;

public class BeobachtungDao extends AbstractDao {

	private static final int FIVE_MINUTES = 300;
	private final MemcacheService cache = MemcacheServiceFactory
			.getMemcacheService("beobachtungsDao");

	BeobachtungDao() {

	}

	public List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range, User user) {
		final EntityManager em = EMF.get().createEntityManager();
		try {
			return getBeobachtungen(filter, range, user, em);
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	List<GwtBeobachtung> getBeobachtungen(BeobachtungsFilter filter,
			Range range, User user, EntityManager em) {
		final List<Long> childKeys = getSectionDao().getAllChildKeys(
				filter.getSectionKey());
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select b from Beobachtung b");
		queryBuilder.append(createWhereQuery(filter, user, childKeys));
		queryBuilder.append(" order by b.date desc");

		final Query query = em.createQuery(queryBuilder.toString());
		addParameter(query, filter, user, childKeys);
		query.setFirstResult(range.getStart());
		query.setMaxResults(range.getLength());

		return mapToGwtBeobachtung(query.getResultList(), em);
	}

	public int getRowCount(BeobachtungsFilter filter, User user) {

		final List<Long> childKeys = getSectionDao().getAllChildKeys(
				filter.getSectionKey());
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select count(b) from Beobachtung b");
		queryBuilder.append(createWhereQuery(filter, user, childKeys));

		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Query query = em.createQuery(queryBuilder.toString());
			addParameter(query, filter, user, childKeys);
			return ((Integer) query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public GwtBeobachtung getBeobachtung(Long beobachtungsKey) {
		Beobachtung beobachtung = (Beobachtung) cache.get(beobachtungsKey);
		if (beobachtung == null) {
			final EntityManager em = EMF.get().createEntityManager();
			try {

				beobachtung = em.find(Beobachtung.class, beobachtungsKey);
			} finally {
				em.close();
			}
		}
		return beobachtung.toGwt();
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

	public Long storeBeobachtung(GwtBeobachtung gwtBeobachtung, User user,
			Long masterBeobachtungsKey) {
		final Beobachtung beobachtung = Beobachtung.valueOf(gwtBeobachtung);
		EntityManager em = EMF.get().createEntityManager();
		try {
			beobachtung.setUser(user);
			em.persist(beobachtung);
		} finally {
			em.close();
		}
		cache.put(beobachtung.getKey(), beobachtung,
				Expiration.byDeltaSeconds(FIVE_MINUTES));
		if (masterBeobachtungsKey != null) {
			em = EMF.get().createEntityManager();
			try {
				em.persist(new BeobachtungGroup(masterBeobachtungsKey,
						beobachtung.getKey()));
			} finally {
				em.close();
			}
		}
		return beobachtung.getKey();
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

	private String createWhereQuery(BeobachtungsFilter filter, User user,
			List<Long> childKeys) {
		final List<String> subQueries = new ArrayList<String>();

		if (filter.getChildKey() != null) {
			subQueries.add(" b.childKey = :childKey");
		}

		final Long sectionKey = filter.getSectionKey();
		if (sectionKey != null) {
			final StringBuilder query = new StringBuilder();
			query.append(" b.sectionKey in ( :sectionKey0");
			for (int i = 1; i <= childKeys.size(); i++) {
				query.append(", :sectionKey" + i);
			}
			query.append(" )");
			subQueries.add(query.toString());
		}

		if (user != null) {
			subQueries.add(" b.user = :user");
		}

		if (subQueries.size() > 0) {
			return " where" + AbstractDao.join(subQueries, " and");
		}
		return "";
	}

	private void addParameter(Query query, BeobachtungsFilter filter,
			User user, List<Long> childKeys) {
		if (filter.getChildKey() != null) {
			query.setParameter("childKey", filter.getChildKey());
		}

		final Long sectionKey = filter.getSectionKey();
		if (sectionKey != null) {
			query.setParameter("sectionKey0", filter.getSectionKey());
			for (int i = 1; i <= childKeys.size(); i++) {
				query.setParameter("sectionKey" + i, childKeys.get(i - 1));
			}
		}

		if (user != null) {
			query.setParameter("user", user);
		}
	}

	private List<GwtBeobachtung> mapToGwtBeobachtung(
			List<Beobachtung> resultList, EntityManager em) {
		final List<GwtBeobachtung> result = new ArrayList<GwtBeobachtung>();
		for (Beobachtung beobachtung : resultList) {
			final GwtBeobachtung gwtBeobachtung = beobachtung.toGwt();
			// gwtBeobachtung.setText("");
			gwtBeobachtung.setChildName(getChildDao().getChildName(
					beobachtung.getChildKey(), em));
			gwtBeobachtung.setSectionName(getSectionDao().getSectionName(
					beobachtung.getSectionKey(), em));
			result.add(gwtBeobachtung);
		}
		return result;
	}

	private ChildDao getChildDao() {
		return DaoRegistry.get(ChildDao.class);
	}

	private SectionDao getSectionDao() {
		return DaoRegistry.get(SectionDao.class);
	}

	public void deleteBeobachtung(Long beobachtungsKey) {
		cache.delete(beobachtungsKey);
		final EntityManager em = EMF.get().createEntityManager();
		try {
			final Beobachtung beobachtung = em.find(Beobachtung.class,
					beobachtungsKey);
			if (beobachtung != null) {
				em.remove(beobachtung);
			}
		} finally {
			em.close();
		}
	}

}
