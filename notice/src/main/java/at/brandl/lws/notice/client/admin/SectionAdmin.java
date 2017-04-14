package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.shared.service.SectionService;
import at.brandl.lws.notice.shared.service.SectionServiceAsync;

public class SectionAdmin extends AbstractAdminTab {

	private static final Logger LOGGER = Logger.getLogger("SectionAdmin");

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

		@Override
		public String toString() {

			return super.toString() + ": section=" + section + ": delete=" + delete;
		}
	}

	private final SectionServiceAsync sectionService = GWT.create(SectionService.class);
	private final Map<SaveItem, SaveItem> saveItems = new HashMap<>();

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
		for (SaveItem item : saveItems.values()) {

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

		tree.clear();

		deleteSections();
		List<GwtSection> sections = getSectionEdits();

		saveItems.clear();

		if (!sections.isEmpty()) {
			sectionService.storeSection(sections, new AsyncVoidCallBack());
		}
	}

	private List<GwtSection> getSectionEdits() {

		List<GwtSection> sections = new ArrayList<GwtSection>();
		for (final SaveItem saveItem : saveItems.values()) {
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

		LOGGER.info("in delete");
		List<GwtSection> sectionsToDelete = new ArrayList<>();
		List<SaveItem> allItems = new ArrayList<>(saveItems.values());
		LOGGER.info("saveitems " + allItems);
		for (SaveItem saveItem : allItems) {
			LOGGER.info("saveitem " + saveItem);
			if (saveItem.section == null || !saveItem.delete) {
				continue;
			}

			LOGGER.info("deleting " + saveItem.section);
			saveItems.remove(saveItem);
			sectionsToDelete.add(saveItem.section);
		}
		
		if (!sectionsToDelete.isEmpty()) {
			LOGGER.info("deleting " + sectionsToDelete);
			sectionService.deleteSection(sectionsToDelete, new AsyncVoidCallBack());
		}
	}

	private class TreeBuilder extends ErrorReportingCallback<List<GwtSection>> {

		private static final String ACTIVE = "active";
		private static final String ARCHIVED = "archived";

		@Override
		public void onSuccess(List<GwtSection> sections) {

			tree.clear();
			addChildSections(tree, null, groupSections(sections));
			updateButtonPanel();
		}

		private void addChildSections(HasTreeItems parent, GwtSection parentSection,
				Map<String, Map<String, List<GwtSection>>> groupedSections) {

			String parentKey = parentSection == null ? null : parentSection.getKey();

			addNewSectionItem(parent, parentKey);

			Map<String, List<GwtSection>> allSections = groupedSections.get(parentKey);
			if (allSections == null) {
				return;
			}

			createItems(parent, parentSection, groupedSections, allSections.get(ACTIVE));
			createItems(parent, parentSection, groupedSections, allSections.get(ARCHIVED));
		}

		private void createItems(HasTreeItems parent, GwtSection parentSection,
				Map<String, Map<String, List<GwtSection>>> groupedSections, List<GwtSection> sections) {
			int numSections = sections.size();
			for (int pos = 0; pos < numSections; pos++) {

				GwtSection section = sections.get(pos);
				boolean archived = Boolean.TRUE.equals(section.getArchived());
				boolean first = pos == 0;
				boolean last = pos + 1 == numSections;

				if (!archived) {
					handleReordering(section, pos);
				}

				TreeItem item = createItem(section, parentSection, parent, groupedSections, first, last);

				if (!archived) {
					addChildSections(item, section, groupedSections);
				}
			}
		}

		private SaveItem createSaveItemForNewSection(String parentKey) {

			final SaveItem saveItem = new SaveItem(parentKey);
			saveItems.put(saveItem, saveItem);
			addButtonUpdateChangeHandler(saveItem);
			addButtonUpdateKeyPressHandler(saveItem);
			return saveItem;
		}

		private SaveItem createSaveItemForSectionEdit(GwtSection section) {
			LOGGER.log(Level.INFO, "save for edit: " + section);
			SaveItem saveItem = new SaveItem(section);
			saveItems.put(saveItem, saveItem);
			addButtonUpdateChangeHandler(saveItem);
			addButtonUpdateKeyPressHandler(saveItem);
			return saveItem;
		}

		private void createDelItem(GwtSection section) {
			LOGGER.info("creating saveitem for section " + section);
			SaveItem saveItem = new SaveItem(section, true);
			saveItems.put(saveItem, saveItem);
		}

		private TreeItem createItem(final GwtSection section, final GwtSection parentSection, final HasTreeItems parent,
				final Map<String, Map<String, List<GwtSection>>> groupedSections, boolean first, boolean last) {

			final Anchor sectionEdit = createEditLink(section);
			final Anchor sectionArchive = createArchiveLink(section);
			final Anchor sectionDel = new Anchor(labels().delete());
			final Anchor sortUp = createSortUpLink(section, first);
			final Anchor sortDown = createSortDownLink(section, last);
			final InlineLabel sectionName = createSectionLabel(section);

			final TreeItem sectionItem = new TreeItem(
					createSectionLabel(sectionName, sectionEdit, sectionArchive, sectionDel, sortUp, sortDown));
			parent.addItem(sectionItem);

			final HandlerRegistration editClickHandler;
			if (sectionEdit != null) {
				editClickHandler = sectionEdit.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (Utils.isLeftClick(event)) {
							handleEdit(section, sectionItem);
						}
					}
				});
			} else {
				editClickHandler = null;
			}

			final HandlerRegistration archiveClickHandler = sectionArchive.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (Utils.isLeftClick(event)) {
						handleArchive(section, sectionName, sectionArchive, sectionItem);
					}
				}
			});

			final HandlerRegistration sortUpClickHandler;
			if (sortUp != null) {
				sortUpClickHandler = sortUp.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (Utils.isLeftClick(event)) {
							handleSortUp(parent, groupedSections, section, sectionItem, parentSection);
						}
					}
				});
			} else {
				sortUpClickHandler = null;
			}

			final HandlerRegistration sortDownClickHandler;
			if (sortDown != null) {
				sortDownClickHandler = sortDown.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (Utils.isLeftClick(event)) {
							handleSortDown(parent, groupedSections, section, sectionItem, parentSection);
						}
					}
				});
			} else {
				sortDownClickHandler = null;
			}

			sectionDel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (Utils.isLeftClick(event)) {
						decisionBox.addOkClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (Utils.isLeftClick(event)) {
									handleDelete(section, sectionName, sectionItem, editClickHandler,
											archiveClickHandler, sortDownClickHandler, sortUpClickHandler);
								}
							}
						});
						decisionBox.center();
					}
				}
			});

			return sectionItem;
		}

		private Anchor createEditLink(GwtSection section) {
			if (Boolean.TRUE.equals(section.getArchived())) {
				return null;
			}
			return new Anchor(labels().change());
		}

		private Anchor createSortDownLink(GwtSection section, boolean last) {
			if (last || Boolean.TRUE.equals(section.getArchived())) {
				return null;
			}
			return new Anchor(Utils.DOWN_ARROW);
		}

		private Anchor createSortUpLink(GwtSection section, boolean first) {
			if (first || Boolean.TRUE.equals(section.getArchived())) {
				return null;
			}
			return new Anchor(Utils.UP_ARROW);
		}

		private InlineLabel createSectionLabel(GwtSection section) {
			final InlineLabel sectionName = new InlineLabel(section.getSectionName());
			if (section.getArchived()) {
				sectionName.addStyleName(Utils.ARCHIVED_STYLE);
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
				SaveItem saveItem = new SaveItem(section);
				if (!saveItems.containsKey(saveItem)) {
					saveItems.put(saveItem, saveItem);
				}
			}
		}

		private HorizontalPanel createSectionLabel(InlineLabel sectionName, Anchor... anchorsArray) {

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

		private void addNewSectionItem(HasTreeItems object, final String parentKey) {

			if (object instanceof TreeItem) {

				Anchor addLabel = new Anchor(labels().create());
				TreeItem addItem = new TreeItem(addLabel);
				final TreeItem sectionItem = (TreeItem) object;
				sectionItem.addItem(addItem);
				addLabel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
							sectionItem.insertItem(1, createSaveItemForNewSection(parentKey));
						}
					}
				});
			} else {
				object.addItem(createSaveItemForNewSection(parentKey));
			}
		}

		private Map<String, Map<String, List<GwtSection>>> groupSections(List<GwtSection> sections) {

			Map<String, Map<String, List<GwtSection>>> grouped = new HashMap<>();

			for (GwtSection section : sections) {
				Map<String, List<GwtSection>> sectionsMap = grouped.get(section.getParentKey());
				if (sectionsMap == null) {
					sectionsMap = new HashMap<>();
					sectionsMap.put(ACTIVE, new ArrayList<GwtSection>());
					sectionsMap.put(ARCHIVED, new ArrayList<GwtSection>());
					grouped.put(section.getParentKey(), sectionsMap);
				}
				sectionsMap.get(Boolean.TRUE.equals(section.getArchived()) ? ARCHIVED : ACTIVE).add(section);
			}

			return grouped;
		}

		private void handleDelete(final GwtSection section, final InlineLabel sectionName, final TreeItem sectionItem,
				final HandlerRegistration... clickHandlers) {

			sectionName.setStylePrimaryName(Utils.DELETED_STYLE);
			sectionItem.setState(false);
			for (HandlerRegistration handler : clickHandlers) {
				if (handler != null) {
					handler.removeHandler();
				}
			}
			createDelItem(section);
			updateButtonPanel();
		}

		private void handleArchive(GwtSection section, InlineLabel sectionName, Anchor sectionArchive,
				TreeItem sectionItem) {
			if (section.getArchived()) {
				sectionName.removeStyleName(Utils.ARCHIVED_STYLE);
				section.setArchived(Boolean.FALSE);
				section.setPos(-1);
				sectionArchive.setText(labels().archive());
			} else {
				sectionName.addStyleName(Utils.ARCHIVED_STYLE);
				section.setArchived(Boolean.TRUE);
				sectionArchive.setText(labels().unarchive());
				sectionItem.removeItems();
			}
			SaveItem saveItem = new SaveItem(section);
			saveItems.put(saveItem, saveItem);
			updateButtonPanel();
		}

		private void handleEdit(final GwtSection section, final TreeItem sectionItem) {
			sectionItem.setWidget(createSaveItemForSectionEdit(section));
		}

		private void handleSortUp(HasTreeItems parent, Map<String, Map<String, List<GwtSection>>> groupedSections,
				GwtSection section, TreeItem sectionItem, GwtSection parentSection) {

			moveSection(section, parent, parentSection, groupedSections, Direction.UP);
		}

		private void handleSortDown(HasTreeItems parent, Map<String, Map<String, List<GwtSection>>> groupedSections,
				GwtSection section, TreeItem sectionItem, GwtSection parentSection) {

			moveSection(section, parent, parentSection, groupedSections, Direction.DOWN);
		}

		private void moveSection(GwtSection section, HasTreeItems parent, GwtSection parentSection,
				Map<String, Map<String, List<GwtSection>>> groupedSections, Direction direction) {

			int pos = (int) section.getPos();

			final List<GwtSection> sections = groupedSections.get(parentSection == null ? null : parentSection.getKey())
					.get(ACTIVE);
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

	@Override
	protected String getPageName() {
		return "SectionAdmin";
	}
}
