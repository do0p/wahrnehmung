package at.brandl.lws.notice.server.dao.ds;

import java.util.ArrayList;
import java.util.List;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.util.Constants;
import at.brandl.lws.notice.shared.util.Constants.AnswerTemplate;
import at.brandl.lws.notice.shared.util.Constants.MultipleChoiceOption;
import at.brandl.lws.notice.shared.util.Constants.Question;
import at.brandl.lws.notice.shared.util.Constants.QuestionGroup;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class FormDsReader {

	private final DatastoreService ds;

	public FormDsReader(DatastoreService ds) {
		this.ds = ds;
	}

	public List<GwtQuestionnaire> readAllForms(){
		List<GwtQuestionnaire> questionnaires = new ArrayList<>();
		PreparedQuery query = ds.prepare(new Query(Questionnaire.KIND)
				.addSort(Questionnaire.CREATE_DATE));
		for (Entity form : query.asIterable()) {
			GwtQuestionnaire questionnaire = convertToQuestionnaire(form);
			questionnaires.add(questionnaire);
		}
		return questionnaires;
	}

	public GwtQuestionnaire readForm(String key) throws EntityNotFoundException {
		Entity form = ds.get(DsUtil.toKey(key));
		return convertToQuestionnaire(form);
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
			gwtQuestionGroup.setQuestions(getQuestions(entity.getKey()));
			groups.add(gwtQuestionGroup);
		}
		return groups;
	}

	private List<GwtQuestion> getQuestions(Key parent) {

		List<GwtQuestion> questions = new ArrayList<GwtQuestion>();
		PreparedQuery query = ds.prepare(new Query(Question.KIND, parent)
				.addSort(Question.ORDER));
		for (Entity entity : query.asIterable()) {

			GwtQuestion gwtQuestion = toGwtQuestion(entity);
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

	private GwtQuestionnaire toGwtQuestionnaire(Entity entity) {

		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle((String) entity.getProperty(Questionnaire.TITLE));
		form.setSection(DsUtil.toString((Key) entity
				.getProperty(Questionnaire.SECTION)));
		form.setKey(DsUtil.toString(entity.getKey()));
		return form;
	}

	private GwtQuestionGroup toGwtQuestionGroup(Entity entity) {

		GwtQuestionGroup group = new GwtQuestionGroup();
		group.setTitle((String) entity.getProperty(QuestionGroup.TITLE));
		group.setKey(DsUtil.toString(entity.getKey()));
		return group;
	}

	private GwtQuestion toGwtQuestion(Entity entity) {

		GwtQuestion question = new GwtQuestion();
		question.setLabel((String) entity.getProperty(Question.LABEL));
		question.setKey(DsUtil.toString(entity.getKey()));
		return question;
	}

	private GwtAnswerTemplate toGwtAnswerTemplate(Entity entity) {

		String type = (String) entity.getProperty(AnswerTemplate.Type);
		if (Constants.MULTIPLE_CHOICE.equals(type)) {

			GwtMultipleChoiceAnswerTemplate template = new GwtMultipleChoiceAnswerTemplate();
			template.setKey(DsUtil.toString(entity.getKey()));
			return template;
		}
		throw new IllegalStateException("unknown type of answer template "
				+ type);
	}

	private GwtMultipleChoiceOption toGwtMultipleChoiceOption(Entity entity) {

		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setLabel((String) entity.getProperty(MultipleChoiceOption.LABEL));
		option.setValue((String) entity.getProperty(MultipleChoiceOption.VALUE));
		option.setKey(DsUtil.toString(entity.getKey()));
		return option;
	}
}
