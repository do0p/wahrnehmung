package at.brandl.lws.notice.server.dao.ds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire.Cache;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireValidator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;

public class FormDsDao extends AbstractDsDao {

	static final String ALL_FORMS = "allForms";

	public List<GwtQuestionnaire> getAllQuestionnaires() {

		Map<String, GwtQuestionnaire> allForms = getAllQuestionnairesAsMap();
		return new ArrayList<GwtQuestionnaire>(allForms.values());
	}

	public GwtQuestionnaire getQuestionnaire(String key) {

		Map<String, GwtQuestionnaire> allForms = getAllQuestionnairesAsMap();
		GwtQuestionnaire questionnaire = allForms.get(key);
		if (questionnaire == null) {

			try {
				final DatastoreService ds = getDatastoreService();
				questionnaire = new FormDsReader(ds).readForm(key);

			} catch (EntityNotFoundException e) {
				throw new IllegalArgumentException("no form with key " + key, e);
			}
			updateCache(questionnaire);
		}
		return questionnaire;
	}

	public GwtQuestionnaire getQuestionnaire(GwtQuestionnaireAnswers answers) {

		GwtQuestionnaire questionnaire = getQuestionnaire(answers
				.getQuestionnaireKey());
		for (GwtAnswer answer : answers.getAnswers()) {
			Date createDate = answer.getCreateDate();
			String questionKey = answer.getQuestionKey();
			// Entity history = getHistoricQuestion(questionKey, createDate);
			// if(history != null) {

			// s }
		}

		return questionnaire;
	}

	public void storeQuestionnaire(GwtQuestionnaire gwtForm) {

		if (!GwtQuestionnaireValidator.valid(gwtForm)) {
			throw new IllegalArgumentException("illegal form " + gwtForm);
		}

		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {

			new FormDsWriter(datastoreService).writeForm(gwtForm);

			transaction.commit();
			updateCache(gwtForm);

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, GwtQuestionnaire> getAllQuestionnairesAsMap() {
		synchronized (ALL_FORMS) {
			if (!getCache().contains(ALL_FORMS)) {

				DatastoreService ds = getDatastoreService();
				List<GwtQuestionnaire> allForms = new FormDsReader(ds)
						.readAllForms();

				Map<String, GwtQuestionnaire> questionnaires = new HashMap<String, GwtQuestionnaire>();
				for (GwtQuestionnaire form : allForms) {
					questionnaires.put(form.getKey(), form);
				}

				getCache().put(ALL_FORMS, questionnaires);
			}
		}

		return (Map<String, GwtQuestionnaire>) getCache().get(ALL_FORMS);
	}

	private void updateCache(GwtQuestionnaire gwtForm) {
		synchronized (ALL_FORMS) {
			@SuppressWarnings("unchecked")
			Map<String, GwtQuestionnaire> questionnaires = (Map<String, GwtQuestionnaire>) getCache()
					.get(ALL_FORMS);
			if (questionnaires == null) {
				questionnaires = new HashMap<>();
			}
			questionnaires.put(gwtForm.getKey(), gwtForm);
			getCache().put(ALL_FORMS, questionnaires);
		}
	}

	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}
}
