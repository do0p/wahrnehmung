package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.List;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.client.service.WahrnehmungsService;
import at.brandl.lws.notice.client.service.WahrnehmungsServiceAsync;
import at.brandl.lws.notice.shared.model.GwtFileInfo;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

public class FileUploadForm extends FormPanel {

	private final Labels labels = (Labels) GWT.create(Labels.class);
	private final WahrnehmungsServiceAsync wahrnehmungService = (WahrnehmungsServiceAsync) GWT
			.create(WahrnehmungsService.class);

	private final FileUpload upload;
	private final Button button;
	private final DecisionBox decisionBox;
	private final PopUp dialogBox;
	private final CellTable<GwtFileInfo> fileTable;
	private final ListDataProvider<GwtFileInfo> dataProvider;

	private boolean actionSet;
	private ChangeHandler handler;

	public FileUploadForm() {

		dataProvider = new ListDataProvider<GwtFileInfo>();
		upload = new FileUpload();
		button = new Button(labels.save());
		decisionBox = new DecisionBox();
		dialogBox = new PopUp();
		fileTable = new CellTable<GwtFileInfo>();

		layout();

		init();
	}

	@Override
	public void reset() {

		super.reset();
		dataProvider.getList().clear();
		updateButtonState();

	}

	private void init() {
		dataProvider.addDataDisplay(fileTable);

		fileTable.addColumn(new TextColumn<GwtFileInfo>() {

			@Override
			public String getValue(GwtFileInfo fileInfo) {
				return fileInfo.getFilename();
			}
		});
		fileTable.addColumn(new Column<GwtFileInfo, String>(new ImageCell()) {
			@Override
			public String getValue(GwtFileInfo fileInfo) {
				return fileInfo.getImageUrl() + "=s32";
			}
		});
		fileTable.addColumn(new IdentityColumn<GwtFileInfo>(
				new ActionCell<GwtFileInfo>(labels.delete(),
						new Delegate<GwtFileInfo>() {
							@Override
							public void execute(GwtFileInfo fileInfo) {
								delete(fileInfo);
							}

						})));

		upload.setName("file");
		upload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateButtonState();
			}
		});
		button.setEnabled(false);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String filename = getFilename();
				wahrnehmungService.fileExists(filename,
						new AsyncCallback<Boolean>() {

							@Override
							public void onFailure(Throwable caught) {
								dialogBox.setErrorMessage(caught.getMessage());
							}

							@Override
							public void onSuccess(Boolean fileExists) {
								if (fileExists) {
									decisionBox.setText(labels
											.fileExistsWarning(filename));
									decisionBox
											.addOkClickHandler(new ClickHandler() {
												@Override
												public void onClick(
														ClickEvent event) {
													submit();
													decisionBox.hide();
												}
											});
									decisionBox.center();
								} else {
									submit();
								}
							}
						});

			}

		});

		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				setAction();
				parseNames(event.getResults());
			}

		});

		setAction();
	}

	@Override
	public void submit() {
		actionSet = false;
		super.submit();
		super.reset();
		updateButtonState();
	}

	private void setAction() {
		wahrnehmungService.getFileUploadUrl(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(String url) {
				setAction(url);
			}
		});
	}

	private void layout() {

		final Panel uploadFields = new HorizontalPanel();
		uploadFields.add(upload);
		uploadFields.add(button);

		final Panel panel = new VerticalPanel();
		panel.add(fileTable);
		panel.add(uploadFields);

		setWidget(panel);
	}

	private void parseNames(String results) {
		String[] names = results.split("\n");
		for (String name : names) {
			GwtFileInfo fileInfo = createFileInfo(name);
			if (fileInfo != null) {
				dataProvider.getList().add(fileInfo);
			}
		}
		dataProvider.flush();
		handler.onChange(null);
	}

	private GwtFileInfo createFileInfo(String name) {
		String[] parts = name.split("::");
		String filename = parts[0];
		String storageFilename = parts[1];
		String contentType = parts[2];
		String imageUrl = parts[3];
		if (at.brandl.lws.notice.shared.Utils.isNotEmpty(filename) && at.brandl.lws.notice.shared.Utils.isNotEmpty(storageFilename)
				&& at.brandl.lws.notice.shared.Utils.isNotEmpty(contentType)) {
			GwtFileInfo fileInfo = new GwtFileInfo();
			fileInfo.setFilename(filename);
			fileInfo.setStorageFilename(storageFilename);
			fileInfo.setContentType(contentType);
			if (imageUrl != "-") {
				fileInfo.setImageUrl(imageUrl);
			}
			return fileInfo;
		}
		return null;
	}

	@Override
	public void setAction(String url) {
		super.setAction(url);
		actionSet = true;
		updateButtonState();
	}

	public List<GwtFileInfo> getFileInfos() {
		return new ArrayList<GwtFileInfo>(dataProvider.getList());
	}

	private void updateButtonState() {
		boolean enabled = at.brandl.lws.notice.shared.Utils.isNotEmpty(upload.getFilename()) && actionSet;
		button.setEnabled(enabled);
	}

	private String getFilename() {
		String filename = upload.getFilename();
		int pos = filename.lastIndexOf("\\");
		if (pos < 0) {
			pos = filename.lastIndexOf("/");
		}
		return filename.substring(pos + 1);
	}

	public void setFileInfos(List<GwtFileInfo> fileInfos) {
		dataProvider.getList().clear();
		dataProvider.getList().addAll(fileInfos);
	}

	public void setChangeHandler(final ChangeHandler handler) {
		this.handler = handler;
	}
	
	public void setEnabled(boolean enabled) {
		upload.setEnabled(enabled);
		button.setEnabled(enabled);
	}

	private void show(GwtFileInfo fileInfo) {
		// TODO Auto-generated method stub

	}

	private void delete(GwtFileInfo fileInfo) {
		dataProvider.getList().remove(fileInfo);
		dataProvider.flush();
		handler.onChange(null);
	}
}
