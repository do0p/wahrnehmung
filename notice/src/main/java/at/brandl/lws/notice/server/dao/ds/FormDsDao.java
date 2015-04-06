package at.brandl.lws.notice.server.dao.ds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.util.Constants.AnswerTemplate;
import at.brandl.lws.notice.shared.util.Constants.MultipleChoiceOption;
import at.brandl.lws.notice.shared.util.Constants.Question;
import at.brandl.lws.notice.shared.util.Constants.QuestionGroup;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire.Cache;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;

public class FormDsDao extends AbstractDsDao {

	private static final String ALL_FORMS = "allForms";
	private static final String MULTIPLE_CHOICE = "multipleChoice";
	private boolean dirty;

	public List<GwtQuestionnaire> getAllQuestionnaires() {

		if (dirty || !getCache().contains(ALL_FORMS)) {

			List<GwtQuestionnaire> questionnaires = new ArrayList<GwtQuestionnaire>();
			DatastoreService ds = getDatastoreService();
			PreparedQuery query = ds.prepare(new Query(Questionnaire.KIND)
					.addSort(Questionnaire.CREATE_DATE));
			for (Entity form : query.asIterable()) {
				questionnaires.add(getQuestionnaire(ds, form));
			}

			getCache().put(ALL_FORMS, questionnaires);
		}

		return (List<GwtQuestionnaire>) getCache().get(ALL_FORMS);
	}
	
	public GwtQuestionnaire getQuestionnaire(String key) {

		GwtQuestionnaire questionnaire = (GwtQuestionnaire) getCache().get(key);
		if (questionnaire == null) {

			try {
				final DatastoreService ds = getDatastoreService();

				Entity form = ds.get(toKey(key));
				questionnaire = getQuestionnaire(ds, form);

				getCache().put(key, questionnaire);

			} catch (EntityNotFoundException e) {
				throw new IllegalArgumentException("no form with key " + key, e);
			}

		}
		return questionnaire;
	}

