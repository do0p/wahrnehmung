package at.brandl.lws.notice.server.dao.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.util.Constants;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswer;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswers;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireAnswersValidator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
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
					QuestionnaireAnswers.KIND, toKey(childKey)));
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
				ds.delete(toKey(answers.getKey()));
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
		answers.setKey(toString(entity.getKey()));
	}

	private boolean storeAnswer(GwtAnswer answer, String answersKey, User user,
			DatastoreService ds) {

		Object value = answer.getValue();
		if (value != null && !isEmptyCollection(value)) {
			
			Entity entity = toEntity(answer, toKey(answersKey), user);
			ds.put(entity);
			answer.setKey(toString(entity.getKey()));
			return true;
		}

		if (answer.getKey() != null) {
			ds.delete(toKey(answer.getKey()));
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

	private GwtQuestionnaireAnswers toGwtQuestionnaireAnswers(Entity entity) {

		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setKey(toString(entity.getKey()));
		answers.setChildKey(toString(entity.getParent()));
		answers.setQuestionnaireKey(toString((Key) entity
				.getProperty(QuestionnaireAnswers.QUESTIONNAIRE_KEY)));
		return answers;
	}

	private GwtAnswer toGwtAnswer(Entity entity) {

		String type = (String) entity.getProperty(QuestionnaireAnswer.TYPE);
		if (Constants.MULTIPLE_CHOICE.equals(type)) {
			GwtMultipleChoiceAnswer answer = new GwtMultipleChoiceAnswer();
			answer.setKey(toString(entity.getKey()));
			answer.setDate((Date) entity.getProperty(QuestionnaireAnswer.DATE));
			answer.setQuestionKey(toString((Key) entity
					.getProperty(QuestionnaireAnswer.QUESTION_KEY)));
			answer.setValue(entity.getProperty(QuestionnaireAnswer.VALUE));
			return answer;
		}
		throw new IllegalStateException("unknown type of answer: " + type);

	}

	private Entity toEntity(GwtQuestionnaireAnswers answers) {
		Entity entity = new Entity(QuestionnaireAnswers.KIND,
				toKey(answers.getChildKey()));
		entity.setProperty(QuestionnaireAnswers.QUESTIONNAIRE_KEY,
				toKey(answers.getQuestionnaireKey()));
		return entity;
	}

	private Entity toEntity(GwtAnswer answer, Key parent, User user) {

		String key = answer.getKey();
		Entity entity;
		if (key == null) {
			entity = new Entity(QuestionnaireAnswer.KIND, parent);
		} else {
			entity = new Entity(toKey(key));
		}
		entity.setProperty(QuestionnaireAnswer.DATE, answer.getDate());
		entity.setProperty(QuestionnaireAnswer.QUESTION_KEY,
				toKey(answer.getQuestionKey()));
		entity.setProperty(QuestionnaireAnswer.VALUE, answer.getValue());
		entity.setProperty(QuestionnaireAnswer.USER, user);
		entity.setProperty(QuestionnaireAnswer.TYPE, Constants.MULTIPLE_CHOICE);

		return entity;
	}

}
