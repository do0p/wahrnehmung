package at.brandl.lws.notice.client.utils;

import java.util.List;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.service.TemplateService;
import at.brandl.lws.notice.client.service.TemplateServiceAsync;
import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class TemplateSelection extends HorizontalPanel {

	private final Labels labels = GWT.create(Labels.class);
	private final Label label;
	private final ListBox templates;
	private final Button button;

	private final TemplateServiceAsync templateService = GWT
			.create(TemplateService.class);

	public TemplateSelection() {
		label = new Label(labels.template());
		templates = new ListBox();
		button = new Button(labels.insert());

		layout();

		init();
	}

	public String getSelectedTemplate() {
		return templates.getSelectedValue();
	}
	
	public void addClickHandler(ClickHandler clickHandler) {
		button.addClickHandler(clickHandler);
	}
	
	private void init() {
		templateService.getTemplates(new AsyncCallback<List<GwtTemplate>>() {

			@Override
			public void onSuccess(List<GwtTemplate> result) {
				templates.clear();
				for (GwtTemplate template : result) {
					templates.addItem(template.getName(), template.getTemplate());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}
		});
	}
	
	

	private void layout() {
		setSpacing(Utils.SPACING);

		templates.setWidth(200 + Utils.PIXEL);
		templates.setHeight(Utils.ROW_HEIGHT + Utils.PIXEL);

		add(label);
		add(templates);
		add(button);
	}
}
