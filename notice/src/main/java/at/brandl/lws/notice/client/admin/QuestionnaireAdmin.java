package at.brandl.lws.notice.client.admin;

import java.util.List;

import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormPrinter;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextArea;

public class QuestionnaireAdmin extends AbstractAdminTab {

	private final FormServiceAsync formService = GWT.create(FormService.class);

	private TextArea textBox;
	private SectionSelection sectionSelection;

	private PopUp dialogBox;
	private FormPrinter formPrinter;

	QuestionnaireAdmin() {
		super(false);
		
		formPrinter = new FormPrinter();
		dialogBox = new PopUp();

		sectionSelection = new SectionSelection(dialogBox);
		
		textBox = new TextArea();
		textBox.setSize("500px", "500px");

		setSpacing(Utils.SPACING);
		add(createSelectionContainer());
		add(textBox);
		add(getButtonPanel());
	}

	@Override
	void save() {
		
		formService.storeFormAsString(textBox.getText(), sectionSelection.getSelectedSectionKey(),
				new ErrorReportingCallback<GwtQuestionnaire>() {
					@Override
					public void onSuccess(GwtQuestionnaire result) {
						textBox.setText(formPrinter.toString(result));
						sectionSelection.setSelected(result.getSection());
					}
				});
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
}
