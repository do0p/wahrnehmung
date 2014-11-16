package at.brandl.lws.notice.client.admin;

import java.util.List;

import at.brandl.lws.notice.client.admin.AbstractAdminTab.ErrorReportingCallback;
import at.brandl.lws.notice.client.service.TemplateService;
import at.brandl.lws.notice.client.service.TemplateServiceAsync;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.TextField;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.model.GwtTemplate;
import at.brandl.lws.notice.shared.validator.GwtTemplateValidator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TemplateAdmin extends AbstractAdminTab {

	private final TemplateServiceAsync templateService = GWT
			.create(TemplateService.class);

	private final TextField textField;
	private final ListBox templates;
	private final TextBox name;


	private final DecisionBox decisionBox;
	
	private GwtTemplate template;
	private boolean changed;

	TemplateAdmin() {
		super(true);
		decisionBox = new DecisionBox();
		decisionBox.setText(labels().templateDelWarning());
		template = new GwtTemplate();
		name = new TextBox();
		textField = new TextField();
		templates = new ListBox();
		templates.setVisibleItemCount(10);
		layout();
		init();
		rebuildTemplateList();
		updateButtonPanel();
	}

	private void rebuildTemplateList() {
		templateService
				.getTemplates(new ErrorReportingCallback<List<GwtTemplate>>() {

					@Override
					public void onSuccess(List<GwtTemplate> result) {
						templates.clear();
						for (GwtTemplate template : result) {
							templates.addItem(template.getName(),
									template.getKey());
						}
					}

				});
	}

	private void init() {
		textField.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				changed = true;
			}
		});
		textField.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				template.setTemplate(textField.getText());
				updateButtonPanel();
			}
		});
		textField.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				template.setTemplate(textField.getText());
				updateButtonPanel();
			}
		});
		name.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				changed = true;
			}
		});
		name.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				template.setName(name.getText());
				updateButtonPanel();
			}
		});
		name.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				template.setName(name.getText());
				updateButtonPanel();
			}
		});
		templates.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				select();
				updateButtonPanel();
			}
		});
	}

	private void select() {
		final int selectedIndex = templates.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		final String templateKey = templates.getValue(selectedIndex);
		templateService.getTemplate(templateKey, new ErrorReportingCallback<GwtTemplate>() {


			@Override
			public void onSuccess(GwtTemplate template) {
				TemplateAdmin.this.template = template;
				updateFields();
				updateButtonPanel();
				getButtonPanel().setSaveButtonLabel(labels().change());

			}

		
		});
	}
	
	private void updateFields() {
		name.setText(template.getName());
		textField.setText(template.getTemplate());
	}
	
	private void layout() {

		Grid nameInput = new Grid(1, 2);
		nameInput.setWidget(0, 0, new Label(labels().name()));
		nameInput.setWidget(0, 1, name);

		final VerticalPanel data = new VerticalPanel();
		data.add(nameInput);
		data.add(textField);
		data.add(getButtonPanel());

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(templates);

		add(root);
	}

	@Override
	void save() {
		templateService.storeTemplate(template,
				new ErrorReportingCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						rebuildTemplateList();
						reset();
					}
				});
	}

	@Override
	void reset() {
		name.setText("");
		textField.setText("");
		template = new GwtTemplate();
		getButtonPanel().setSaveButtonLabel(labels().save());
		changed = false;
	}
	
	@Override
	void delete() {
		if (template.getKey() != null) {
		
			decisionBox.addOkClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					templateService.deleteTemplate(template,
							new ErrorReportingCallback<Void>() {

								@Override
								public void onSuccess(Void result) {
									rebuildTemplateList();
									reset();
									updateButtonPanel();
								}
							});
				}
			});
			decisionBox.center();
		}
	}

	@Override
	boolean enableSave() {
		return GwtTemplateValidator.validate(template);
	}

	@Override
	boolean enableDelete() {
		return at.brandl.lws.notice.shared.Utils.isNotEmpty(template.getKey());
	}

	@Override
	boolean enableCancel() {
		return changed;
	}
}
