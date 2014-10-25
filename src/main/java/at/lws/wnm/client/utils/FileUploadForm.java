package at.lws.wnm.client.utils;

import at.lws.wnm.client.Labels;
import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.client.service.WahrnehmungsServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;

public class FileUploadForm extends FormPanel {
	
	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final WahrnehmungsServiceAsync wahrnehmungService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);
	
	private final FileUpload upload;
	private final Button button;
	private final ListBox fileNames;
	private boolean actionSet;

	public FileUploadForm() {

		upload = new FileUpload();
		button = new Button(labels.save());
		fileNames = new ListBox();

		layout();

		init();
	}

	private void init() {
		upload.setName("file");
		upload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateButtonState();
			}
		});
		button.setEnabled(false);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				actionSet = false;
				updateButtonState();
				submit();
			
			}
		});

		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);
		addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				setAction();
				parseNames(event.getResults());
			}

		});

		setAction();
	}

	private void setAction() {
		wahrnehmungService.getFileUploadUrl(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {	
			}

			@Override
			public void onSuccess(String url) {
				setAction(url);
			}
		});
	}

	private void layout() {
		final Panel panel = new HorizontalPanel();
		panel.add(fileNames);
		panel.add(upload);
		panel.add(button);
		setWidget(panel);
	}

	private void parseNames(String results) {
		String[] names = results.split("\n");
		for (String name : names) {
			String[] parts = name.split(":");
			fileNames.addItem(parts[0]);
		}
	}

	@Override
	public void setAction(String url) {
		super.setAction(url);
		actionSet = true;
		updateButtonState();
	}

	public ListBox getFileNames() {
		return fileNames;
	}

	private void updateButtonState() {
		boolean enabled = Utils.isNotEmpty(upload.getFilename())
				&& actionSet;
		button.setEnabled(enabled);
	}

}
