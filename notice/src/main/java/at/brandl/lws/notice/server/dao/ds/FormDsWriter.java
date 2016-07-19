package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.DsUtil.toKey;
import java.util.Date;

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
import com.google.appengine.api.datastore.Key;

public class FormDsWriter {

	private final DatastoreService ds;

	public FormDsWriter(DatastoreService ds) {
		this.ds = ds;
	}

	public void writeForm(GwtQuestionnaire gwtForm) {

		Entity form = toEntity(gwtForm);
		ds.put(form);
		Key formKey = form.getKey();
		gwtForm.setKey(toString(formKey));

		int order = 0;
		for (GwtQuestionGroup gwtGroup : gwtForm.getGroups()) {

			storeGroup(gwtGroup, formKey, order++);
		}
	}

	private void storeQuestion(GwtQuestion gwtQuestion, Key parent, int order) {

		GwtQuestion storedQuestion = getQuestion(gwtQuestion.getKey());
		if (storedQuestion == null) {

			Entity question = toEntity(gwtQuestion, parent, order);
			ds.put(question);
			Key questionKey = question.getKey();
			gwtQuestion.setKey(toString(questionKey));
			storeTemplate(gwtQuestion.getTemplate(), questionKey);

		} else if (!gwtQuestion.equals(storedQuestion)) {

			archiveStoredQuestion(storedQuestion);
			updateQuestion(gwtQuestion);
		}
	}

	private void updateQuestion(GwtQuestion gwtQuestion) {
		// TODO Auto-generated method stub

	}

	private void archiveStoredQuestion(GwtQuestion storedQuestion) {
		// TODO Auto-generated method stub

	}

	private void storeGroup(GwtQuestionGroup gwtGroup, Key parent, int order) {

		Entity group = toEntity(gwtGroup, parent, order);
		ds.put(group);
		Key groupKey = group.getKey();
		gwtGroup.setKey(toString(groupKey));

		int i = 0;
		for (GwtQuestion gwtQuestion : gwtGroup.getQuestions()) {

			storeQuestion(gwtQuestion, groupKey, i++);
		}
	}

	private GwtQuestion getQuestion(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	private void storeTemplate(GwtAnswerTemplate gwtTemplate, Key parent) {

		if (gwtTemplate instanceof GwtMultipleChoiceAnswerTemplate) {

			storeMultipleChoiceTemplate(
					(GwtMultipleChoiceAnswerTemplate) gwtTemplate, parent);
		} else {

			throw new IllegalArgumentException("unknown type of template "
					+ gwtTemplate);
		}
	}

	private void storeMultipleChoiceTemplate(
			GwtMultipleChoiceAnswerTemplate gwtMultipleChoiceTemplate,
			Key parent) {

		Entity template = toEntity(gwtMultipleChoiceTemplate, parent);
		ds.put(template);
		Key templateKey = template.getKey();
		gwtMultipleChoiceTemplate.setKey(toString(templateKey));

		int i = 0;
		for (GwtMultipleChoiceOption gwtOption : gwtMultipleChoiceTemplate
				.getOptions()) {

			storeOption(gwtOption, templateKey, i++);
		}
	}

	private void storeOption(GwtMultipleChoiceOption gwtOption, Key parent,
			int order) {

		Entity option = toEntity(gwtOption, parent, order);
		ds.put(option);
		gwtOption.setKey(toString(option.getKey()));
	}

	private Entity toEntity(GwtQuestionnaire gwtForm) {

		Entity form;
		if (gwtForm.getKey() != null) {
			form = new Entity(toKey(gwtForm.getKey()));
			form.setProperty(Questionnaire.UPDATE_DATE, new Date());
		} else {
			form = new Entity(Questionnaire.KIND);
			form.setProperty(Questionnaire.CREATE_DATE, new Date());
		}
		form.setProperty(Questionnaire.TITLE, gwtForm.getTitle());
		form.setProperty(Questionnaire.SECTION, toKey(gwtForm.getSection()));
		return form;
	}

	private Entity toEntity(GwtQuestionGroup gwtGroup, Key parent, int order) {

		Entity group;
		if (gwtGroup.getKey() != null) {
			group = new Entity(toKey(gwtGroup.getKey()));
		} else {
			group = new Entity(QuestionGroup.KIND, parent);
		}
		group.setProperty(QuestionGroup.TITLE, gwtGroup.getTitle());
		group.setProperty(QuestionGroup.ORDER, order);
		return group;
	}

	private Entity toEntity(GwtQuestion gwtQuestion, Key parent, int order) {

		Entity question;
		if (gwtQuestion.getKey() != null) {
			question = new Entity(toKey(gwtQuestion.getKey()));
		} else {
			question = new Entity(Question.KIND, parent);
		}
		question.setProperty(Question.LABEL, gwtQuestion.getLabel());
		question.setProperty(Question.ORDER, order);
		return question;
	}

	private Entity toEntity(GwtAnswerTemplate gwtTemplate, Key parent) {
		String type;
		if (gwtTemplate instanceof GwtMultipleChoiceAnswerTemplate) {
			type = Constants.MULTIPLE_CHOICE;
		} else {
			throw new RuntimeException("no mapping for templateclass "
					+ gwtTemplate.getClass());
		}

		Entity template;
		if (gwtTemplate.getKey() != null) {
			template = new Entity(toKey(gwtTemplate.getKey()));
		} else {
			template = new Entity(AnswerTemplate.KIND, parent);
		}
		template.setProperty(AnswerTemplate.Type, type);
		return template;
	}

	private Entity toEntity(GwtMultipleChoiceOption gwtOption, Key parent,
			int order) {

		Entity option;
		if (gwtOption.getKey() != null) {
			option = new Entity(toKey(gwtOption.getKey()));
		} else {
			option = new Entity(MultipleChoiceOption.KIND, parent);
		}
		option.setProperty(MultipleChoiceOption.LABEL, gwtOption.getLabel());
		option.setProperty(MultipleChoiceOption.VALUE, gwtOption.getValue());
		option.setProperty(MultipleChoiceOption.ORDER, order);
		return option;
	}

	private String toString(Key key) {
		return DsUtil.toString(key);
	}
}
