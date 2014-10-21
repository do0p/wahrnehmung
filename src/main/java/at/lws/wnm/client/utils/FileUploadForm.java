package at.lws.wnm.client.utils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FileUploadForm extends FormPanel {

	public FileUploadForm(String action) {
		setAction(action);

		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		final VerticalPanel panel = new VerticalPanel();
		setWidget(panel);

		final FileUpload upload = new FileUpload();
		upload.setName("uploadFormElement");
		panel.add(upload);

		panel.add(new Button("Submit", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FileUploadForm.this.submit();
			}
		}));

		addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				Window.Location.reload();
				Window.alert(event.getResults());
			}
		});
	}

}
