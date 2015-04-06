package at.brandl.lws.notice.client;

import java.util.Date;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.FormFactory;
import at.brandl.lws.notice.client.utils.FormSelection;
import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.QuestionnairePanel;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class Questionnaire extends VerticalPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final FormServiceAsync formService = (FormServiceAsync) GWT
			.create(FormService.class);

	private final DateBox dateBox;
	private final PopUp dialogBox;
	private final NameSelection nameSelection;
	private final FormSelection formSelection;
	private final Button sendButton;
	private final Button newButton;
	// private final Button nameAddButton;
	private final DecisionBox decisionBox;

	private boolean changes;
	private GwtQuestionnaire questionnaire;
	private GwtQuestionnaireAnswers answers;
	private QuestionnairePanel questionnairePanel;
	private FormFactory formFactory;

	public Questionnaire(Authorization authorization) {

		questionnaire = new GwtQuestionnaire();
		answers = new GwtQuestionnaireAnswers();

		dateBox = new DateBox();
		dialogBox = new PopUp();
		nameSelection = new NameSelection(dialogBox);
		formSelection = new FormSelection(dialogBox);
		decisionBox = new DecisionBox();
		sendButton = new Button(labels.save());
		newButton = new Button(labels.cancel());
		questionnairePanel = new QuestionnairePanel(new FormFactory());

		init();
		layout();
		updateState();
	}

	private void init() {

		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				markChanged();
				updateState();
			}

		});

		formSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				questionnairePanel.setQuestionnaire(formSelection.getSelectedForm());
				markChanged();
				updateState();
			}
		});

		dateBox.setValue(new Date());
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dateBox.setFireNullValues(true);
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				markChanged();
				updateState();
			}
		});

		decisionBox.setText(labels.notSavedWarning());
		decisionBox.addOkClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetForm();
				updateState();
			}
		});

		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendButton.setEnabled(false);
				storeBeobachtung();
				updateState();
			}
		});

		newButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (changes) {
					decisionBox.center();
				} else {
					resetForm();
				}
				updateState();
			}
		});

	}

	private void layout() {

		Panel panel = new ScrollPanel(questionnairePanel);
		panel.setStyleName("questionnaireScrollPanel", true);
	
		add(nameSelection);
		add(formSelection);
		add(panel);

		final Panel buttonContainer = createButtonContainer();
		Utils.formatCenter(this, buttonContainer);

	}

	private void resetForm() {
		dateBox.setValue(new Date());

	}

	private Panel createButtonContainer() {
		final Grid buttonContainer = new Grid(1, 2);

		sendButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		newButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		buttonContainer.setWidget(0, 0, sendButton);
		buttonContainer.setWidget(0, 1, newButton);

		return buttonContainer;
	}

	private void markChanged() {
		if (!changes) {
			changes = true;
		}
	}

	private void storeBeobachtung() {

	}

	private void displayErrorMessage() {
		dialogBox.setErrorMessage();
		dialogBox.setDisableWhileShown(sendButton);
		dialogBox.center();
	}

	private String cleanUp(String text) {
		return (text != null && text.equals("<br>")) ? "" : text;
	}

	private void updateState() {

		// nameAddButton.setEnabled(enableNameAdd());
		sendButton.setEnabled(enableSend());
		newButton.setEnabled(true);
	}

	private boolean enableSend() {
		// return changes && GwtBeobachtungValidator.valid(beobachtung);
		return true;
	}

	private String removeBirthDate(String name) {
		int pos = name.indexOf('(');
		if (pos != -1) {
			name = name.substring(0, pos - 1);
		}
		return name;
	}

}
