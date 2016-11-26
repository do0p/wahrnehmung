package at.brandl.lws.notice.interaction.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Interaction;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

public class InteractionDsDao extends AbstractDsDao {

	public Map<String, Integer> getInteractions(String key) {
		
		DatastoreService datastoreService = getDatastoreService();
		Query query = new Query(DsUtil.toKey(key));
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		
		HashMap<String, Integer> result = new HashMap<>();
		for(Entity interaction : preparedQuery.asIterable()) {
			result.put(DsUtil.toString((Key) interaction.getProperty(Interaction.PARTNER)), (Integer) interaction.getProperty(Interaction.COUNT));
		}
		return result;
	}

	public void storeInteraction(String key, String other, Integer count,
			Date date) {

		final DatastoreService datastoreService = getDatastoreService();
		final Transaction transaction = datastoreService.beginTransaction();
		try {

			final Entity interaction = toEntity(key, other, count, date);

//			if (exists(interaction, datastoreService)) {
//				transaction.rollback();
//				throw new IllegalArgumentException(interaction
//						+ " existiert bereits!");
//			}

			datastoreService.put(interaction);
			transaction.commit();

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private Entity toEntity(String key, String other, Integer count, Date date) {
		Entity entity = new Entity(Interaction.KIND, DsUtil.toKey(key));
		entity.setProperty(Interaction.PARTNER, DsUtil.toKey(other));
		entity.setProperty(Interaction.COUNT, count);
		entity.setProperty(Interaction.DATE, date);
		return entity;
	}

}
