package at.brandl.lws.notice.server.dao.ds;

import static at.brandl.lws.notice.server.dao.ds.DsUtil.toKey;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtAnswerTemplateConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtMultipleChoiceOptionConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionConverter.toGwtQuestion;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionGroupConverter.toEntity;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionGroupConverter.toGwtQuestionGroup;
import static at.brandl.lws.notice.server.dao.ds.converter.GwtQuestionnaireConverter.toEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.Utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

public class FormDsWriter {

	private final DatastoreService ds;
	private FormDsReader formReader;

	public FormDsWriter(DatastoreService ds) {
		this.ds = ds;
		formReader = new FormDsReader(ds);
	}

	public void writeForm(GwtQuestionnaire gwtForm) {

		String key = gwtForm.getKey();
		GwtQuestionnaire persistedForm = null;
		if (Utils.isNotEmpty(key)) {
			try {
				persistedForm = formReader.readForm(key);
			} catch (EntityNotFoundException e) {
				System.err.println("no form for key " + key);
				throw new IllegalArgumentException("no form for key " + key, e);
			}
		}

		Entity form = toEntity(gwtForm);
		ds.put(form);
		Key formKey = form.getKey();
		gwtForm.setKey(toString(formKey));

		int order = 0;
		Map<String, GwtQuestionGroup> persistedGroups = new HashMap<>();
		if (persistedForm != null) {
			for (GwtQuestionGroup group : persistedForm.getGroups()) {
				persistedGroups.put(group.getKey(), group);
			}
		}
		for (GwtQuestionGroup gwtGroup : gwtForm.getGroups()) {

			GwtQuestionGroup persistedGroup = persistedGroups.remove(gwtGroup
					.getKey());
			storeGroup(gwtGroup, formKey, order++, persistedGroup);
		}
		for (Entry<String, GwtQuestionGroup> groupToArchive : persistedGroups
				.entrySet()) {
			try {
				Entity entity = ds.get(DsUtil.toKey(groupToArchive.getKey()));
				GwtQuestionGroup persistedGroup = toGwtQuestionGroup(entity);
				persistedGroup.setArchiveDate(new Date());
				ds.put(toEntity(persistedGroup, null, 0));
			} catch (EntityNotFoundException e) {
				throw new IllegalArgumentException("no group for key "
						+ groupToArchive.getKey());
			}
		}
	}

	private GwtQuestion storeQuestion(GwtQuestion gwtQuestion, Key groupKey,
			int order) {

		String key = gwtQuestion.getKey();

		if (!Utils.isEmpty(key)) {

			GwtQuestion storedQuestion = formReader.readQuestion(key);
			Key questionKey = toKey(key);
			if (questionKey.getParent().equals(groupKey)
					&& gwtQuestion.equals(storedQuestion)) {
				return gwtQuestion;
			}

			archiveQuestion(storedQuestion, order);
			gwtQuestion = copy(gwtQuestion);
			gwtQuestion.addArchived(key);
		}

		Entity question = toEntity(gwtQuestion, groupKey, order);
		ds.put(question);
		Key questionKey = question.getKey();
		gwtQuestion.setKey(toString(questionKey));
		storeTemplate(gwtQuestion.getAnswerTemplate(), question.getKey());
		return gwtQuestion;
	}

	private GwtQuestion copy(GwtQuestion questionToStore) {
		GwtAnswerTemplate templateToStore = copy(questionToStore
				.getAnswerTemplate());
		questionToStore = GwtQuestion.valueOf(questionToStore);
		questionToStore.setTemplate(templateToStore);
		return questionToStore;
	}

	private GwtAnswerTemplate copy(GwtAnswerTemplate answerTemplate) {
		if (answerTemplate instanceof GwtMultipleChoiceAnswerTemplate) {
			GwtMultipleChoiceAnswerTemplate multipleChoiceTemplate = (GwtMultipleChoiceAnswerTemplate) answerTemplate;
			List<GwtMultipleChoiceOption> options = new ArrayList<>(
					multipleChoiceTemplate.getOptions());

			answerTemplate = GwtMultipleChoiceAnswerTemplate
					.valueOf(multipleChoiceTemplate);
			multipleChoiceTemplate.getOptions().clear();;
			for (GwtMultipleChoiceOption option : options) {
				multipleChoiceTemplate.addOption(GwtMultipleChoiceOption.valueOf(option));
			}
			return answerTemplate;
		}
		throw new IllegalArgumentException("unsupported answer template");
	}

	private void archiveQuestion(GwtQuestion gwtQuestion, int order) {

		gwtQuestion.setArchiveDate(new Date());
		Entity question = toEntity(gwtQuestion, null, order);
		ds.put(question);
	}

	private void storeGroup(GwtQuestionGroup gwtGroup, Key parent, int order,
			GwtQuestionGroup persistedGroup) {

		Entity group = toEntity(gwtGroup, parent, order);
		ds.put(group);
		Key groupKey = group.getKey();
		gwtGroup.setKey(toString(groupKey));

		Map<String, GwtQuestion> persistedQuestions = new HashMap<>();
		if (persistedGroup != null) {
			for (GwtQuestion question : persistedGroup.getQuestions()) {
				persistedQuestions.put(question.getKey(), question);
			}
		}
		int questionOrder = 0;
		for (GwtQuestion gwtQuestion : gwtGroup.getQuestions()) {
			String questionKey = gwtQuestion.getKey();
			persistedQuestions.remove(questionKey);
			GwtQuestion storedQuestion = storeQuestion(gwtQuestion, groupKey,
					questionOrder++);
			if (questionKey != null
					&& !questionKey.equals(storedQuestion.getKey())) {
				gwtGroup.replaceQuestion(questionKey, storedQuestion);
			}
		}
		for (Entry<String, GwtQuestion> questionToArchive : persistedQuestions
				.entrySet()) {
			try {
				Entity entity = ds
						.get(DsUtil.toKey(questionToArchive.getKey()));
				GwtQuestion persistedQuestion = toGwtQuestion(entity);
				persistedQuestion.setArchiveDate(new Date());
				ds.put(toEntity(persistedQuestion, null, 0));
			} catch (EntityNotFoundException e) {
				throw new IllegalArgumentException("no group for key "
						+ questionToArchive.getKey());
			}
		}
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

	private String toString(Key key) {
		return DsUtil.toString(key);
	}
}
