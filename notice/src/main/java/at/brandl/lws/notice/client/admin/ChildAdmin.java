package at.brandl.lws.notice.client.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.shared.service.ChildService;
import at.brandl.lws.notice.shared.service.ChildServiceAsync;
import at.brandl.lws.notice.shared.validator.GwtChildValidator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;

public class ChildAdmin extends AbstractAdminTab {

	private static final int VISIBLE_CHILDREN = 20;
	private static final int MAX_YEARS_IN_SCHOOL = 15;

	private final ChildServiceAsync childService = GWT
			.create(ChildService.class);
	private final TextBox fnBox;
	private final TextBox lnBox;
	private final ListBox beginYearBox;
	private final ListBox beginGradeBox;
	private final DateBox bdBox;
	private final CheckBox archivedBox;
	private final DecisionBox decisionBox;
	private final ListBox children;

	private Map<Long, Integer> beginYearMap = new HashMap<>();
	private Map<Long, Integer> beginGradeMap = new HashMap<>();

	private GwtChild child;

	public ChildAdmin() {
		super(true);

		beginYearBox = new ListBox();
		beginGradeBox = new ListBox();
		int year = new Date().getYear() + 1900;
		for (int i = 0; i < MAX_YEARS_IN_SCHOOL; i++) {

			String beginYear = Integer.toString(year - i);
			beginYearMap.put(Long.valueOf(beginYear), i);
			beginYearBox.addItem(beginYear);

			String beginGrade = Integer.toString(i + 1);
			beginGradeMap.put(Long.valueOf(beginGrade), i);
			beginGradeBox.addItem(beginGrade);
		}

		initChild();

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
		beginYearBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = beginYearBox.getSelectedIndex();
				if (selectedIndex != -1) {
					Long beginYear = Long.valueOf(beginYearBox
							.getItemText(selectedIndex));
					child.setBeginYear(beginYear);
				}
			}
		});
		beginGradeBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = beginGradeBox.getSelectedIndex();
				if (selectedIndex != -1) {
					Long beginGrade = Long.valueOf(beginGradeBox
							.getItemText(selectedIndex));
					child.setBeginGrade(beginGrade);
				}
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
		
		resetForm();
		initChild();
		
		getButtonPanel().setSaveButtonLabel(labels().create());
	}

	private void resetForm() {
		
		fnBox.setValue(null);
		lnBox.setValue(null);
		bdBox.setValue(null);
		beginYearBox.setSelectedIndex(0);
		beginGradeBox.setSelectedIndex(0);
		archivedBox.setValue(false);
	}

	private void initChild() {

		child = new GwtChild();
		child.setBeginYear(Long.valueOf(beginYearBox.getSelectedValue()));
		child.setBeginGrade(Long.valueOf(beginGradeBox.getSelectedValue()));
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
				
				Integer index = beginYearMap.get(child.getBeginYear());
				if (index != null) {
					beginYearBox.setSelectedIndex(index);
				} else {
					child.setBeginYear(Long.valueOf(beginYearBox.getSelectedValue()));
				}

				index = beginGradeMap.get(child.getBeginGrade());
				if (index != null) {
					beginGradeBox.setSelectedIndex(index);
				} else {
					child.setBeginGrade(Long.valueOf(beginGradeBox.getSelectedValue()));
				}
				
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
