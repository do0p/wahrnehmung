package at.brandl.lws.notice.client.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

public class FormSelection extends ListBox {

	private final PopUp dialogBox;

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final FormServiceAsync formService = (FormServiceAsync) GWT
			.create(FormService.class);
	private Map<String, GwtQuestionnaire> forms = new HashMap<String, GwtQuestionnaire>();


	private String childKey;

	private ReadyListener readyListener;

	public FormSelection(PopUp dialogBox, ReadyListener readyListener) {
		this.dialogBox = dialogBox;
		this.readyListener = readyListener;
		updateFormMap();
	}

	public GwtQuestionnaire getSelectedForm() {
		
		final int index = getSelectedIndex();
		if (index != -1) {
			return forms.get(getValue(index));
		}
		return null;
	}

	public void setSelectedForm(GwtQuestionnaire form) {
		
		for(int i = 1; i < getItemCount(); i++) {
			if(getValue(i).equals(form.getTitle())) {
				setSelectedIndex(i);
			}
		}
	}
	
	public void updateFormMap() {

		formService.getAllForms(childKey, new AsyncCallback<List<GwtQuestionnaire>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtQuestionnaire> result) {
				forms.clear();
				clear();
				addItem("- "+labels.chooseAForm()+" -");
				for (GwtQuestionnaire form : result) {
					forms.put(form.getTitle(), form);
					addItem(form.getTitle());
				}
				readyListener.notifyReady();
			}
		});
	}


	public void setChildKey(String childKey) {

		this.childKey = childKey;
		updateFormMap();
	}

	public void reset() {
		
		setSelectedIndex(0);
		childKey = null;
	}

}
