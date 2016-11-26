package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.converter.GwtAnswerConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAnswerConverter.toGwtAnswer;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionnaireAnswersConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionnaireAnswersConverter.toGwtQuestionnaireAnswers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.brandl.lws.notice.dao.AbstractDsDao;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswer;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswers;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireAnswersValidator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;

public class QuestionnaireDsDao extends AbstractDsDao {

	private final Map<String, Boolean> dirtyMap = new HashMap<>();

	public Collection<GwtQuestionnaireAnswers> getAllAnswers(String childKey) {

		@SuppressWarnings("unchecked")
		Collection<GwtQuestionnaireAnswers> result = (Collection<GwtQuestionnaireAnswers>) getCache()
				.get(childKey);
		if (result == null || isDirty(childKey)) {

			result = new ArrayList<GwtQuestionnaireAnswers>();
			DatastoreService ds = getDatastoreService();
			PreparedQuery query = ds.prepare(new Query(
					QuestionnaireAnswers.KIND, DsUtil.toKey(childKey)));
			for (Entity entity : query.asIterable()) {

				result.add(getQuestionnaireAnswers(entity, ds));
			}
			getCache().put(childKey, result);
			dirtyMap.remove(childKey);
		}
		return result;
	}

	public GwtQuestionnaireAnswers storeAnswers(GwtQuestionnaireAnswers answers, User user) {

		if(!GwtQuestionnaireAnswersValidator.valid(answers)) {
			throw new IllegalArgumentException("invalid answers " + answers);
		}
		
		DatastoreService ds = getDatastoreService();

		final Transaction transaction = ds.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {

			String answersKey = answers.getKey();
			if (answersKey == null) {
				storeAnswers(answers, ds);
				answersKey = answers.getKey();
			}

			Iterator<GwtAnswer> iterator = answers.getAnswers().iterator();
			while (iterator.hasNext()) {
				GwtAnswer answer = iterator.next();
				if (answer.isUpdated()) {
					boolean stored = storeAnswer(answer, answersKey, user, ds);
					if (!stored) {
						iterator.remove();
					} else {
						answer.setUpdated(false);
					}
				}
			}

			if (answers.getAnswers().isEmpty()) {
				ds.delete(DsUtil.toKey(answers.getKey()));
			}

			transaction.commit();

			dirtyMap.put(answers.getChildKey(), true);
			return answers;

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private boolean isDirty(String childKey) {
		Boolean dirty = dirtyMap.get(childKey);
		return dirty != null && dirty.booleanValue();
	}

	private GwtQuestionnaireAnswers getQuestionnaireAnswers(Entity answers,
			DatastoreService ds) {

		GwtQuestionnaireAnswers gwtAnswers = toGwtQuestionnaireAnswers(answers);
		PreparedQuery query = ds.prepare(new Query(QuestionnaireAnswer.KIND,
				answers.getKey()));
		for (Entity entity : query.asIterable()) {
			gwtAnswers.addAnswer(toGwtAnswer(entity));
		}
		return gwtAnswers;
	}

	private void storeAnswers(GwtQuestionnaireAnswers answers,
			DatastoreService ds) {
		Entity entity = toEntity(answers);
		ds.put(entity);
		answers.setKey(DsUtil.toString(entity.getKey()));
	}

	private boolean storeAnswer(GwtAnswer answer, String answersKey, User user,
			DatastoreService ds) {

		Object value = answer.getValue();
		if (value != null && !isEmptyCollection(value)) {
			
			Entity entity = toEntity(answer, DsUtil.toKey(answersKey), user);
			ds.put(entity);
			answer.setKey(DsUtil.toString(entity.getKey()));
			return true;
		}

		if (answer.getKey() != null) {
			ds.delete(DsUtil.toKey(answer.getKey()));
		}
		return false;
	}

	private boolean isEmptyCollection(Object value) {
		if(value instanceof Collection) {
			return ((Collection<?>) value).isEmpty();
		}
		return false;
	}

	private MemcacheService getCache() {
		return getCache(QuestionnaireAnswers.Cache.NAME);
	}

}
