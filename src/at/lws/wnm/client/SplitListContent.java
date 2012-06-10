package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class SplitListContent extends HorizontalPanel implements ChangeHandler, ClickHandler {
	private final WahrnehmungsServiceAsync wahrnehmungService = GWT
			.create(WahrnehmungsService.class);
	private final ListContent listContent;
	private final ListBox listBox;

	public SplitListContent() {

		setSize("100%", "550px");
		setSpacing(10);

		listBox = new ListBox();
		listBox.setWidth("200px");
		listBox.setVisibleItemCount(28);
		listBox.addClickHandler(this);
		add(listBox);

		listContent = new ListContent("530px");
		listContent.getNameSelection().getTextBox().addChangeHandler(this);
		listContent.getSectionSelection().addChangeHandler(this);
		add(listContent);
	}

	@Override
	public void onChange(ChangeEvent event) {
		listBox.clear();
		fillListBox();

	}

	private void fillListBox() {
		final Long childNo = listContent.getSelectedChildKey();
		final Long sectionNo = listContent.getSelectedSectionKey();
		if (childNo != null || sectionNo != null) {
			wahrnehmungService.getBeobachtungen(childNo, sectionNo,
					new AsyncCallback<List<GwtBeobachtung>>() {

						@Override
						public void onSuccess(List<GwtBeobachtung> result) {
							for(GwtBeobachtung beobachtung : result)
							{
								listBox.addItem(formatBeobachtung(beobachtung), beobachtung.getKey().toString());
							}
						}

						private String formatBeobachtung(
								GwtBeobachtung beobachtung) {
							return beobachtung.getChildName() + " in " + beobachtung.getSectionName() + " am " + Utils.DATE_FORMAT.format(beobachtung.getDate());
						}

						@Override
						public void onFailure(Throwable caught) {
							listContent.getDialogBox().setErrorMessage();
							listContent.getDialogBox().center();
						}
					});
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		final int selectedIndex = listBox.getSelectedIndex();
		if(selectedIndex < 0 )
		{
			return;
		}
		final Long beobachtungsKey = Long.valueOf(listBox.getValue(selectedIndex));
		wahrnehmungService.getBeobachtung(beobachtungsKey, new AsyncCallback<GwtBeobachtung>() {

			@Override
			public void onFailure(Throwable caught) {
				listContent.getDialogBox().setErrorMessage();
				listContent.getDialogBox().center();
			}

			@Override
			public void onSuccess(GwtBeobachtung result) {
				listContent.getTextArea().setText(result.getText());
			}
		});
	}
}
