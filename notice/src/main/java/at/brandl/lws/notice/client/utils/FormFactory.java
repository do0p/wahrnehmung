package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.List;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FormFactory {

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

		GwtAnswerTemplate template = question.getTemplate();
		if (template instanceof GwtMultipleChoiceAnswerTemplate) {

			Panel panel = new VerticalPanel();
			panel.setStyleName("answers", true);
			List<GwtMultipleChoiceOption> options = ((GwtMultipleChoiceAnswerTemplate) template)
					.getOptions();
			List<CheckBox> checkBoxes = new ArrayList<CheckBox>();
			for (GwtMultipleChoiceOption option : options) {

				CheckBox checkBox = createOption(option, question.getKey());
				checkBoxes.add(checkBox);
				panel.add(checkBox);
			}

			addValueChangeListener(checkBoxes);

			return panel;
		}
		return null;
	}

	private void addValueChangeListener(List<CheckBox> checkBoxes) {
		for (CheckBox checkBox : checkBoxes) {
			final List<CheckBox> otherBoxes = new ArrayList<CheckBox>(
					checkBoxes);
			otherBoxes.remove(checkBox);
			checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue() != null
							&& event.getValue().booleanValue()) {
						for (CheckBox other : otherBoxes) {
							other.setValue(false);
						}
					}

				}
			});
		}
	}

	public CheckBox createOption(GwtMultipleChoiceOption option,
			String questionKey) {

		CheckBox checkBox = new CheckBox(option.getLabel());

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

}
