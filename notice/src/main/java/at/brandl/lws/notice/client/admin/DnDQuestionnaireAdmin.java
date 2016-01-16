package at.brandl.lws.notice.client.admin;

import java.util.List;

import at.brandl.lws.notice.client.utils.PopUp;
import at.brandl.lws.notice.client.utils.SectionSelection;
import at.brandl.lws.notice.client.utils.SectionSelectionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DnDQuestionnaireAdmin extends AbstractAdminTab {

	private final FormServiceAsync formService = GWT.create(FormService.class);

	private VerticalPanel panel;
	private SectionSelection sectionSelection;

	private PopUp dialogBox;

	private int count = 1;

	// private FormPrinter formPrinter;

	DnDQuestionnaireAdmin() {
		super(false);

		// formPrinter = new FormPrinter();
		dialogBox = new PopUp();

		sectionSelection = new SectionSelection(dialogBox);

		panel = new VerticalPanel();
		panel.add(createLabel("- ende -", false));
		panel.setWidth("500px");


		setSpacing(Utils.SPACING);
		add(createSelectionContainer());
		add(panel);

		createTestSetup();
		add(getButtonPanel());
	}

	private Label createLabel(String text, boolean dragable) {
		final Label label = new Label(text);

		if (dragable) {
			label.getElement().setDraggable(Element.DRAGGABLE_TRUE);
			label.addDragStartHandler(new DragStartHandler() {
				@Override
				public void onDragStart(DragStartEvent event) {
					// Required: set data for the event.
					event.setData("text", label.getText());
					// Optional: show a copy of the widget under cursor.
					event.getDataTransfer().setDragImage(label.getElement(),
							10, 10);
					// label.removeFromParent();
				}
			});
			label.addDragEndHandler(new DragEndHandler() {

				@Override
				public void onDragEnd(DragEndEvent event) {
					label.removeFromParent();
					// panel.getElement().getStyle().setBackgroundColor("#fff");
				}
			});
		}
		label.addDomHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
				label.getElement().getStyle().setBackgroundColor("#ffa");
			}

		}, DragOverEvent.getType());
		label.addDomHandler(new DragLeaveHandler() {

			@Override
			public void onDragLeave(DragLeaveEvent event) {
				label.getElement().getStyle().setBackgroundColor("#fff");
			}
		}, DragLeaveEvent.getType());
		label.addDomHandler(new DropHandler() {

			@Override
			public void onDrop(DropEvent event) {

				Label newLabel = createLabel(event.getData("text"), true);
				panel.insert(newLabel, panel.getWidgetIndex(label));
				label.getElement().getStyle().setBackgroundColor("#fff");
			
			}

		}, DropEvent.getType());

		return label;
	}

	private void createTestSetup() {
		final Label label = new Label("test");
		add(label);
		label.getElement().setDraggable(Element.DRAGGABLE_TRUE);
		label.addDragStartHandler(new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {
				// Required: set data for the event.
				event.setData("text", "Hello World " + count++);
				// Optional: show a copy of the widget under cursor.
				event.getDataTransfer()
						.setDragImage(label.getElement(), 10, 10);
			}

		});
		
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
}
