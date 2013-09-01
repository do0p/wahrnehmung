package at.lws.wnm.server.service;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.ADMIN_FIELD;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.AUTHORIZATION_KIND;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.EDIT_SECTIONS_FIELD;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.EMAIL_FIELD;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.SEE_ALL_FIELD;
import static at.lws.wnm.server.dao.ds.AuthorizationDsDao.USER_ID_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.BEOBACHTUNGS_GROUP_KIND;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.BEOBACHTUNGS_KEY_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.BEOBACHTUNG_KIND;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.DATE_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.DURATION_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.SECTION_KEY_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.SOCIAL_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.TEXT_FIELD;
import static at.lws.wnm.server.dao.ds.BeobachtungDsDao.USER_FIELD;
import static at.lws.wnm.server.dao.ds.ChildDsDao.BIRTHDAY_FIELD;
import static at.lws.wnm.server.dao.ds.ChildDsDao.CHILD_KIND;
import static at.lws.wnm.server.dao.ds.ChildDsDao.FIRSTNAME_FIELD;
import static at.lws.wnm.server.dao.ds.ChildDsDao.LASTNAME_FIELD;
import static at.lws.wnm.server.dao.ds.SectionDsDao.SECTION_KIND;
import static at.lws.wnm.server.dao.ds.SectionDsDao.SECTION_NAME_FIELD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.lws.wnm.server.dao.ds.ChildDsDao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

public class MigrationService extends HttpServlet {

	private static final long serialVersionUID = -4674341239260156601L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (!"karkerlark".equals(req.getParameter("key"))) {
			return;
		}

