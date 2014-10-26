package at.lws.wnm.client.utils;

import java.util.HashMap;
import java.util.Map;

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
import com.google.gwt.user.client.ui.Panel;

public class FileUploadForm extends FormPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final WahrnehmungsServiceAsync wahrnehmungService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);

	private final FileUpload upload;
	private final Button button;
	private final Map<String, String> filenames;
	private final DecisionBox decisionBox;
	private final PopUp dialogBox;

	private boolean actionSet;

	public FileUploadForm() {

		upload = new FileUpload();
		button = new Button(labels.save());
		filenames = new HashMap<String, String>();
		decisionBox = new DecisionBox();
		dialogBox = new PopUp();

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
				final String filename = getFilename();
				wahrnehmungService.fileExists(filename,
						new AsyncCallback<Boolean>() {

							@Override
							public void onFailure(Throwable caught) {
								dialogBox.setErrorMessage(caught.getMessage());
							}

							@Override
							public void onSuccess(Boolean fileExists) {
								if (fileExists) {
									decisionBox.setText(labels
											.fileExistsWarning(filename));
									decisionBox
											.addOkClickHandler(new ClickHandler() {
												@Override
												public void onClick(
														ClickEvent event) {
													submit();
													decisionBox.hide();
												}
											});
									decisionBox.center();
								} else {
									submit();
								}
							}
						});

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

	@Override
	public void submit() {
		actionSet = false;
		updateButtonState();
		super.submit();
	}
	
	private void setAction() {
		wahrnehmungService.getFileUploadUrl(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(String url) {
				setAction(url);
			}
		});
	}

	private void layout() {
		final Panel panel = new HorizontalPanel();
		panel.add(upload);
		panel.add(button);
		setWidget(panel);
	}

	private void parseNames(String results) {
		String[] names = results.split("\n");
		for (String name : names) {
			String[] parts = name.split(":");
			filenames.put(parts[0], parts[1]);
		}
	}

	@Override
	public void setAction(String url) {
		super.setAction(url);
		actionSet = true;
		updateButtonState();
	}

	public Map<String, String> getFileNames() {
		return filenames;
	}

	private void updateButtonState() {
		boolean enabled = Utils.isNotEmpty(upload.getFilename()) && actionSet;
		button.setEnabled(enabled);
	}

	private String getFilename() {
		String filename = upload.getFilename();
		int pos = filename.lastIndexOf("\\");
		if(pos < 0) {
			pos = filename.lastIndexOf("/");
		}
		return filename.substring(pos + 1);
	}

	public void addAllFilenames(Map<String, String> filenames) {
		this.filenames.putAll(filenames);
		
	}
}
