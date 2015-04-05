package at.brandl.lws.notice.client.admin;

import at.brandl.lws.notice.shared.service.FormService;
import at.brandl.lws.notice.shared.service.FormServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TextArea;

public class QuestionnaireAdmin extends AbstractAdminTab {

	private final FormServiceAsync formService = GWT.create(FormService.class);

	private TextArea textBox;

	QuestionnaireAdmin() {
		super(false);

		textBox = new TextArea();
		textBox.setSize("500px", "500px");

		add(textBox);
		add(getButtonPanel());
	}

	@Override
	void save() {
		formService.storeFormAsString(textBox.getText(),
				new ErrorReportingCallback<String>() {
					@Override
					public void onSuccess(String result) {
						textBox.setText(result);
					}
				});
	}

}
