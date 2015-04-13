package at.brandl.lws.notice.shared.service;

import java.util.Iterator;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;

public class FormPrinter {

	protected static final String QUESTION_MARK = "?";
	protected static final String OPTION_DELIMITER = "|";
	protected static final String SEPERATOR = ":";
	protected static final String ANSWER_MARK = SEPERATOR + SEPERATOR;
	private static final String NL = "\r\n";


	public String toString(GwtQuestionnaire form) {

		StringBuilder formText = new StringBuilder();

		appendForm(form, formText);

		return formText.toString();
	}

	private void appendForm(GwtQuestionnaire form, StringBuilder formText) {

		appendTitle(formText, form.getTitle());

		for (GwtQuestionGroup group : form.getGroups()) {

			appendGroup(formText, group);
		}
	}

	private void appendGroup(StringBuilder formText, GwtQuestionGroup group) {

		formText.append("-");
		if (group.getTitle() != null) {
			formText.append(" ").append(group.getTitle());
		}
		formText.append(NL);

		for (GwtQuestion question : group.getQuestions()) {

			appendQuestion(formText, question);

		}
		formText.append("-");
		formText.append(NL);
	}

	private void appendQuestion(StringBuilder formText, GwtQuestion question) {

		appendQuestionLabel(formText, question.getLabel());

		GwtAnswerTemplate template = question.getTemplate();

		if (template instanceof GwtMultipleChoiceAnswerTemplate) {
			appendMultipleChoiceTemplate(formText,
					(GwtMultipleChoiceAnswerTemplate) template);
		}

		formText.append(NL);
	}

	private void appendMultipleChoiceTemplate(StringBuilder formText,
			GwtMultipleChoiceAnswerTemplate template) {

		Iterator<GwtMultipleChoiceOption> options = template.getOptions()
				.iterator();

		while (options.hasNext()) {

			appendMultipleChoiceOption(formText, options.next());

			if (options.hasNext()) {
				formText.append(" " + OPTION_DELIMITER + " ");
			}
		}
	}

	private void appendMultipleChoiceOption(StringBuilder formText,
			GwtMultipleChoiceOption option) {

		formText.append(option.getLabel());
		formText.append(SEPERATOR);
		formText.append(option.getValue());
	}

	private void appendQuestionLabel(StringBuilder formText, String label) {

		formText.append(QUESTION_MARK + " ");
		formText.append(label);
		formText.append(" " + ANSWER_MARK + " ");
	}

	private void appendTitle(StringBuilder formText, String title) {

		formText.append(title);
		formText.append(NL);
	}



}
