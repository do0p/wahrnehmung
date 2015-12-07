package at.brandl.lws.notice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.brandl.lws.notice.client.utils.DecisionBox;
import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtSection;
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

	private static enum Direction {
		
		UP(-1), DOWN(1);

		private final int delta;
		
		private Direction(int delta) {
			this.delta = delta;
		}
		
		public int getDelta() {
			return delta;
		}
	}

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final Tree tree;

	private final List<SaveItem> saveItems = new ArrayList<SaveItem>();
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

	@Override
	void reset() {
		saveItems.clear();
		tree.clear();
		refresh();
	}

	private class TreeBuilder extends ErrorReportingCallback<List<GwtSection>> {

		@Override
		public void onSuccess(List<GwtSection> sections) {
			final Map<String, List<GwtSection>> groupedSections = groupSections(sections);
			final List<GwtSection> rootSections = groupedSections.get(null);

			addChildSections(null, rootSections, tree, groupedSections);
			// tree.add(createSaveItem((String) null));
			updateButtonPanel();
		}

		private SaveItem createSaveItem(String parentKey) {
			final SaveItem saveItem = new SaveItem(parentKey);
			saveItems.add(saveItem);
			addButtonUpdateChangeHandler(saveItem);
			addButtonUpdateKeyPressHandler(saveItem);
			return saveItem;
		}

		protected SaveItem createSaveItem(GwtSection section) {
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

		private void addChildSections(final GwtSection parentSection,
				final List<GwtSection> sections, final HasTreeItems parent,
				final Map<String, List<GwtSection>> groupedSections) {

			addPlusItem(parentSection == null ? null : parentSection.getKey(),
					parent);

			if (sections == null) {
				return;
			}

			int numSections = sections.size();
			for (int i = 0; i < numSections; i++) {
				boolean first = i == 0;
				boolean last = i + 1 == numSections;
				final GwtSection section = sections.get(i);
				if (section.getPos() != i) {
					section.setPos(i);
					saveItems.add(new SaveItem(section));
				}
				final InlineLabel sectionName = new InlineLabel(
						section.getSectionName());
				String archiveLabel;
				if (section.getArchived()) {
					sectionName.setStylePrimaryName(Utils.ARCHIVED_STYLE);
					archiveLabel = labels().unarchive();
				} else {
					archiveLabel = labels().archive();
				}
				Anchor sectionEdit = new Anchor(labels().change());
				final Anchor sectionArchive = new Anchor(archiveLabel);
				Anchor sectionDel = new Anchor(labels().delete());
				Anchor sortUp = null;
				if (!first) {
					sortUp = new Anchor(Utils.UP_ARROW);
				}
				Anchor sortDown = null;
				if (!last) {
					sortDown = new Anchor(Utils.DOWN_ARROW);
				}
				final TreeItem sectionItem = new TreeItem(createSectionLabel(
						sectionName, sectionEdit, sectionArchive, sectionDel,
						sortUp, sortDown));

				final HandlerRegistration editClickHandler = sectionEdit
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (isLeftClick(event)) {
									handleEdit(section, sectionItem);
								}
							}

						});

				final HandlerRegistration archiveClickHandler = sectionArchive
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (isLeftClick(event)) {
									handleArchive(section, sectionName,
											sectionArchive, sectionItem);
								}
							}

						});

				sectionDel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (isLeftClick(event)) {
							decisionBox.addOkClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									if (isLeftClick(event)) {
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
							if (isLeftClick(event)) {
								handleSortUp(parent, groupedSections, sections,
										section, sectionItem, parentSection);
							}
						}
					});
				}
				if (sortDown != null) {
					sortDown.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (isLeftClick(event)) {
								handleSortDown(parent, groupedSections,
										sections, section, sectionItem,
										parentSection);
							}
						}
					});
				}

				if (!section.getArchived()) {
					// addPlusItem(section.getKey(), sectionItem);
					addChildSections(section,
							groupedSections.get(section.getKey()), sectionItem,
							groupedSections);
				}
				parent.addItem(sectionItem);
			}
		}

		private boolean isLeftClick(ClickEvent event) {
			return NativeEvent.BUTTON_LEFT == event.getNativeButton();
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

		private void addPlusItem(final String parentKey,
				final HasTreeItems object) {
			if (object instanceof TreeItem) {
				final Anchor addLabel = new Anchor(labels().create());
				final TreeItem addItem = new TreeItem(addLabel);
				final TreeItem sectionItem = (TreeItem) object;
				sectionItem.addItem(addItem);
				addLabel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
							sectionItem
									.insertItem(1, createSaveItem(parentKey));
						}
					}
				});
			} else if (object instanceof Tree) {
				Tree tree = (Tree) object;
				tree.add(createSaveItem((String) null));
			}
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
			createSaveItem(section);
			updateButtonPanel();
		}

		private void handleEdit(final GwtSection section,
				final TreeItem sectionItem) {
			sectionItem.setWidget(createSaveItem(section));
		}

		private void handleSortUp(HasTreeItems parent,
				Map<String, List<GwtSection>> groupedSections,
				List<GwtSection> sections, GwtSection section,
				TreeItem sectionItem, GwtSection parentSection) {

			moveSection(section, sections, parent, parentSection,
					groupedSections, Direction.UP);
		}


		private void handleSortDown(HasTreeItems parent,
				Map<String, List<GwtSection>> groupedSections,
				List<GwtSection> sections, GwtSection section,
				TreeItem sectionItem, GwtSection parentSection) {

			moveSection(section, sections, parent, parentSection,
					groupedSections, Direction.DOWN);
		}
		

		private void moveSection(GwtSection section, List<GwtSection> sections,
				HasTreeItems parent, GwtSection parentSection,
				Map<String, List<GwtSection>> groupedSections, Direction direction) {

			int pos = (int) section.getPos();
			sections.remove(pos);
			sections.add(pos + direction.getDelta(), section);
			parent.removeItems();
			addChildSections(parentSection, sections, parent, groupedSections);
			updateButtonPanel();
		}
	}

	@Override
	void save() {
		final List<SaveItem> tmpSaveItems = new ArrayList<SaveItem>(saveItems);

		final Set<String> processed = deleteSections(tmpSaveItems);

		List<GwtSection> sections = new ArrayList<GwtSection>();
		for (final SaveItem saveItem : tmpSaveItems) {

			if (saveItem.section != null
					&& processed.contains(saveItem.section.getKey())) {
				continue;
			}

			final String value = saveItem.getValue();
			if (at.brandl.lws.notice.shared.Utils.isEmpty(value)) {
				continue;
			}

			sections.add(createSectionFromSaveItem(saveItem, value));

			if (saveItem.section != null) {
				processed.add(saveItem.section.getKey());
			}
		}

		if (!sections.isEmpty()) {
			sectionService.storeSection(sections, new AsyncVoidCallBack());
		}
	}

	private Set<String> deleteSections(List<SaveItem> tmpSaveItems) {

		final Set<String> alreadySaved = new HashSet<>();

		for (final SaveItem saveItem : tmpSaveItems) {

			if (saveItem.section == null) {
				continue;
			}

			String key = saveItem.section.getKey();
			if (alreadySaved.contains(key)) {
				continue;
			}

			if (saveItem.delete) {
				sectionService.deleteSection(saveItem.section,
						new AsyncVoidCallBack());
				alreadySaved.add(key);
			}
		}

		return alreadySaved;
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
			reset();
		}

		@Override
		public void onSuccess(Void result) {
			reset();
		}

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

}
