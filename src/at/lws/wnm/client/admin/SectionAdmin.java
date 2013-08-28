package at.lws.wnm.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.client.Labels;
import at.lws.wnm.client.service.SectionService;
import at.lws.wnm.client.service.SectionServiceAsync;
import at.lws.wnm.client.utils.DecisionBox;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SectionAdmin extends VerticalPanel {

	private final Labels labels = GWT.create(Labels.class);
	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final Button saveButton;
	private final PopUp dialogBox;
//	private final SaveSuccess saveSuccess;
	private Tree tree;

	private Button cancelButton;
	private List<SaveItem> saveItems = new ArrayList<SaveItem>();

	private int countDown = 0;
	private DecisionBox decisionBox;

	public SectionAdmin() {

		saveButton = new Button(labels.save());
		cancelButton = new Button(labels.cancel());
		dialogBox = new PopUp();
//		saveSuccess = new SaveSuccess();

		tree = new Tree();
		add(tree);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		buttonPanel.setSpacing(Utils.SPACING);
		add(buttonPanel);

		decisionBox = new DecisionBox();
		decisionBox.setText(labels.sectionDelWarning());
		
		saveButton.addClickHandler(new SaveClickHandler());
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				resetForm();
			}
		});
	}

	@Override
	protected void onLoad() {
		refresh();
	}

	private void refresh() {
		sectionService.querySections(new TreeBuilder());
	}

	private void resetFormAtTheEnd() {
		if (--countDown < 1) {
			resetForm();
		}
	}

	private void resetForm() {
		countDown = 0;
		saveItems.clear();
		tree.clear();
		refresh();
	}

	private class TreeBuilder implements AsyncCallback<List<GwtSection>> {

		@Override
		public void onFailure(Throwable caught) {
			dialogBox.setErrorMessage(caught.getLocalizedMessage());
			dialogBox.setDisableWhileShown(saveButton);
			dialogBox.center();
		}

		@Override
		public void onSuccess(List<GwtSection> sections) {
			final Map<String, List<GwtSection>> groupedSections = groupSections(sections);
			final List<GwtSection> rootSections = groupedSections.get(null);

			addChildSections(rootSections, tree, groupedSections);
			tree.add(createSaveItem((String) null));

		}

		private SaveItem createSaveItem(String parentKey) {
			final SaveItem saveItem = new SaveItem(parentKey);
			saveItems.add(saveItem);
			return saveItem;
		}

		protected SaveItem createSaveItem(GwtSection section) {
			final SaveItem saveItem = new SaveItem(section);
			saveItems.add(saveItem);
			return saveItem;
		}

		private void createDelItem(GwtSection section) {
			final SaveItem saveItem = new SaveItem(section, true);
			saveItems.add(saveItem);
		}

		private void addChildSections(List<GwtSection> sections,
				final HasTreeItems parent,				Map<String, List<GwtSection>> groupedSections) {
			if (sections == null) {
				return;
			}
			for (final GwtSection section : sections) {
				final InlineLabel sectionName = new InlineLabel(
						section.getSectionName());
				final Anchor sectionEdit = new Anchor(labels.change());
				final Anchor sectionDel = new Anchor(labels.delete());

				final TreeItem sectionItem = new TreeItem(createSectionLabel(
						sectionName, sectionEdit, sectionDel));
				final HandlerRegistration editClickHandler = sectionEdit
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (NativeEvent.BUTTON_LEFT == event
										.getNativeButton()) {
									sectionItem
											.setWidget(createSaveItem(section));
								}
							}
						});
				sectionDel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
							decisionBox.addOkClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									sectionName.setStylePrimaryName(Utils.DELETED_STYLE);
									sectionItem.setState(false);
									editClickHandler.removeHandler();
									createDelItem(section);
								}
							});
							decisionBox.center();
						}
					}

				});

				addPlusItem(section.getKey(), sectionItem);
				addChildSections(groupedSections.get(section.getKey()),
						sectionItem, groupedSections);
				parent.addItem(sectionItem);
			}
		}

		private HorizontalPanel createSectionLabel(
				final InlineLabel sectionName, final Anchor sectionEdit,
				final Anchor sectionDel) {
			final HorizontalPanel sectionLabel = new HorizontalPanel();
			sectionLabel.setSpacing(Utils.SPACING);
			sectionLabel.add(sectionName);
			sectionLabel.add(new InlineLabel(" ("));
			sectionLabel.add(sectionEdit);
			sectionLabel.add(new InlineLabel("/"));
			sectionLabel.add(sectionDel);
			sectionLabel.add(new InlineLabel(")"));
			return sectionLabel;
		}

		private void addPlusItem(final String parentKey,
				final TreeItem sectionItem) {
			final Anchor addLabel = new Anchor(labels.create());
			final TreeItem addItem = new TreeItem(addLabel);
			sectionItem.addItem(addItem);
			addLabel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
						sectionItem.insertItem(1, createSaveItem(parentKey));
					}
				}

			});
		}

		private Map<String, List<GwtSection>> groupSections(
				List<GwtSection> sections) {
			final Map<String, List<GwtSection>> result = new HashMap<String, List<GwtSection>>();
			for (GwtSection section : sections) {
				List<GwtSection> sectionList = result.get(section
						.getParentKey());
				if (sectionList == null) {
					sectionList = new ArrayList<GwtSection>();
					result.put(section.getParentKey(), sectionList);
				}
				sectionList.add(section);
			}
			return result;
		}

	}

	public class SaveClickHandler implements ClickHandler {


		@Override
		public void onClick(ClickEvent event) {
			final List<SaveItem> tmpSaveItems = new ArrayList<SaveItem>(
					saveItems);
			countDown = saveItems.size();
			for (final SaveItem saveItem : tmpSaveItems) {

				if (saveItem.delete) {
					sectionService.deleteSection(saveItem.section,
							new AsyncVoidCallBack());
				} else {
					final String value = saveItem.getValue();
					if (Utils.isEmpty(value)) {
						countDown--;
						continue;
					}
					sectionService.storeSection(
							createSectionFromSaveItem(saveItem, value),
							new AsyncVoidCallBack());
				}
			}
		}

		private GwtSection createSectionFromSaveItem(SaveItem saveItem,
				final String value) {

			final GwtSection section;

			if (saveItem.section != null) {
				section = saveItem.section;
				section.setSectionName(value);
			} else {
				section = new GwtSection();
				section.setParentKey(saveItem.parentKey);
				section.setSectionName(value);
			}
			return section;
		}

	}

	public class SaveItem extends TextBox {

		private String parentKey;
		private GwtSection section;
		private boolean delete;

		public SaveItem(String parentKey) {
			this.parentKey = parentKey;
			setFocus(true);
		}

		public SaveItem(GwtSection section) {
			this.section = section;
			setText(section.getSectionName());
			selectAll();
			setFocus(true);
		}

		public SaveItem(GwtSection section, boolean delete) {
			this.section = section;
			this.delete = delete;
		}

	}

	public class AsyncVoidCallBack implements AsyncCallback<Void> {

		@Override
		public void onFailure(Throwable caught) {
			final String errorMessage = caught.getLocalizedMessage();
			showError(errorMessage);
			resetFormAtTheEnd();
		}

		@Override
		public void onSuccess(Void result) {
//			if (!dialogBox.isShowing()) {
//				saveSuccess.center();
//				saveSuccess.show();
//			}
			resetFormAtTheEnd();
		}

		private void showError(final String errorMessage) {
//			if (saveSuccess.isShowing()) {
//				saveSuccess.hide();
//			}
			dialogBox.setErrorMessage(errorMessage);
			dialogBox.setDisableWhileShown(saveButton);
			dialogBox.center();
		}

	}

}
