package at.brandl.lws.notice.client.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class DnDQuestionnaireAdmin extends AbstractAdminTab {

	private final FormServiceAsync formService = GWT.create(FormService.class);
	private final Map<String, GwtQuestionnaire> questionnaires = new HashMap<String, GwtQuestionnaire>();

	private DragContainer panel;
	private SectionSelection sectionSelection;
	private PopUp dialogBox;
	private ListBox questionnaireListbox;
	private final DecisionBox decisionBox;


	// private FormPrinter formPrinter;

	DnDQuestionnaireAdmin() {
		super(false);

		// formPrinter = new FormPrinter();
		dialogBox = new PopUp();
		
		decisionBox = new DecisionBox();
		decisionBox.setText(labels().questionDelWarning());

		sectionSelection = new SectionSelection(dialogBox);

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
						sectionSelection.setSelected(gwtQuestionnaire
								.getSection());
						load(gwtQuestionnaire);
					} else {
						sectionSelection.reset();
					}
				}
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

		// formService.storeFormAsString(panel.getText(),
		// sectionSelection.getSelectedSectionKey(),
		// new ErrorReportingCallback<GwtQuestionnaire>() {
		// @Override
		// public void onSuccess(GwtQuestionnaire result) {
		// panel.setText(formPrinter.toString(result));
		// sectionSelection.setSelected(result.getSection());
		// }
		// });
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
	
	private void load(GwtQuestionnaire gwtQuestionnaire) {
		int index = 0;
		panel.insert(new ChangeableLabel(gwtQuestionnaire.getTitle()), index++);
		for(GwtQuestionGroup group : gwtQuestionnaire.getGroups()) {
			String title = group.getTitle();
			if(at.brandl.lws.notice.shared.Utils.isNotEmpty(title)) {
				DragableQuestionGroup questionGroup = DragableQuestionGroup.valueOf(new Data(group.getKey(), title), panel);
				for(GwtQuestion question : group.getQuestions()) {
					questionGroup.addQuestion(new Data(question.getKey(), question.getLabel()));
				}
				panel.insert(questionGroup, index++);
			} else {
				for(GwtQuestion question : group.getQuestions()) {
					panel.insert(DragableQuestion.valueOf(new Data(question.getKey(), question.getLabel()), panel), index++);
				}
			}
		}
	}
}
