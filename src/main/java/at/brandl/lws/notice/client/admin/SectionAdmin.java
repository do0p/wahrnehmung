package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.shared.model.GwtSection;
import at.brandl.lws.notice.shared.service.SectionService;
import at.brandl.lws.notice.shared.service.SectionServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class SectionAdmin extends AbstractAdminTab {

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	// private final SaveSuccess saveSuccess;
	private final Tree tree;

	private final List<SaveItem> saveItems = new ArrayList<SaveItem>();
	private final DecisionBox decisionBox;

	private int countDown = 0;

	public SectionAdmin() {
		super(false);

		decisionBox = new DecisionBox();
		decisionBox.setText(labels().sectionDelWarning());

		tree = new Tree();

		// saveSuccess = new SaveSuccess();

		layout();
		
		updateButtonPanel();

	}

	private void layout() {

		add(tree);
		add(getButtonPanel());
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
			reset();
		}
	}

	@Override
	void reset() {
		countDown = 0;
		saveItems.clear();
		tree.clear();
		refresh();
	}

	private class TreeBuilder extends ErrorReportingCallback<List<GwtSection>> {

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
				final HasTreeItems parent,
				Map<String, List<GwtSection>> groupedSections) {
			if (sections == null) {
				return;
			}
			for (final GwtSection section : sections) {
				final InlineLabel sectionName = new InlineLabel(
						section.getSectionName());
				final Anchor sectionEdit = new Anchor(labels().change());
				final Anchor sectionDel = new Anchor(labels().delete());

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
									sectionName
											.setStylePrimaryName(Utils.DELETED_STYLE);
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
			final Anchor addLabel = new Anchor(labels().create());
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

	@Override
	void save() {
		final List<SaveItem> tmpSaveItems = new ArrayList<SaveItem>(saveItems);
		countDown = saveItems.size();
		for (final SaveItem saveItem : tmpSaveItems) {

			if (saveItem.delete) {
				sectionService.deleteSection(saveItem.section,
						new AsyncVoidCallBack());
			} else {
				final String value = saveItem.getValue();
				if (at.brandl.lws.notice.shared.Utils.isEmpty(value)) {
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

	private class SaveItem extends TextBox {

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

	private class AsyncVoidCallBack extends ErrorReportingCallback<Void> {

		@Override
		public void onFailure(Throwable caught) {
			super.onFailure(caught);
			resetFormAtTheEnd();
		}

		@Override
		public void onSuccess(Void result) {
			// if (!dialogBox.isShowing()) {
			// saveSuccess.center();
			// saveSuccess.show();
			// }
			resetFormAtTheEnd();
		}

	}

	@Override
	boolean enableCancel() {
		return true;
	}

	@Override
	boolean enableSave() {
		return true;
	}

}
