package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.shared.service.SectionService;
import at.brandl.lws.notice.shared.service.SectionServiceAsync;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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

	private static enum Direction {

		UP(-1), DOWN(1);

		private final int delta;

		private Direction(int delta) {
			this.delta = delta;
		}
	}

	private static class SaveItem extends TextBox {

		private static int count = 0;
		private final String id;
		private GwtSection section;
		private String parentKey;
		private boolean delete;

		public SaveItem(String parentKey) {

			id = "p" + count++ + parentKey;
			this.parentKey = parentKey;
			setFocus(true);
		}

		public SaveItem(GwtSection section) {

			id = section.getKey();
			this.section = section;
			setText(section.getSectionName());
			selectAll();
			setFocus(true);
		}

		public SaveItem(GwtSection section, boolean delete) {

			id = section.getKey();
			this.section = section;
			this.delete = delete;
		}

		private GwtSection createSectionFromSaveItem() {

			GwtSection section;
			if (this.section != null) {
				section = this.section;
			} else {
				section = new GwtSection();
				section.setParentKey(parentKey);
			}
			section.setSectionName(getValue());
			return section;
		}

		@Override
		public boolean equals(Object obj) {

			if (!(obj instanceof SaveItem)) {
				return false;
			}
			SaveItem other = (SaveItem) obj;
			return id.equals(other.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);
	private final Set<SaveItem> saveItems = new HashSet<SaveItem>();

	private final Tree tree;
	private final DecisionBox decisionBox;

	public SectionAdmin() {
		super(false);
		getButtonPanel().setSaveButtonLabel(labels().save());
		decisionBox = new DecisionBox();
		decisionBox.setText(labels().sectionDelWarning());

		tree = new Tree();

		layout();

		updateButtonPanel();
	}

	@Override
	protected void onLoad() {
		refresh();
	}

	@Override
	boolean enableCancel() {
		return enableSave();
	}

	@Override
	boolean enableSave() {
		for (SaveItem item : saveItems) {

			if (item.section != null) {
				return true;
			}
			if (!at.brandl.lws.notice.shared.Utils.isEmpty(item.getValue())) {
				return true;
			}
		}
		return false;
	}

	@Override
	void reset() {
		saveItems.clear();
		tree.clear();
		refresh();
	}

	@Override
	void save() {

		deleteSections();
		List<GwtSection> sections = getSections();

		if (!sections.isEmpty()) {
			sectionService.storeSection(sections, new AsyncVoidCallBack());
		}
	}

	private List<GwtSection> getSections() {

		List<GwtSection> sections = new ArrayList<GwtSection>();
		for (final SaveItem saveItem : saveItems) {
			if (at.brandl.lws.notice.shared.Utils.isEmpty(saveItem.getValue())) {
				continue;
			}
			sections.add(saveItem.createSectionFromSaveItem());
		}
		return sections;
	}

	private void layout() {

		add(tree);
		add(getButtonPanel());
	}

	private void refresh() {
		sectionService.querySections(new TreeBuilder());
	}

	private void deleteSections() {

		Iterator<SaveItem> iterator = saveItems.iterator();
		while (iterator.hasNext()) {
			SaveItem saveItem = iterator.next();

			if (saveItem.section == null) {
				continue;
			}

			if (saveItem.delete) {
				sectionService.deleteSection(saveItem.section,
						new AsyncVoidCallBack());
				iterator.remove();
			}
		}
	}

	private class TreeBuilder extends ErrorReportingCallback<List<GwtSection>> {

		@Override
		public void onSuccess(List<GwtSection> sections) {

			addChildSections(tree, null, groupSections(sections));
			updateButtonPanel();
		}

		private void addChildSections(HasTreeItems parent,
				GwtSection parentSection,
				ListMultimap<String, GwtSection> groupedSections) {

			String parentKey = parentSection == null ? null : parentSection
					.getKey();

			addNewSectionItem(parent, parentKey);

			List<GwtSection> sections = groupedSections.get(parentKey);
			if (sections == null) {
				return;
			}

			int numSections = sections.size();
			for (int pos = 0; pos < numSections; pos++) {

				boolean first = pos == 0;
				boolean last = pos + 1 == numSections;
				GwtSection section = sections.get(pos);

				handleReordering(section, pos);

				TreeItem item = createItem(section, parentSection, parent,
						groupedSections, first, last);

				if (!section.getArchived()) {
					addChildSections(item, section, groupedSections);
				}
			}
		}

		private SaveItem createSaveItemForNewSection(String parentKey) {

			final SaveItem saveItem = new SaveItem(parentKey);
			saveItems.add(saveItem);
			addButtonUpdateChangeHandler(saveItem);
			addButtonUpdateKeyPressHandler(saveItem);
			return saveItem;
		}

		private SaveItem createSaveItemForSectionEdit(GwtSection section) {

			final SaveItem saveItem = new SaveItem(section);
			saveItems.add(saveItem);
			addButtonUpdateChangeHandler(saveItem);
			addButtonUpdateKeyPressHandler(saveItem);
			return saveItem;
		}

		private void createDelItem(GwtSection section) {

			final SaveItem saveItem = new SaveItem(section, true);
			saveItems.add(saveItem);
		}

		private TreeItem createItem(final GwtSection section,
				final GwtSection parentSection, final HasTreeItems parent,
				final ListMultimap<String, GwtSection> groupedSections,
				boolean first, boolean last) {

			final Anchor sectionEdit = new Anchor(labels().change());
			final Anchor sectionArchive = createArchiveLink(section);
			final Anchor sectionDel = new Anchor(labels().delete());
			final Anchor sortUp = createSortUpLink(first);
			final Anchor sortDown = createSortDownLink(last);
			final InlineLabel sectionName = createSectionLabel(section);

			final TreeItem sectionItem = new TreeItem(createSectionLabel(
					sectionName, sectionEdit, sectionArchive, sectionDel,
					sortUp, sortDown));
			parent.addItem(sectionItem);

			final HandlerRegistration editClickHandler = sectionEdit
					.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (Utils.isLeftClick(event)) {
								handleEdit(section, sectionItem);
							}
						}
					});

			final HandlerRegistration archiveClickHandler = sectionArchive
					.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (Utils.isLeftClick(event)) {
								handleArchive(section, sectionName,
										sectionArchive, sectionItem);
							}
						}
					});

			sectionDel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (Utils.isLeftClick(event)) {
						decisionBox.addOkClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (Utils.isLeftClick(event)) {
									handleDelete(section, sectionName,
											sectionItem, editClickHandler,
											archiveClickHandler);
								}
							}
						});
						decisionBox.center();
					}
				}
			});

			if (sortUp != null) {
				sortUp.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (Utils.isLeftClick(event)) {
							handleSortUp(parent, groupedSections, section,
									sectionItem, parentSection);
						}
					}
				});
			}

			if (sortDown != null) {
				sortDown.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (Utils.isLeftClick(event)) {
							handleSortDown(parent, groupedSections, section,
									sectionItem, parentSection);
						}
					}
				});
			}

			return sectionItem;
		}

		private Anchor createSortDownLink(boolean last) {
			Anchor sortDown = null;
			if (!last) {
				sortDown = new Anchor(Utils.DOWN_ARROW);
			}
			return sortDown;
		}

		private Anchor createSortUpLink(boolean first) {
			Anchor sortUp = null;
			if (!first) {
				sortUp = new Anchor(Utils.UP_ARROW);
			}
			return sortUp;
		}

		private InlineLabel createSectionLabel(GwtSection section) {
			final InlineLabel sectionName = new InlineLabel(
					section.getSectionName());
			if (section.getArchived()) {
				sectionName.setStylePrimaryName(Utils.ARCHIVED_STYLE);
			}
			return sectionName;
		}

		private Anchor createArchiveLink(GwtSection section) {
			String archiveLabel;
			if (section.getArchived()) {
				archiveLabel = labels().unarchive();
			} else {
				archiveLabel = labels().archive();
			}
			final Anchor sectionArchive = new Anchor(archiveLabel);
			return sectionArchive;
		}

		private void handleReordering(GwtSection section, int actualPos) {

			if (section.getPos() != actualPos) {
				section.setPos(actualPos);
				saveItems.add(new SaveItem(section));
			}
		}

		private HorizontalPanel createSectionLabel(InlineLabel sectionName,
				Anchor... anchorsArray) {

			List<Anchor> anchors = filterNullAnchors(anchorsArray);
			final HorizontalPanel sectionLabel = new HorizontalPanel();
			sectionLabel.setSpacing(Utils.SPACING);
			sectionLabel.add(sectionName);
			sectionLabel.add(new InlineLabel(" ("));
			int numAnchors = anchors.size();
			for (int i = 0; i < numAnchors; i++) {
				sectionLabel.add(anchors.get(i));
				if (i + 1 < numAnchors) {
					sectionLabel.add(new InlineLabel("/"));
				}
			}
			sectionLabel.add(new InlineLabel(")"));
			return sectionLabel;
		}

		private List<Anchor> filterNullAnchors(Anchor... anchorsArray) {
			List<Anchor> anchors = new ArrayList<>();
			for (int i = 0; i < anchorsArray.length; i++) {
				Anchor anchor = anchorsArray[i];
				if (anchor != null) {
					anchors.add(anchor);
				}
			}
			return anchors;
		}

		private void addNewSectionItem(HasTreeItems object,
				final String parentKey) {

			if (object instanceof TreeItem) {

				Anchor addLabel = new Anchor(labels().create());
				TreeItem addItem = new TreeItem(addLabel);
				final TreeItem sectionItem = (TreeItem) object;
				sectionItem.addItem(addItem);
				addLabel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
							sectionItem.insertItem(1,
									createSaveItemForNewSection(parentKey));
						}
					}
				});
			} else {
				object.addItem(createSaveItemForNewSection(parentKey));
			}
		}

		private ListMultimap<String, GwtSection> groupSections(
				List<GwtSection> sections) {

			final ListMultimap<String, GwtSection> grouped = ArrayListMultimap
					.create();
			for (GwtSection section : sections) {
				grouped.put(section.getParentKey(), section);
			}
			return grouped;
		}

		private void handleDelete(final GwtSection section,
				final InlineLabel sectionName, final TreeItem sectionItem,
				final HandlerRegistration editClickHandler,
				final HandlerRegistration archiveClickHandler) {

			sectionName.setStylePrimaryName(Utils.DELETED_STYLE);
			sectionItem.setState(false);
			editClickHandler.removeHandler();
			archiveClickHandler.removeHandler();
			createDelItem(section);
			updateButtonPanel();
		}

		private void handleArchive(final GwtSection section,
				final InlineLabel sectionName, final Anchor sectionArchive,
				final TreeItem sectionItem) {
			if (section.getArchived()) {
				sectionName.removeStyleName(Utils.ARCHIVED_STYLE);
				section.setArchived(Boolean.FALSE);
				sectionArchive.setText(labels().archive());
			} else {
				sectionName.setStylePrimaryName(Utils.ARCHIVED_STYLE);
				section.setArchived(Boolean.TRUE);
				sectionArchive.setText(labels().unarchive());
				sectionItem.removeItems();
			}
			saveItems.add(new SaveItem(section));
			updateButtonPanel();
		}

		private void handleEdit(final GwtSection section,
				final TreeItem sectionItem) {
			sectionItem.setWidget(createSaveItemForSectionEdit(section));
		}

		private void handleSortUp(HasTreeItems parent,
				ListMultimap<String, GwtSection> groupedSections,
				GwtSection section, TreeItem sectionItem,
				GwtSection parentSection) {

			moveSection(section, parent, parentSection, groupedSections,
					Direction.UP);
		}

		private void handleSortDown(HasTreeItems parent,
				ListMultimap<String, GwtSection> groupedSections,
				GwtSection section, TreeItem sectionItem,
				GwtSection parentSection) {

			moveSection(section, parent, parentSection, groupedSections,
					Direction.DOWN);
		}

		private void moveSection(GwtSection section, HasTreeItems parent,
				GwtSection parentSection,
				ListMultimap<String, GwtSection> groupedSections,
				Direction direction) {

			int pos = (int) section.getPos();

			final List<GwtSection> sections = groupedSections
					.get(parentSection == null ? null : parentSection.getKey());
			sections.remove(pos);
			sections.add(pos + direction.delta, section);

			parent.removeItems();
			addChildSections(parent, parentSection, groupedSections);
			updateButtonPanel();
		}
	}

	private class AsyncVoidCallBack extends ErrorReportingCallback<Void> {

		@Override
		public void onFailure(Throwable caught) {
			super.onFailure(caught);
			reset();
		}

		@Override
		public void onSuccess(Void result) {
			reset();
		}
	}
}
