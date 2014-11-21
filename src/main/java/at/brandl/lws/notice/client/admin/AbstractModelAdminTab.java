package at.brandl.lws.notice.client.admin;

import java.util.List;

import at.brandl.lws.notice.client.service.ModelServiceAsync;
import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.shared.model.GwtModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

abstract class AbstractModelAdminTab<T extends GwtModel> extends
		AbstractAdminTab {

	private final DecisionBox decisionBox;
	private final ModelServiceAsync<T> modelService;
	private final ListBox objectList;

	private T model;

	AbstractModelAdminTab(boolean showDeleteButton,
			ModelServiceAsync<T> modelService) {
		super(showDeleteButton);
		this.modelService = modelService;

		decisionBox = new DecisionBox();
		decisionBox.setText(getDelWarning());

		objectList = new ListBox();
		objectList.setVisibleItemCount(getListCount());
		objectList.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select();
				updateButtonPanel();
			}
		});

		init();
		layout();

		refresh();
	}

	protected abstract void init();

	protected abstract Widget createContentLayout();

	protected abstract int getListCount();

	protected abstract void updateFields();

	protected abstract T createModel();

	protected abstract String getKey(T object);

	protected abstract String getDisplayName(T object);

	protected String getDelWarning() {
		return labels().defaultDelWarning();
	}

	@Override
	protected void save() {
		modelService.store(model, new ErrorReportingCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				refresh();
			}

		});
	}

	@Override
	protected void delete() {
		decisionBox.addOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modelService.delete(getModel(),
						new ErrorReportingCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								refresh();
							}
						});
			}
		});
		decisionBox.center();
	}

	@Override
	protected void reset() {
		this.model = createModel();
		updateFields();
		getButtonPanel().setSaveButtonLabel(labels().save());
	}

	@Override
	protected boolean enableDelete() {
		return !model.isNew();
	}

	@Override
	protected boolean enableCancel() {
		return !model.isNew() || model.hasChanges();
	}

	@Override
	protected boolean enableSave() {
		return model.hasChanges() && model.isValid();
	}

	protected T getModel() {
		return model;
	}

	private void layout() {

		final VerticalPanel data = new VerticalPanel();
		data.add(createContentLayout());
		data.add(getButtonPanel());

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(objectList);

		add(root);
	}

	private void refresh() {
		rebuildObjectList();
		reset();
		updateButtonPanel();
	}

	private void rebuildObjectList() {
		modelService.getAll(new ErrorReportingCallback<List<T>>() {

			@Override
			public void onSuccess(List<T> result) {
				objectList.clear();
				for (T object : result) {
					objectList.addItem(getDisplayName(object), getKey(object));
				}
			}

		});
	}

	private void select() {
		final int selectedIndex = objectList.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		final String templateKey = objectList.getValue(selectedIndex);
		modelService.get(templateKey, new ErrorReportingCallback<T>() {

			@Override
			public void onSuccess(T object) {
				object.setChanged(false);
				AbstractModelAdminTab.this.model = object;
				updateFields();
				updateButtonPanel();
				getButtonPanel().setSaveButtonLabel(labels().change());

			}
		});
	}

}