	public void storeQuestionnaire(GwtQuestionnaire gwtForm) {
		final DatastoreService datastoreService = getDatastoreService();

		final Transaction transaction = datastoreService.beginTransaction();
		try {

			storeForm(gwtForm, datastoreService);

			transaction.commit();
			getCache().put(gwtForm.getKey(), gwtForm);
			dirty = true;

		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private GwtQuestionnaire getQuestionnaire(final DatastoreService ds,
			Entity form) {
		
		GwtQuestionnaire questionnaire = toGwtQuestionnaire(form);
		questionnaire.setGroups(getGroups(form.getKey(), ds));
		return questionnaire;
	}

	private List<GwtQuestionGroup> getGroups(Key parent,
			final DatastoreService ds) {

		List<GwtQuestionGroup> groups = new ArrayList<GwtQuestionGroup>();
		PreparedQuery query = ds.prepare(new Query(QuestionGroup.KIND, parent)
				.addSort(QuestionGroup.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtQuestionGroup gwtQuestionGroup = toGwtQuestionGroup(entity);
			gwtQuestionGroup.setQuestions(getQuestions(entity.getKey(), ds));
			groups.add(gwtQuestionGroup);
		}
		return groups;
	}

	private List<GwtQuestion> getQuestions(Key parent, DatastoreService ds) {

		List<GwtQuestion> questions = new ArrayList<GwtQuestion>();
		PreparedQuery query = ds.prepare(new Query(Question.KIND, parent)
				.addSort(Question.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtQuestion gwtQuestion = toGwtQuestion(entity);
			gwtQuestion.setTemplate(getTemplate(entity.getKey(), ds));
			questions.add(gwtQuestion);
		}
		return questions;
	}

	private GwtAnswerTemplate getTemplate(Key key, DatastoreService ds) {

		PreparedQuery query = ds.prepare(new Query(AnswerTemplate.KIND, key));
		Entity entity = query.asSingleEntity();
		GwtAnswerTemplate gwtAnswerTemplate = toGwtAnswerTemplate(entity);
		if (gwtAnswerTemplate instanceof GwtMultipleChoiceAnswerTemplate) {
			((GwtMultipleChoiceAnswerTemplate) gwtAnswerTemplate)
					.setOptions(getOptions(entity.getKey(), ds));
		}

		return gwtAnswerTemplate;
	}

	private List<GwtMultipleChoiceOption> getOptions(Key parent,
			DatastoreService ds) {

		List<GwtMultipleChoiceOption> options = new ArrayList<GwtMultipleChoiceOption>();
		PreparedQuery query = ds.prepare(new Query(MultipleChoiceOption.KIND,
				parent).addSort(MultipleChoiceOption.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtMultipleChoiceOption gwtOption = toGwtMultipleChoiceOption(entity);
			options.add(gwtOption);
		}
		return options;
	}

	private void storeForm(GwtQuestionnaire gwtForm, final DatastoreService ds) {

		Entity form = toEntity(gwtForm);
		form.setProperty(Questionnaire.CREATE_DATE, new Date());
		ds.put(form);
		Key formKey = form.getKey();
		gwtForm.setKey(toString(formKey));

		int i = 0;
		for (GwtQuestionGroup gwtGroup : gwtForm.getGroups()) {

			storeGroup(gwtGroup, formKey, i++, ds);
		}
	}

	private void storeGroup(GwtQuestionGroup gwtGroup, Key parent, int order,
			final DatastoreService ds) {

		Entity group = toEntity(gwtGroup, parent, order);
		ds.put(group);
		Key groupKey = group.getKey();
		gwtGroup.setKey(toString(groupKey));

		int i = 0;
		for (GwtQuestion gwtQuestion : gwtGroup.getQuestions()) {

			storeQuestion(gwtQuestion, groupKey, i++, ds);
		}
	}

	private void storeQuestion(GwtQuestion gwtQuestion, Key parent, int order,
			final DatastoreService ds) {

		Entity question = toEntity(gwtQuestion, parent, order);
		ds.put(question);
		Key questionKey = question.getKey();
		gwtQuestion.setKey(toString(questionKey));

		storeTemplate(gwtQuestion.getTemplate(), questionKey, ds);
	}

	private void storeTemplate(GwtAnswerTemplate gwtTemplate, Key parent,
			final DatastoreService ds) {

		if (gwtTemplate instanceof GwtMultipleChoiceAnswerTemplate) {

			storeMultipleChoiceTemplate(
					(GwtMultipleChoiceAnswerTemplate) gwtTemplate, parent, ds);
		} else {

			throw new IllegalArgumentException("unknown type of template "
					+ gwtTemplate);
		}
	}

	private void storeMultipleChoiceTemplate(
			GwtMultipleChoiceAnswerTemplate gwtMultipleChoiceTemplate,
			Key parent, final DatastoreService ds) {

		Entity template = toEntity(MULTIPLE_CHOICE, parent);
		ds.put(template);
		Key templateKey = template.getKey();
		gwtMultipleChoiceTemplate.setKey(toString(templateKey));

		int i = 0;
		for (GwtMultipleChoiceOption gwtOption : gwtMultipleChoiceTemplate
				.getOptions()) {

			storeOption(gwtOption, templateKey, i++, ds);
		}
	}

	private void storeOption(GwtMultipleChoiceOption gwtOption, Key parent,
			int order, final DatastoreService ds) {

		Entity option = toEntity(gwtOption, parent, order);
		ds.put(option);
		gwtOption.setKey(toString(option.getKey()));
	}

	private Entity toEntity(GwtQuestionnaire gwtForm) {

		Entity form = new Entity(Questionnaire.KIND);
		form.setProperty(Questionnaire.TITLE, gwtForm.getTitle());
		form.setProperty(Questionnaire.SECTION, gwtForm.getSection());
		return form;
	}

	private Entity toEntity(GwtQuestionGroup gwtGroup, Key parent, int order) {

		Entity group = new Entity(QuestionGroup.KIND, parent);
		group.setProperty(QuestionGroup.TITLE, gwtGroup.getTitle());
		group.setProperty(QuestionGroup.ORDER, order);
		return group;
	}

	private Entity toEntity(GwtQuestion gwtQuestion, Key parent, int order) {

		Entity question = new Entity(Question.KIND, parent);
		question.setProperty(Question.LABEL, gwtQuestion.getLabel());
		question.setProperty(Question.ORDER, order);
		return question;
	}

	private Entity toEntity(String type, Key parent) {

		Entity template = new Entity(AnswerTemplate.KIND, parent);
		template.setProperty(AnswerTemplate.Type, type);
		return template;
	}

	private Entity toEntity(GwtMultipleChoiceOption gwtOption, Key parent,
			int order) {

		Entity option = new Entity(MultipleChoiceOption.KIND, parent);
		option.setProperty(MultipleChoiceOption.LABEL, gwtOption.getLabel());
		option.setProperty(MultipleChoiceOption.VALUE, gwtOption.getValue());
		option.setProperty(MultipleChoiceOption.ORDER, order);
		return option;
	}

	private GwtQuestionnaire toGwtQuestionnaire(Entity entity) {

		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle((String) entity.getProperty(Questionnaire.TITLE));
		form.setSection(toString((Key) entity
				.getProperty(Questionnaire.SECTION)));
		form.setKey(toString(entity.getKey()));
		return form;
	}

	private GwtQuestionGroup toGwtQuestionGroup(Entity entity) {

		GwtQuestionGroup group = new GwtQuestionGroup();
		group.setTitle((String) entity.getProperty(QuestionGroup.TITLE));
		group.setKey(toString(entity.getKey()));
		return group;
	}

	private GwtQuestion toGwtQuestion(Entity entity) {

		GwtQuestion question = new GwtQuestion();
		question.setLabel((String) entity.getProperty(Question.LABEL));
		question.setKey(toString(entity.getKey()));
		return question;
	}

	private GwtAnswerTemplate toGwtAnswerTemplate(Entity entity) {

		String type = (String) entity.getProperty(AnswerTemplate.Type);
		if (MULTIPLE_CHOICE.equals(type)) {

			GwtMultipleChoiceAnswerTemplate template = new GwtMultipleChoiceAnswerTemplate();
			template.setKey(toString(entity.getKey()));
			return template;
		}
		throw new IllegalStateException("unknown type of answer template "
				+ type);
	}

	private GwtMultipleChoiceOption toGwtMultipleChoiceOption(Entity entity) {

		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setLabel((String) entity.getProperty(MultipleChoiceOption.LABEL));
		option.setValue((String) entity.getProperty(MultipleChoiceOption.VALUE));
		option.setKey(toString(entity.getKey()));
		return option;
	}



	private MemcacheService getCache() {
		return getCache(Cache.NAME);
	}

}
