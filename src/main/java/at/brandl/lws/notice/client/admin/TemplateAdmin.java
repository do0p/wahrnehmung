package at.brandl.lws.notice.client.admin;

import at.brandl.lws.notice.client.utils.TextField;
import at.brandl.lws.notice.shared.model.GwtTemplate;
import at.brandl.lws.notice.shared.service.TemplateService;
import at.brandl.lws.notice.shared.service.TemplateServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TemplateAdmin extends AbstractModelAdminTab<GwtTemplate> {

	private TextField textField;
	private TextBox name;

	TemplateAdmin() {
		super(true, (TemplateServiceAsync) GWT.create(TemplateService.class));

	}

	@Override
	protected int getListCount() {
		return 10;
	}

	@Override
	protected String getDelWarning() {
		return labels().templateDelWarning();
	}

	@Override
	protected GwtTemplate createModel() {
		return new GwtTemplate();
	}

	@Override
	protected void updateFields() {
		name.setText(getModel().getName());
		textField.setText(getModel().getTemplate());
	}

	@Override
	protected String getKey(GwtTemplate template) {
		return template.getKey();
	}

	@Override
	protected String getDisplayName(GwtTemplate template) {
		return template.getName();
	}

	@Override
	protected Widget createContentLayout() {
		Grid nameInput = new Grid(1, 2);
		nameInput.setWidget(0, 0, new Label(labels().name()));
		nameInput.setWidget(0, 1, name);

		final VerticalPanel data = new VerticalPanel();
		data.add(nameInput);
		data.add(textField);

		return data;
	}

	@Override
	protected void init() {
		textField = new TextField();
		textField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				getModel().setTemplate(textField.getText());
				updateButtonPanel();
			}
		});
		textField.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				getModel().setTemplate(textField.getText());
				updateButtonPanel();
			}
		});

		name = new TextBox();
		name.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				getModel().setName(name.getText());
				updateButtonPanel();
			}
		});
		name.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				getModel().setName(name.getText());
				updateButtonPanel();
			}
		});

	}

}
