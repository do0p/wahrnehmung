package at.lws.wnm.client.admin;

import java.util.List;

import at.lws.wnm.client.service.ChildService;
import at.lws.wnm.client.service.ChildServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.FileUploadForm;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.SaveSuccess;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.GwtChild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends VerticalPanel {

	private static final String CHILD_DEL_WARNING = "Achtung, dieses Kind und alle seine Beobachtungen werden gel&ouml;scht. Der Vorgang nicht mehr r&uuml;ckg&auml;nig gemacht werden!";

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);

	private final TextBox fnBox;
	private final TextBox lnBox;
	private final DateBox bdBox;
	private final Button saveButton;
	private final Button cancelButton;
	private final Button deleteButton;
	private final PopUp dialogBox;
	private final SaveSuccess saveSuccess;

	private final ListBox children;

	private Long childNo;

	private DecisionBox decisionBox;

	public ChildAdmin() {

		fnBox = new TextBox();
		lnBox = new TextBox();
		bdBox = new DateBox();
		bdBox.setFormat(Utils.DATEBOX_FORMAT);
		saveButton = new Button(Utils.ADD);
		deleteButton = new Button(Utils.DEL);
		deleteButton.setEnabled(false);
		cancelButton = new Button(Utils.CANCEL);
		dialogBox = new PopUp();
		saveSuccess = new SaveSuccess();

		final Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, new Label("Vorname"));
		grid.setWidget(0, 1, fnBox);
		grid.setWidget(1, 0, new Label("Nachname"));
		grid.setWidget(1, 1, lnBox);
		grid.setWidget(2, 0, new Label("Geburtstag"));
		grid.setWidget(2, 1, bdBox);

		final VerticalPanel data = new VerticalPanel();
		data.add(grid);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(cancelButton);
		data.add(buttonPanel);

		data.add(new HTML("<br/><br/>"));
		
		final FileUploadForm fileUpload = new FileUploadForm(GWT.getModuleBaseURL()+"csvUpload");
		data.add(fileUpload);
		
		children = new ListBox(false);
		children.setVisibleItemCount(20);
		children.addClickHandler(new ChildClickHandler());
		rebuildChildList();

		final HorizontalPanel root = new HorizontalPanel();
		root.add(data);

		root.add(children);

		add(root);
		saveButton.addClickHandler(new SaveClickHandler());
		cancelButton.addClickHandler(new CancelClickHandler());
		deleteButton.addClickHandler(new DeleteClickHandler());

		decisionBox = new DecisionBox();
		decisionBox.setText(CHILD_DEL_WARNING);
	}

	private void rebuildChildList() {
		children.clear();
		childService.queryChildren(new AsyncCallback<List<GwtChild>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage(caught.getLocalizedMessage());
				dialogBox.setDisableWhileShown(saveButton);
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtChild> result) {
				for (GwtChild child : result) {
					children.addItem(Utils.formatChildName(child), child
							.getKey().toString());
				}
			}
		});
	}

	private void resetForm() {
		childNo = null;
		fnBox.setText("");
		lnBox.setText("");
		bdBox.setValue(null);
		if (deleteButton.isEnabled()) {
			deleteButton.setEnabled(false);
		}
		saveButton.setHTML(Utils.ADD);
	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final GwtChild child = new GwtChild();
			child.setKey(childNo);
			child.setFirstName(fnBox.getValue());
			child.setLastName(lnBox.getValue());
			child.setBirthDay(bdBox.getValue());

			childService.storeChild(child, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					dialogBox.setErrorMessage(caught.getLocalizedMessage());
					dialogBox.setDisableWhileShown(saveButton);
					dialogBox.center();
				}

				@Override
				public void onSuccess(Void result) {
					saveSuccess.center();
					saveSuccess.show();
					rebuildChildList();
					resetForm();
				}

			});
		}

	}

	public class ChildClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			final int selectedIndex = children.getSelectedIndex();
			if (selectedIndex < 0) {
				return;
			}
			final Long childKey = Long
					.valueOf(children.getValue(selectedIndex));
			childService.getChild(childKey, new AsyncCallback<GwtChild>() {

				@Override
				public void onFailure(Throwable caught) {
					dialogBox.setErrorMessage(caught.getLocalizedMessage());
					dialogBox.setDisableWhileShown(deleteButton);
					dialogBox.center();
				}

				@Override
				public void onSuccess(GwtChild child) {
					childNo = child.getKey();
					fnBox.setText(child.getFirstName());
					lnBox.setText(child.getLastName());
					bdBox.setValue(child.getBirthDay());
					saveButton.setHTML(Utils.CHANGE);
					deleteButton.setEnabled(true);

				}
			});
		}

	}

	public class DeleteClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
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
								new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										dialogBox.setErrorMessage(caught
												.getLocalizedMessage());
										dialogBox
												.setDisableWhileShown(deleteButton);
										dialogBox.center();
									}

									@Override
									public void onSuccess(Void result) {
										rebuildChildList();
										resetForm();
									}
								});
					}
				});
				decisionBox.center();
			}

		}

	}

	public class CancelClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			resetForm();
		}

	}
}
