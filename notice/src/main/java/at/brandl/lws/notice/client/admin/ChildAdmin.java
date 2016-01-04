package at.brandl.lws.notice.client.admin;

import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;
import at.brandl.lws.notice.shared.validator.GwtChildValidator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends AbstractAdminTab {

	private static final int VISIBLE_CHILDREN = 20;

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final TextBox fnBox;
	private final TextBox lnBox;
	private final LongBox beginYearBox;
	private final LongBox beginGradeBox;
	private final DateBox bdBox;
	private final CheckBox archivedBox;
	private final DecisionBox decisionBox;
	private final ListBox children;

	private GwtChild child;

	public ChildAdmin() {
		super(true);

		child = new GwtChild();

		decisionBox = new DecisionBox();
		decisionBox.setText(labels().childDelWarning());

		fnBox = new TextBox();
		fnBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				child.setFirstName(fnBox.getValue());
			}
		});
		lnBox = new TextBox();
		lnBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				child.setLastName(lnBox.getValue());
			}
		});
		bdBox = new DateBox();
		bdBox.setFormat(Utils.DATEBOX_FORMAT);
		bdBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				child.setBirthDay(bdBox.getValue());
			}
		});
		beginYearBox = new LongBox();
		beginYearBox.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
					child.setBeginYear(beginYearBox.getValue());
			}
		});
		beginGradeBox = new LongBox();
		beginGradeBox.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
					child.setBeginGrade(beginGradeBox.getValue());
			}
		});
		archivedBox = new CheckBox();
		archivedBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				child.setArchived(archivedBox.getValue());
			}
		});

		// remove comments to enable fileupload
		// final FileUploadForm fileUpload = new
		// FileUploadForm(GWT.getModuleBaseURL()+"csvUpload");
		// data.add(fileUpload);

		children = new ListBox();
		children.setVisibleItemCount(VISIBLE_CHILDREN);

		rebuildChildList();

		layout();

		addButtonUpdateChangeHandler(fnBox);
		addButtonUpdateChangeHandler(lnBox);
		addButtonUpdateChangeHandler(bdBox);
		addButtonUpdateChangeHandler(beginYearBox);
		addButtonUpdateChangeHandler(beginGradeBox);

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
		final Grid grid = new Grid(6, 2);
		grid.setWidget(0, 0, new Label(labels().firstName()));
		grid.setWidget(0, 1, fnBox);
		grid.setWidget(1, 0, new Label(labels().lastName()));
		grid.setWidget(1, 1, lnBox);
		grid.setWidget(2, 0, new Label(labels().birthday()));
		grid.setWidget(2, 1, bdBox);
		grid.setWidget(3, 0, new Label(labels().beginYear()));
		grid.setWidget(3, 1, beginYearBox);
		grid.setWidget(4, 0, new Label(labels().beginGrade()));
		grid.setWidget(4, 1, beginGradeBox);
		grid.setWidget(5, 0, new Label(labels().archived()));
		grid.setWidget(5, 1, archivedBox);

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
		child = new GwtChild();

		getButtonPanel().setSaveButtonLabel(labels().create());
	}

	@Override
	void save() {
		if (!GwtChildValidator.valid(child)) {
			return;
		}

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
		final String key = children.getValue(selectedIndex);
		childService.getChild(key, new ErrorReportingCallback<GwtChild>() {

			@Override
			public void onSuccess(GwtChild child) {
				ChildAdmin.this.child = child;
				fnBox.setText(child.getFirstName());
				lnBox.setText(child.getLastName());
				bdBox.setValue(child.getBirthDay());
				beginYearBox.setValue(child.getBeginYear());
				beginGradeBox.setValue(child.getBeginGrade());
				archivedBox.setValue(child.getArchived());
				getButtonPanel().setSaveButtonLabel(labels().change());
			}
		});
	}

	@Override
	void delete() {

		if (child.getKey() == null) {
			return;
		}

		decisionBox.addOkClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				childService.deleteChild(child.getKey(),
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

	@Override
	boolean enableDelete() {
		return child.getKey() != null;
	}

	@Override
	boolean enableCancel() {
		return bdBox.getValue() != null
				|| at.brandl.lws.notice.shared.Utils.isNotEmpty(fnBox
						.getValue())
				|| at.brandl.lws.notice.shared.Utils.isNotEmpty(lnBox
						.getValue());
	}

	@Override
	boolean enableSave() {
		return GwtChildValidator.valid(child);
	}

}