		if ("enqueue".equals(req.getParameter("action"))) {
			final Queue queue = QueueFactory.getQueue("migration");
			queue.add(withUrl("/wahrnehmung/migration").param("key",
					"karkerlark"));
		} else {

			final DatastoreService datastoreService = DatastoreServiceFactory
					.getDatastoreService();
			final StringBuilder log = new StringBuilder();

			try {
				mapAuthorization(datastoreService, log);
				final Map<Long, Key> childMap = mapChildren(datastoreService,
						log);
				final Map<Long, Key> sectionMap = mapSections(datastoreService,
						log);
				final Map<Long, Key> beobachtungsMap = mapBeobachtungen(
						datastoreService, log, childMap, sectionMap);
				mapBeobachtungsGroup(datastoreService, log, beobachtungsMap);
			} finally {
				log(log.toString());
			}
		}

	}

	private void mapBeobachtungsGroup(DatastoreService datastoreService,
			StringBuilder log, Map<Long, Key> beobachtungsMap) {
		log.append("Mapping Beobachtungsgroups\n");
		final Query query = new Query("BeobachtungGroup");
		int numEntities = datastoreService.prepare(query).countEntities(
				FetchOptions.Builder.withDefaults());
		final int size = 100;
		int i = 0;
		while (numEntities > 0) {
			for (Entity oldEntity : datastoreService.prepare(query).asIterable(
					withOffset(i * size).limit(size))) {
				final Long oldMasterKey = (Long) oldEntity
						.getProperty("masterBeobachtungsKey");
				final Key masterKey = beobachtungsMap.get(oldMasterKey);
				if (masterKey == null) {
					continue;
				}
				final Long oldBeobachtunsKey = (Long) oldEntity
						.getProperty("beobachtungsKey");
				final Key beobachtungsKey = beobachtungsMap
						.get(oldBeobachtunsKey);
				if (beobachtungsKey == null) {
					continue;
				}
				final Entity newEntity = new Entity(BEOBACHTUNGS_GROUP_KIND,
						masterKey);
				log.append(oldEntity.getKey().getId()).append(" : ")
						.append(oldEntity.getKey()).append(" -> ");
				mapBeobachtungsGroup(oldEntity, newEntity, beobachtungsKey);
				datastoreService.put(newEntity);
				log.append(newEntity.getKey()).append("\n");
			}
			i++;
			numEntities -= size;
		}

	}

	private void mapBeobachtungsGroup(Entity oldEntity, Entity newEntity,
			Key beobachtungsKey) {
		newEntity.setProperty(BEOBACHTUNGS_KEY_FIELD, beobachtungsKey);
	}

	private Map<Long, Key> mapBeobachtungen(DatastoreService datastoreService,
			StringBuilder log, Map<Long, Key> childMap,
			Map<Long, Key> sectionMap) {
		log.append("Mapping Beobachtungen\n");
		final Map<Long, Key> mapping = new HashMap<Long, Key>();
		final Query query = new Query("Beobachtung");
		int numEntities = datastoreService.prepare(query).countEntities(
				FetchOptions.Builder.withDefaults());
		final int size = 100;
		int i = 0;
		while (numEntities > 0) {

			for (Entity oldEntity : datastoreService.prepare(query).asIterable(
					withOffset(i * size).limit(size))) {
				final Long oldChildKey = (Long) oldEntity
						.getProperty("childKey");
				final Key childKey = childMap.get(oldChildKey);
				if (childKey == null) {
					throw new IllegalStateException("no child for key "
							+ oldChildKey);
				}
				final Long oldSectionKey = (Long) oldEntity
						.getProperty("sectionKey");
				final Key sectionKey = sectionMap.get(oldSectionKey);
				if (sectionKey == null) {
					throw new IllegalStateException("no section for key "
							+ oldSectionKey);
				}
				final Entity newEntity = new Entity(BEOBACHTUNG_KIND, childKey);
				log.append(oldEntity.getKey().getId()).append(" : ")
						.append(oldEntity.getKey()).append(" -> ");
				mapBeobachtung(oldEntity, newEntity, sectionKey);
				datastoreService.put(newEntity);
				log.append(newEntity.getKey()).append("\n");
				mapping.put(oldEntity.getKey().getId(), newEntity.getKey());
			}
			i++;
			numEntities -= size;
		}
		return mapping;
	}

	private void mapBeobachtung(Entity oldEntity, Entity newEntity,
			Key sectionKey) {
		newEntity.setProperty(DATE_FIELD, oldEntity.getProperty("date"));
		newEntity.setProperty(SECTION_KEY_FIELD, sectionKey);
		newEntity
				.setProperty(DURATION_FIELD, oldEntity.getProperty("duration"));
		newEntity.setProperty(SOCIAL_FIELD, oldEntity.getProperty("social"));
		newEntity.setProperty(TEXT_FIELD, oldEntity.getProperty("text"));
		newEntity.setProperty(USER_FIELD, oldEntity.getProperty("user"));
	}

	private Map<Long, Key> mapSections(DatastoreService datastoreService,
			StringBuilder log) {
		log.append("Mapping Sections\n");
		final Map<Long, Key> mapping = new HashMap<Long, Key>();
		final Map<Long, Entity> oldEntities = new HashMap<Long, Entity>();
		final Query query = new Query("Section");
		for (Entity oldEntity : datastoreService.prepare(query).asIterable()) {
			oldEntities.put(oldEntity.getKey().getId(), oldEntity);
		}
		for (Entity oldEntity : oldEntities.values()) {
			if (mapping.containsKey(oldEntity.getKey().getId())) {
				continue;
			}
			persistNewSection(oldEntity, datastoreService, log, mapping,
					oldEntities);
		}
		return mapping;
	}

	private void persistNewSection(Entity oldEntity,
			DatastoreService datastoreService, StringBuilder log,
			Map<Long, Key> mapping, Map<Long, Entity> oldEntities) {
		{
			final Long parentKey = (Long) oldEntity.getProperty("parentKey");
			final Entity newEntity;
			if (parentKey != null) {
				if (!mapping.containsKey(parentKey)) {
					final Entity parentEntity = oldEntities.get(parentKey);
					if (parentEntity != null) {
						persistNewSection(parentEntity, datastoreService, log,
								mapping, oldEntities);
					}
				}
				final Key newParentKey = mapping.get(parentKey);
				if (newParentKey != null) {
					newEntity = new Entity(SECTION_KIND, newParentKey);
				} else {
					newEntity = null;
				}
			} else {
				newEntity = new Entity(SECTION_KIND);
			}
			if (newEntity != null) {
				log.append(oldEntity.getProperty("sectionName")).append(" : ")
						.append(oldEntity.getKey().getId()).append(" -> ");
				mapSection(oldEntity, newEntity);
				datastoreService.put(newEntity);
				log.append(newEntity.getKey()).append("\n");
				mapping.put(oldEntity.getKey().getId(), newEntity.getKey());
			}
		}
	}

	private void mapSection(Entity oldEntity, Entity newEntity) {
		newEntity.setProperty(SECTION_NAME_FIELD,
				oldEntity.getProperty("sectionName"));
	}

	private Map<Long, Key> mapChildren(DatastoreService datastoreService,
			StringBuilder log) {
		log.append("Mapping Children\n");
		final Map<Long, Key> mapping = new HashMap<Long, Key>();
		final Query query = new Query("Child");
		for (Entity oldEntity : datastoreService.prepare(query).asIterable()) {

			final Entity newEntity = new Entity(CHILD_KIND);
			mapChild(oldEntity, newEntity);
			log.append(ChildDsDao.formatChildName(newEntity)).append(" : ")
					.append(oldEntity.getKey()).append(" -> ");
			datastoreService.put(newEntity);
			log.append(newEntity.getKey()).append("\n");
			mapping.put(oldEntity.getKey().getId(), newEntity.getKey());
		}
		return mapping;
	}

	private void mapChild(Entity oldEntity, Entity newEntity) {
		newEntity.setProperty(FIRSTNAME_FIELD,
				oldEntity.getProperty("firstName"));
		newEntity
				.setProperty(LASTNAME_FIELD, oldEntity.getProperty("lastName"));
		newEntity
				.setProperty(BIRTHDAY_FIELD, oldEntity.getProperty("birthDay"));
	}

	private void mapAuthorization(final DatastoreService datastoreService,
			final StringBuilder log) {
		log.append("Mapping Authorization\n");
		final Query query = new Query("Authorization");
		for (Entity oldEntity : datastoreService.prepare(query).asIterable()) {
			final String userId = (String) oldEntity.getProperty("email");
			final Entity newEntity = new Entity(KeyFactory.createKey(
					AUTHORIZATION_KIND, userId.toLowerCase()));
			log.append(userId).append(" : ").append(oldEntity.getKey())
					.append(" -> ");
			mapAuthorization(oldEntity, newEntity);
			log.append(newEntity.getKey()).append("\n");
			datastoreService.put(newEntity);
		}
	}

	private void mapAuthorization(Entity oldEntity, Entity newEntity) {
		newEntity.setProperty(USER_ID_FIELD,
				((String) oldEntity.getProperty("email")).toLowerCase());
		newEntity.setProperty(EMAIL_FIELD, oldEntity.getProperty("email"));
		newEntity.setProperty(ADMIN_FIELD, oldEntity.getProperty("admin"));
		newEntity.setProperty(SEE_ALL_FIELD, oldEntity.getProperty("seeAll"));
		newEntity.setProperty(EDIT_SECTIONS_FIELD,
				oldEntity.getProperty("editSections"));
	}

	//
	// private String toString(final Key key) {
	// return KeyFactory.keyToString(key);
	// }
}
