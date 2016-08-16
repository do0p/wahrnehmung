package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.brandl.lws.notice.client.utils.ChangeableLabel;
import at.brandl.lws.notice.client.utils.Data;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.DragContainer;
import at.brandl.lws.notice.client.utils.DragTargetLabel;
import at.brandl.lws.notice.client.utils.DragTemplate;
import at.brandl.lws.notice.client.utils.DragableQuestion;
import at.brandl.lws.notice.client.utils.DragableQuestionGroup;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireValidator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class DnDQuestionnaireAdmin extends AbstractAdminTab {

	private final FormServiceAsync formService = GWT.create(FormService.class);
	private final Map<String, GwtQuestionnaire> questionnaires = new HashMap<String, GwtQuestionnaire>();

	private final DecisionBox decisionBox;

	private GwtQuestionnaire questionnaire;
	private DragContainer panel;
	private SectionSelection sectionSelection;
	private PopUp dialogBox;
	private ListBox questionnaireListbox;

	Logger logger = Logger.getLogger("DnDQuestionnaireAdmin");

	DnDQuestionnaireAdmin() {
		super(false);

		dialogBox = new PopUp();

		decisionBox = new DecisionBox();
		decisionBox.setText(labels().questionDelWarning());

		sectionSelection = new SectionSelection(dialogBox);

		questionnaire = new GwtQuestionnaire();

		initPanel();

		Grid selectionContainer = createSelectionContainer();
		questionnaireListbox = new ListBox();
		initQuestionnaireListbox();
		questionnaireListbox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = questionnaireListbox.getSelectedIndex();
				if (selectedIndex >= 0) {
					GwtQuestionnaire gwtQuestionnaire = questionnaires
							.get(questionnaireListbox.getValue(selectedIndex));
					if (gwtQuestionnaire != null) {
						questionnaire = gwtQuestionnaire;
						sectionSelection.setSelected(questionnaire.getSection());
						load();
					} else {
						sectionSelection.reset();
					}
				}
			}

		});

		sectionSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				questionnaire.setSection(sectionSelection
						.getSelectedSectionKey());
			}
		});

		setSpacing(Utils.SPACING);
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(questionnaireListbox);
		horizontalPanel.add(selectionContainer);
		add(horizontalPanel);
		add(panel);

		createTestSetup();
		add(getButtonPanel());

		formService.getAllForms(new AsyncCallback<List<GwtQuestionnaire>>() {

			@Override
			public void onSuccess(List<GwtQuestionnaire> result) {
				initQuestionnaireListbox();
				for (GwtQuestionnaire questionnaire : result) {
					questionnaires.put(questionnaire.getKey(), questionnaire);
					questionnaireListbox.addItem(questionnaire.getTitle(),
							questionnaire.getKey());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private void initPanel() {
		panel = new DragContainer(decisionBox);
		panel.add(new ChangeableLabel("Titel"));
		panel.add(DragTargetLabel.valueOf("-- ende --", panel));
		panel.setWidth("500px");
	}

	private void initQuestionnaireListbox() {
		questionnaireListbox.clear();
		questionnaireListbox.addItem("choose a questionnaire", "nope");
	}

	private void createTestSetup() {
		add(DragTemplate.createQuestionTemplate());
		add(DragTemplate.createQuestionGroupTemplate());
	}

	@Override
	void save() {
		logger.log(Level.INFO, "in save - info");

		// store current state of questionnaire
		Map<String, GwtQuestionGroup> groupMap = questionnaire.getAllGroups();
		Map<String, GwtQuestion> questionMap = questionnaire.getAllQuestions();
		List<GwtQuestionGroup> ungroupedQuestionGroups = getGroupsWithoutTitle(groupMap);
		Iterator<GwtQuestionGroup> ungroupedQuestionGroupsIterator = ungroupedQuestionGroups
				.iterator();

		questionnaire.clear();

		ChangeableLabel title = (ChangeableLabel) panel.getWidget(0);
		questionnaire.setTitle(title.getText());

		GwtQuestionGroup ungroupedQuestionsGroup = null;
		for (int i = 1; i < panel.getWidgetCount(); i++) {
			
			Widget widget = panel.getWidget(i);
			if (widget instanceof DragableQuestionGroup) {

				if (ungroupedQuestionsGroup != null) {
					ungroupedQuestionsGroup = null;
				}

				DragableQuestionGroup group = (DragableQuestionGroup) widget;
				GwtQuestionGroup gwtGroup = convertQuestionGroup(group,
						groupMap);
				questionnaire.addQuestionGroup(gwtGroup);

				for (DragableQuestion question : group.getQuestions()) {
					GwtQuestion gwtQuestion = convertQuestion(question,
							questionMap);
					gwtGroup.addQuestion(gwtQuestion);
				}


			} else if (widget instanceof DragableQuestion) {

				if (ungroupedQuestionsGroup == null) {
					if (ungroupedQuestionGroupsIterator.hasNext()) {
						ungroupedQuestionsGroup = ungroupedQuestionGroupsIterator
								.next();
					} else {
						ungroupedQuestionsGroup = new GwtQuestionGroup();
					}
					
					questionnaire.addQuestionGroup(ungroupedQuestionsGroup);
				}

				GwtQuestion gwtQuestion = convertQuestion(
						(DragableQuestion) widget, questionMap);
				ungroupedQuestionsGroup.addQuestion(gwtQuestion);
			}
		}

		logger.log(Level.INFO, "before store");

		if (GwtQuestionnaireValidator.valid(questionnaire)) {
			formService.storeForm(questionnaire,
					new ErrorReportingCallback<GwtQuestionnaire>() {
						@Override
						public void onSuccess(GwtQuestionnaire result) {
							questionnaire = result;
							sectionSelection.setSelected(result.getSection());
							load();
						}
					});
		} else {
			logger.log(Level.INFO, "questionnaire is invalid");
		}
	}

	private List<GwtQuestionGroup> getGroupsWithoutTitle(
			Map<String, GwtQuestionGroup> groupMap) {
		List<GwtQuestionGroup> groupsWithoutTitle = new ArrayList<>();
		for (GwtQuestionGroup group : new ArrayList<>(groupMap.values())) {
			if (at.brandl.lws.notice.shared.Utils.isEmpty(group.getTitle())) {
				groupsWithoutTitle.add(group);
			}
		}
		return groupsWithoutTitle;
	}

	private GwtQuestionGroup convertQuestionGroup(DragableQuestionGroup group,
			Map<String, GwtQuestionGroup> groupMap) {

		String groupKey = group.getKey();
		GwtQuestionGroup gwtGroup;
		if (isKeyNew(groupKey)) {
			gwtGroup = new GwtQuestionGroup();
		} else if (groupMap.containsKey(groupKey)) {
			gwtGroup = groupMap.get(groupKey);
		} else {
			throw new IllegalStateException("no group for key " + groupKey);
		}
		gwtGroup.setTitle(group.getTitle());
		return gwtGroup;
	}

	private GwtQuestion convertQuestion(DragableQuestion question,
			Map<String, GwtQuestion> questionMap) {

		String questionKey = question.getKey();
		GwtQuestion gwtQuestion;
		if (isKeyNew(questionKey)) {
			gwtQuestion = new GwtQuestion();
			gwtQuestion.setTemplate(createDefaultTemplate());
		} else if (questionMap.containsKey(questionKey)) {
			gwtQuestion = questionMap.get(questionKey);
		} else {
			throw new IllegalStateException("no question for key "
					+ questionKey);
		}
		gwtQuestion.setLabel(question.getLabel());
		return gwtQuestion;
	}

	private GwtAnswerTemplate createDefaultTemplate() {
		GwtMultipleChoiceAnswerTemplate template = new GwtMultipleChoiceAnswerTemplate();
		template.addOption(createMultipleChoiceOption("kennen gelernt", "k"));
		template.addOption(createMultipleChoiceOption("im Üben","u"));
		template.addOption(createMultipleChoiceOption("sicher","s"));
		return template;
	}

	private GwtMultipleChoiceOption createMultipleChoiceOption(
			String label, String value) {
		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setValue(value);
		option.setLabel(label);
		return option;
	}

	private boolean isKeyNew(String key) {
		return key.startsWith(DragTemplate.NEW_PREFIX);
	}

	private Grid createSelectionContainer() {
		final List<SectionSelectionBox> sectionSelectionBoxes = sectionSelection
				.getSectionSelectionBoxes();
		final Grid selectionContainer = new Grid(1,
				sectionSelectionBoxes.size());
		int i = 0;
		for (SectionSelectionBox sectionSelectionBox : sectionSelectionBoxes) {
			sectionSelectionBox.setSize(Utils.LISTBOX_WIDTH + Utils.PIXEL,
					Utils.ROW_HEIGHT + Utils.PIXEL);
			selectionContainer.setWidget(0, i++, sectionSelectionBox);
		}
		return selectionContainer;
	}

	private void load() {
		panel.clear();
		int index = 0;
		panel.insert(new ChangeableLabel(questionnaire.getTitle()), index++);
		for (GwtQuestionGroup group : questionnaire.getGroups()) {
			String title = group.getTitle();
			if (at.brandl.lws.notice.shared.Utils.isNotEmpty(title)) {
				DragableQuestionGroup questionGroup = DragableQuestionGroup
						.valueOf(new Data(group.getKey(), title), panel);
				for (GwtQuestion question : group.getQuestions()) {
					questionGroup.addQuestion(new Data(question.getKey(),
							question.getLabel()));
				}
				panel.insert(questionGroup, index++);
			} else {
				for (GwtQuestion question : group.getQuestions()) {
					panel.insert(DragableQuestion.valueOf(
							new Data(question.getKey(), question.getLabel()),
							panel), index++);
				}
			}
		}
	}
}
