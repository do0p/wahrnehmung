package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.dao.DsUtil.toKey;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAnswerTemplateConverter.toGwtAnswerTemplate;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtMultipleChoiceOptionConverter.toGwtMultipleChoiceOption;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionConverter.toGwtQuestion;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionGroupConverter.toGwtQuestionGroup;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionnaireConverter.toGwtQuestionnaire;

import java.util.ArrayList;
import java.util.List;

import at.brandl.lws.notice.dao.DsUtil;
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class FormDsReader {

	private final DatastoreService ds;

	public FormDsReader(DatastoreService ds) {
		this.ds = ds;
	}

	public List<GwtQuestionnaire> readAllForms() {
		List<GwtQuestionnaire> questionnaires = new ArrayList<>();
		PreparedQuery query = ds.prepare(new Query(Questionnaire.KIND));
		for (Entity form : query.asIterable(FetchOptions.Builder.withDefaults())) {
			GwtQuestionnaire questionnaire = convertToQuestionnaire(form);
			questionnaires.add(questionnaire);
		}
		return questionnaires;
	}

	public GwtQuestionnaire readForm(String key) throws EntityNotFoundException {
		Entity form = ds.get(DsUtil.toKey(key));
		return convertToQuestionnaire(form);
	}

	public GwtQuestion readQuestion(String questionKey) {

		try {
			Entity entity = ds.get(toKey(questionKey));
			GwtQuestion gwtQuestion = toGwtQuestion(entity);
			gwtQuestion.setTemplate(getTemplate(entity.getKey()));
			return gwtQuestion;
		} catch (EntityNotFoundException e) {
			throw new IllegalArgumentException("no question for key "
					+ questionKey);
		}
	}

	private GwtQuestionnaire convertToQuestionnaire(Entity form) {

		GwtQuestionnaire questionnaire = toGwtQuestionnaire(form);
		questionnaire.setGroups(getGroups(form.getKey()));
		return questionnaire;
	}

	private List<GwtQuestionGroup> getGroups(Key parent) {

		List<GwtQuestionGroup> groups = new ArrayList<GwtQuestionGroup>();
		PreparedQuery query = ds.prepare(new Query(QuestionGroup.KIND, parent)
				.addSort(QuestionGroup.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtQuestionGroup gwtQuestionGroup = toGwtQuestionGroup(entity);
			if(gwtQuestionGroup.isArchived()) {
				continue;
			}
			gwtQuestionGroup.setQuestions(getQuestions(entity.getKey()));
			groups.add(gwtQuestionGroup);
		}
		return groups;
	}

	private List<GwtQuestion> getQuestions(Key groupKey) {

		List<GwtQuestion> questions = new ArrayList<GwtQuestion>();
		PreparedQuery query = ds.prepare(new Query(Question.KIND, groupKey)
				.addSort(Question.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtQuestion gwtQuestion = toGwtQuestion(entity);
			if(gwtQuestion.isArchived()) {
				continue;
			}
			gwtQuestion.setTemplate(getTemplate(entity.getKey()));
			questions.add(gwtQuestion);
		}
		return questions;
	}

	private GwtAnswerTemplate getTemplate(Key key) {

		PreparedQuery query = ds.prepare(new Query(AnswerTemplate.KIND, key));
		Entity entity = query.asSingleEntity();
		GwtAnswerTemplate gwtAnswerTemplate = toGwtAnswerTemplate(entity);
		if (gwtAnswerTemplate instanceof GwtMultipleChoiceAnswerTemplate) {
			((GwtMultipleChoiceAnswerTemplate) gwtAnswerTemplate)
					.setOptions(getOptions(entity.getKey()));
		}

		return gwtAnswerTemplate;
	}

	private List<GwtMultipleChoiceOption> getOptions(Key parent) {

		List<GwtMultipleChoiceOption> options = new ArrayList<GwtMultipleChoiceOption>();
		PreparedQuery query = ds.prepare(new Query(MultipleChoiceOption.KIND,
				parent).addSort(MultipleChoiceOption.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtMultipleChoiceOption gwtOption = toGwtMultipleChoiceOption(entity);
			options.add(gwtOption);
		}
		return options;
	}
}
