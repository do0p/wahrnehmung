package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import at.brandl.lws.notice.client.utils.ChangeListener;
import at.brandl.lws.notice.client.utils.ChangeableLabel;
import at.brandl.lws.notice.client.utils.Data;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.DragContainer;
import at.brandl.lws.notice.client.utils.DragTargetLabel;
import at.brandl.lws.notice.client.utils.DragTemplate;
import at.brandl.lws.notice.client.utils.DragableQuestion;
import at.brandl.lws.notice.client.utils.DragableQuestionGroup;
import at.brandl.lws.notice.client.utils.FormSelection;
import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.ReadyListener;
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

public class DnDQuestionnaireAdmin extends AbstractAdminTab implements
		ReadyListener, ChangeListener {

	private static final Logger LOGGER = Logger
			.getLogger("DnDQuestionnaireAdmin");
	private final FormServiceAsync formService = GWT.create(FormService.class);

	private final DecisionBox decisionBox;

	private GwtQuestionnaire questionnaire;
	private DragContainer panel;
	private SectionSelection sectionSelection;
	private PopUp dialogBox;
	private FormSelection formSelection;
	private boolean changes;

	private static int instanceCount;
	private int instanceNo;

	DnDQuestionnaireAdmin() {

		super(false);

		instanceNo = instanceCount++;

		dialogBox = new PopUp();
		decisionBox = new DecisionBox();
		decisionBox.setText(labels().questionDelWarning());
		sectionSelection = new SectionSelection(dialogBox);
		questionnaire = new GwtQuestionnaire();
		panel = new DragContainer(decisionBox);

		initPanel();

		panel.registerChangeListener(this);

		formSelection = new FormSelection(dialogBox, this);
		formSelection.setSize(Utils.NAMESELECTION_WIDTH + Utils.PIXEL,
				Utils.ROW_HEIGHT + Utils.PIXEL);

		formSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				updatePanel();
				updateButtonPanel();
			}
		});

		sectionSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				questionnaire.setSection(sectionSelection
						.getSelectedSectionKey());
				updateButtonPanel();
				changes = true;
			}
		});

		setSpacing(Utils.SPACING);
		add(createSelectionContainer(formSelection, sectionSelection));

		CellPanel content = new HorizontalPanel();
		content.setWidth("100%");
		content.add(panel);
		content.setSpacing(Utils.SPACING);
		panel.setWidth(Utils.QUESTIONNAIRE_WIDTH + Utils.PIXEL);
		CellPanel templates = new VerticalPanel();
		templates.setSpacing(Utils.SPACING);

		DragTemplate questionTemplate = DragTemplate
				.createQuestionTemplate(labels().questionLabel());
		questionTemplate.setStyleName("roundedBox");
		templates.add(questionTemplate);
		DragTemplate questionGroupTemplate = DragTemplate
				.createQuestionGroupTemplate(labels().questionGroupLabel());
		questionGroupTemplate.setStyleName("roundedBox");
		templates.add(questionGroupTemplate);
		content.add(templates);
		add(content);

		add(getButtonPanel());
		updateButtonPanel();
	}

	@Override
	void reset() {
		initPanel();
		updatePanel();
		updateButtonPanel();
		changes = false;
	}

	@Override
	boolean enableSave() {
		return changes
				&& panel.getWidgetCount() > 2
				&& at.brandl.lws.notice.shared.Utils
						.isNotEmpty(sectionSelection.getSelectedSectionKey());
	}

	@Override
	boolean enableCancel() {
		return changes && panel.getWidgetCount() > 2;
	}

	@Override
	void save() {
		LOGGER.log(Level.INFO, "in save - info");

		// store current state of questionnaire
		updateQuestionnaire(panel, questionnaire);

		LOGGER.log(Level.INFO, "before store");

		if (GwtQuestionnaireValidator.valid(questionnaire)) {
			formService.storeForm(questionnaire,
					new ErrorReportingCallback<GwtQuestionnaire>() {
						@Override
						public void onSuccess(GwtQuestionnaire result) {
							getButtonPanel().setSaveButtonLabel(
									labels().change());
							questionnaire = result;
							sectionSelection.setSelected(result.getSection());
							load();
							changes = false;
							formSelection.updateFormMap();
						}
					});
		} else {
			LOGGER.log(Level.INFO, "questionnaire is invalid");
		}
	}

	private void updateQuestionnaire(DragContainer panel,
			GwtQuestionnaire questionnaire) {

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
	}

	@Override
	public void notifyReady() {

		formSelection.setSelectedForm(questionnaire);
		updateState();
		updateButtonPanel();
	}

	private void initPanel() {
		panel.add(new ChangeableLabel("Titel", "questionnaireHeading"));
		panel.add(DragTargetLabel.valueOf("~~~~", panel));
		panel.setStyleName("questionnaireScrollPanel");
		changes = false;
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
		template.addOption(createMultipleChoiceOption("im Üben", "u"));
		template.addOption(createMultipleChoiceOption("sicher", "s"));
		return template;
	}

	private GwtMultipleChoiceOption createMultipleChoiceOption(String label,
			String value) {
		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setValue(value);
		option.setLabel(label);
		return option;
	}

	private boolean isKeyNew(String key) {
		return key.startsWith(DragTemplate.NEW_PREFIX);
	}

	private Grid createSelectionContainer(Widget formSelection,
			SectionSelection sectionSelection) {
		final List<SectionSelectionBox> sectionSelectionBoxes = sectionSelection
				.getSectionSelectionBoxes();
		final Grid selectionContainer = new Grid(1,
				sectionSelectionBoxes.size() + 1);
		selectionContainer.setWidget(0, 0, formSelection);
		int i = 1;
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
		panel.insert(new ChangeableLabel(questionnaire.getTitle(),
				"questionnaireHeading"), index++);
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
		panel.add(DragTargetLabel.valueOf("~~~~", panel));
	}

	private void updatePanel() {
		GwtQuestionnaire gwtQuestionnaire = formSelection.getSelectedForm();
		if (gwtQuestionnaire != null) {
			questionnaire = gwtQuestionnaire;
			sectionSelection.setSelected(questionnaire.getSection());
			load();
			getButtonPanel().setSaveButtonLabel(labels().change());
		} else {
			questionnaire.clear();
			questionnaire.setKey(null);
			questionnaire.setSection(null);
			sectionSelection.reset();
			panel.clear();
			initPanel();
			getButtonPanel().setSaveButtonLabel(labels().create());
		}
		changes = false;
	}

	private void updateState() {
		updatePanel();
	}

	@Override
	public void notifyChange() {
		LOGGER.log(Level.INFO, "got notified of changes");
		changes = true;
		updateButtonPanel();
	}

	@Override
	public String toString() {

		return "DnDQuestionnaireAdmin" + instanceNo;
	}
}
