package at.brandl.lws.notice.client;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

import at.brandl.lws.notice.client.utils.ChangeListener;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.FormSelection;
import at.brandl.lws.notice.client.utils.NameSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.Print;
import at.brandl.lws.notice.client.utils.QuestionnairePanel;
import at.brandl.lws.notice.client.utils.ReadyListener;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.service.QuestionnaireService;
import at.brandl.lws.notice.shared.service.QuestionnaireServiceAsync;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireAnswersValidator;

public class Questionnaire extends VerticalPanel implements ChangeListener,
		ReadyListener {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final QuestionnaireServiceAsync questionnaireService = (QuestionnaireServiceAsync) GWT
			.create(QuestionnaireService.class);

	private final DateBox dateBox;
	private final PopUp dialogBox;
	private final NameSelection nameSelection;
	private final FormSelection formSelection;
	private final Button sendButton;
	private final Button newButton;
	private final Button printButton;
	// private final Button nameAddButton;
	private final DecisionBox decisionBox;
	private final QuestionnairePanel questionnairePanel;
	private final CheckBox archived;

	private final Map<String, GwtQuestionnaireAnswers> allAnswers = new HashMap<String, GwtQuestionnaireAnswers>();

	private boolean ready;
	private String childKey;
	private boolean changes;
	private boolean formSelectionReady;

	public Questionnaire() {

		dialogBox = new PopUp();
		decisionBox = new DecisionBox();
		nameSelection = new NameSelection(dialogBox);
		formSelection = new FormSelection(dialogBox, this);
		dateBox = new DateBox();
		sendButton = new Button(labels.save());
		newButton = new Button(labels.cancel());
		printButton = new Button(labels.print());
		questionnairePanel = new QuestionnairePanel(this);
		archived = new CheckBox(labels.archived());

		init();
		updateState();
		layout();
	}

	private void init() {

		nameSelection.addSelectionHandler(new SelectionHandler<Suggestion>() {

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {

				reload();
				updateState();
			}

		});

		formSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {

				updateQuestionnairePanel();
				updateState();
			}
		});

		dateBox.setValue(new Date());
		dateBox.setFormat(Utils.DATEBOX_FORMAT);
		dateBox.setFireNullValues(true);

		archived.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nameSelection.setIncludeArchived(archived.getValue());
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
				storeAnswers();
				disableAll();
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

		printButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if (questionnaireIsShown()) {
					String html = Utils.createPrintQuestionnaire(questionnairePanel.getQuestionnaire(), questionnairePanel.getAnswers());
//					dialogBox.setErrorMessage(html);
//					dialogBox.show();
					Print.it(html);
				}
			}

		});
	}

	private void layout() {

		setSpacing(Utils.SPACING);

		nameSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT - 12 + Utils.PIXEL);
		formSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT + Utils.PIXEL);
		dateBox.setSize(Utils.DATEBOX_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				- 12 + Utils.PIXEL);

		Panel panel = new ScrollPanel(questionnairePanel);
		panel.setSize(Utils.QUESTIONNAIRE_WIDTH + Utils.PIXEL, Utils.APP_HEIGHT
				- 300 + Utils.PIXEL);
		panel.setStyleName("questionnaireScrollPanel", true);

		HorizontalPanel selections = new HorizontalPanel();
		selections.setSpacing(Utils.SPACING);
		selections.add(nameSelection);
		selections.add(formSelection);
		selections.add(dateBox);

		VerticalPanel controls = new VerticalPanel();
		add(controls);

		controls.add(selections);
		controls.add(archived);

		add(panel);

		final Panel buttonContainer = createButtonContainer();
		Utils.formatCenter(this, buttonContainer);
	}

	private void resetForm() {

		childKey = null;
		allAnswers.clear();
		nameSelection.reset();
		formSelection.reset();
		dateBox.setValue(new Date());
		questionnairePanel.reset();
		changes = false;
		ready = false;
		formSelectionReady = false;
	}

	private void reload() {
		String selectedChildKey = nameSelection.getSelectedChildKey();
		if (selectedChildKey != null) {

			childKey = selectedChildKey;
			allAnswers.clear();
			formSelection.reset();
			questionnairePanel.reset();
			changes = false;
			ready = false;
			formSelectionReady = false;
			fetchQuestionnaireAnswers();
			formSelection.setChildKey(childKey);
		}
	}

	private void fetchQuestionnaireAnswers() {

		questionnaireService.getQuestionnaireAnswers(childKey,
				new AsyncCallback<Collection<GwtQuestionnaireAnswers>>() {

					@Override
					public void onSuccess(
							Collection<GwtQuestionnaireAnswers> result) {

						for (GwtQuestionnaireAnswers answers : result) {
							allAnswers.put(answers.getQuestionnaireKey(),
									answers);
						}
						ready = true;
						updateState();
					}

					@Override
					public void onFailure(Throwable caught) {
						displayErrorMessage();
					}
				});
	}

	private void updateQuestionnairePanel() {

		GwtQuestionnaire selectedForm = formSelection.getSelectedForm();
		if (selectedForm == null) {
			return;
		}

		String questionnaireKey = selectedForm.getKey();
		GwtQuestionnaireAnswers answers = allAnswers.get(questionnaireKey);
		if (answers == null) {
			answers = new GwtQuestionnaireAnswers();
			answers.setQuestionnaireKey(questionnaireKey);
			answers.setChildKey(childKey);
		}

		questionnairePanel.setQuestionnaire(selectedForm, answers);
	}

	private Panel createButtonContainer() {

		final Grid buttonContainer = new Grid(1, 3);

		sendButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		newButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		printButton.setSize(Utils.BUTTON_WIDTH + Utils.PIXEL, Utils.ROW_HEIGHT
				+ Utils.PIXEL);

		buttonContainer.setWidget(0, 0, sendButton);
		buttonContainer.setWidget(0, 1, newButton);
		buttonContainer.setWidget(0, 2, printButton);

		return buttonContainer;
	}

	private void storeAnswers() {

		if (!changes) {
			return;
		}

		final GwtQuestionnaireAnswers answers = questionnairePanel.getAnswers();
		
		Date date = dateBox.getValue();
		if (date == null) {
			date = new Date();
		}
		
		for (GwtAnswer answer : answers.getAnswers()) {
			if (answer.isUpdated()) {
				answer.setDate(date);
			}
		}

		if (!GwtQuestionnaireAnswersValidator.valid(answers)) {
			return;
		}
		
		questionnaireService.storeQuestionnaireAnswers(answers,
				new AsyncCallback<GwtQuestionnaireAnswers>() {

					@Override
					public void onFailure(Throwable caught) {
						displayErrorMessage();
					}

					@Override
					public void onSuccess(GwtQuestionnaireAnswers answers) {
						changes = false;
						allAnswers.put(answers.getQuestionnaireKey(), answers);
						questionnairePanel.setAnswers(answers);
						updateState();
					}
				});
	}

	private void disableAll() {
		nameSelection.setEnabled(false);
		formSelection.setEnabled(false);
		sendButton.setEnabled(false);
		newButton.setEnabled(false);

	}

	private void displayErrorMessage() {

		dialogBox.setErrorMessage();
		dialogBox.setDisableWhileShown(sendButton);
		dialogBox.center();
	}

	private void updateState() {

		nameSelection.setEnabled(true);
		formSelection.setEnabled(enableFormSelection());
		sendButton.setEnabled(enableSend());
		newButton.setEnabled(true);
		printButton.setEnabled(questionnaireIsShown());
	}

	private boolean questionnaireIsShown() {

		return questionnairePanel.iterator().hasNext();
	}

	private boolean enableFormSelection() {

		return childKey != null && ready && formSelectionReady;
	}

	private boolean enableSend() {

		return changes;
	}

	@Override
	public void notifyChange() {

		if (!changes) {
			changes = true;
			updateState();
		}
	}

	@Override
	public void notifyReady() {

		if (!formSelectionReady) {
			formSelectionReady = true;
			updateState();
		}
	}

}
