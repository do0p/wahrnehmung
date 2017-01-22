package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

public class FormFactory {

	private QuestionnaireAnswerListener answerListener;
	private GwtQuestionnaireAnswers answers;

	public FormFactory(QuestionnaireAnswerListener answerListener) {
		this.answerListener = answerListener;
	}

	public Label createTitle(String titleText) {

		return createLabel(titleText, "questionnaireHeading");
	}

	public Widget createGroup(GwtQuestionGroup group) {

		Panel panel = new VerticalPanel();
		panel.setStyleName("questionGroup", true);
		Label title = createGroupTitle(group.getTitle());
		if (title != null) {
			panel.add(title);
		}

		List<GwtQuestion> questions = group.getQuestions();

		for (GwtQuestion question : questions) {

			String style = title == null ? "question" : "groupQuestion";
			panel.add(createLabel(question.getLabel(), style));
			panel.add(createAnswerTemplate(question));
		}

		return panel;
	}

	public Widget createAnswerTemplate(GwtQuestion question) {

		GwtAnswerTemplate template = question.getAnswerTemplate();
		if (template instanceof GwtMultipleChoiceAnswerTemplate) {

			Panel panel = new VerticalPanel();
			panel.setStyleName("answers", true);
			List<GwtMultipleChoiceOption> options = ((GwtMultipleChoiceAnswerTemplate) template)
					.getOptions();
			List<MultipleChoiceCheckBox> checkBoxes = new ArrayList<MultipleChoiceCheckBox>();
			for (GwtMultipleChoiceOption option : options) {

				MultipleChoiceCheckBox checkBox = createOption(option,
						question.getKey());
				checkBoxes.add(checkBox);
				panel.add(checkBox);
			}

			addValueChangeListener(checkBoxes);

			return panel;
		}
		return null;
	}

	private void addValueChangeListener(List<MultipleChoiceCheckBox> checkBoxes) {
		for (final MultipleChoiceCheckBox checkBox : checkBoxes) {
			final List<MultipleChoiceCheckBox> otherBoxes = new ArrayList<MultipleChoiceCheckBox>(
					checkBoxes);
			otherBoxes.remove(checkBox);
			checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					boolean checked = event.getValue() != null
							&& event.getValue().booleanValue();
					List<String> value = new ArrayList<String>();
					if (checked) {
						for (CheckBox other : otherBoxes) {
							other.setValue(false, false);
						}
						value.add(checkBox.getOptionValue());
					}
					answerListener.notifyAnswer(checkBox.getQuestionKey(),
							value);
				}
			});
		}
	}

	public MultipleChoiceCheckBox createOption(GwtMultipleChoiceOption option,
			String questionKey) {

		MultipleChoiceCheckBox checkBox = new MultipleChoiceCheckBox(option,
				questionKey);
		GwtAnswer answer = answers.getAnswer(questionKey);
		if (answer != null) {
			
			if (answer instanceof GwtMultipleChoiceAnswer) {
				Collection<String> values = ((GwtMultipleChoiceAnswer) answer)
						.getValue();
				if (values.contains(option.getValue())) {
					checkBox.setValue(true, false);
				}
			} else {
				throw new IllegalStateException("MultipleChoiceAnswer expected");
			}
		}
		return checkBox;

	}

	public Label createGroupTitle(String titleText) {

		return createLabel(titleText, "groupHeading");
	}

	private Label createLabel(String titleText, String style) {
		if (titleText == null) {
			return null;
		}
		Label title = new Label(titleText);
		title.setStyleName(style, true);
		return title;
	}

	public void setAnswers(GwtQuestionnaireAnswers answers) {
		this.answers = answers;
	}

}
