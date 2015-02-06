package at.brandl.lws.notice.client.admin;

import java.util.List;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends AbstractAdminTab {

	private static final int VISIBLE_CHILDREN = 20;
	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);

	private final TextBox fnBox;
	private final TextBox lnBox;
	private final DateBox bdBox;

	private final DecisionBox decisionBox;

	private final ListBox children;

	private String childNo;


	public ChildAdmin() {
		super(true);
		
		decisionBox = new DecisionBox();
		decisionBox.setText(labels().childDelWarning());
		
		fnBox = new TextBox();
		lnBox = new TextBox();
		bdBox = new DateBox();
		bdBox.setFormat(Utils.DATEBOX_FORMAT);

		// remove comments to enable fileupload
		// final FileUploadForm fileUpload = new
		// FileUploadForm(GWT.getModuleBaseURL()+"csvUpload");
		// data.add(fileUpload);

		children = new ListBox(false);
		children.setVisibleItemCount(VISIBLE_CHILDREN);
		
		rebuildChildList();

		layout();
		
		addButtonUpdateChangeHandler(fnBox);
		addButtonUpdateChangeHandler(lnBox);
		addButtonUpdateChangeHandler(bdBox);
		
		children.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select();
				updateButtonPanel();
			}
		});

		updateButtonPanel();
	}

	private void layout() {
		final Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label(labels().firstName()));
		grid.setWidget(0, 1, fnBox);
		grid.setWidget(1, 0, new Label(labels().lastName()));
		grid.setWidget(1, 1, lnBox);
		grid.setWidget(2, 0, new Label(labels().birthday()));
		grid.setWidget(2, 1, bdBox);

		final VerticalPanel data = new VerticalPanel();
		data.add(grid);
		data.add(getButtonPanel());
		data.add(new HTML(Utils.LINE_BREAK + Utils.LINE_BREAK));

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);
		root.add(children);

		add(root);
	}

	private void rebuildChildList() {
		children.clear();
		childService
				.queryChildren(new ErrorReportingCallback<List<GwtChild>>() {

					@Override
					public void onSuccess(List<GwtChild> result) {
						for (GwtChild child : result) {
							children.addItem(Utils.formatChildName(child),
									child.getKey().toString());
						}
					}
				});
	}

	@Override
	void reset() {
		childNo = null;
		fnBox.setText("");
		lnBox.setText("");
		bdBox.setValue(null);

		getButtonPanel().setSaveButtonLabel(labels().create());
	}

	@Override
	void save() {
		final GwtChild child = new GwtChild();
		child.setKey(childNo);
		child.setFirstName(fnBox.getValue());
		child.setLastName(lnBox.getValue());
		child.setBirthDay(bdBox.getValue());

		childService.storeChild(child, new ErrorReportingCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
				// saveSuccess.center();
				// saveSuccess.show();
				rebuildChildList();
				reset();
			}

		});
	}

	private void select() {
		final int selectedIndex = children.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		final String childKey = children.getValue(selectedIndex);
		childService.getChild(childKey, new ErrorReportingCallback<GwtChild>() {

			@Override
			public void onSuccess(GwtChild child) {
				childNo = child.getKey();
				fnBox.setText(child.getFirstName());
				lnBox.setText(child.getLastName());
				bdBox.setValue(child.getBirthDay());
				getButtonPanel().setSaveButtonLabel(labels().change());

			}
		});
	}

	@Override
	void delete() {
		if (childNo != null) {
			final GwtChild child = new GwtChild();
			child.setKey(childNo);
			child.setFirstName(fnBox.getValue());
			child.setLastName(lnBox.getValue());
			child.setBirthDay(bdBox.getValue());

			decisionBox.addOkClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					childService.deleteChild(child,
							new ErrorReportingCallback<Void>() {

								@Override
								public void onSuccess(Void result) {
									rebuildChildList();
									reset();
								}
							});
				}
			});
			decisionBox.center();
		}
	}

	@Override
	boolean enableDelete() {
		return children.getSelectedIndex() != -1;
	}

	@Override
	boolean enableCancel() {
		return bdBox.getValue() != null || Utils.isNotEmpty(fnBox.getValue())
				|| Utils.isNotEmpty(lnBox.getValue());
	}

	@Override
	boolean enableSave() {
		return bdBox.getValue() != null && Utils.isNotEmpty(fnBox.getValue())
				&& Utils.isNotEmpty(lnBox.getValue());
	}

}
