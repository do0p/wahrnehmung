package at.brandl.lws.notice.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.client.Labels;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.shared.service.SectionService;
import at.brandl.lws.notice.shared.service.SectionServiceAsync;

public class SectionSelection {

	private final Labels labels = GWT.create(Labels.class);
	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final Map<String, List<String[]>> subSectionSelections = new HashMap<String, List<String[]>>();
	private final PopUp dialogBox;
	private final List<SectionSelectionBox> selectionBoxes;
	private final Map<String, List<Integer>> sectionSelectionMap = new HashMap<String, List<Integer>>();
	private boolean updated;
	private boolean archived;

	private String selectedSectionKey;

	public SectionSelection(PopUp dialogBox) {
		this.dialogBox = dialogBox;

		selectionBoxes = new ArrayList<SectionSelectionBox>();
		selectionBoxes.add(createSelection(labels.category()));
		selectionBoxes.add(createSelection(labels.section()));
		selectionBoxes.add(createSelection(labels.subSection()));

		final Iterator<SectionSelectionBox> iterator = selectionBoxes
				.iterator();
		SectionSelectionBox sectionBox = iterator.next();
		while (iterator.hasNext()) {
			SectionSelectionBox subSectionBox = iterator.next();
			sectionBox.addChangeHandler(new SectionChangeHandler(sectionBox,
					subSectionBox));
			subSectionBox.setEnabled(false);
			sectionBox = subSectionBox;
		}

		createSectionSelections();
	}

	private SectionSelectionBox createSelection(String defaultText) {
		return new SectionSelectionBox("- " + defaultText + " -");
	}

	public String getSelectedSectionKey() {

		for (int i = selectionBoxes.size() - 1; i > -1; i--) {
			final String value = selectionBoxes.get(i).getSelectedValue();
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public int getSelectedSectionKeyLevel() {
		int size = selectionBoxes.size();
		for (int i = size - 1; i > -1; i--) {
			final String value = selectionBoxes.get(i).getSelectedValue();
			if (value != null) {
				return i;
			}
		}
		return -1;
	}

	private void createSectionSelections() {
		sectionService.querySections(new AsyncCallback<List<GwtSection>>() {

			@Override
			public void onFailure(Throwable caught) {
				dialogBox.setErrorMessage();
				dialogBox.center();
			}

			@Override
			public void onSuccess(List<GwtSection> result) {

				for(SectionSelectionBox selectionBox : selectionBoxes) {
					selectionBox.clear();
				}
				
				final Map<String, List<GwtSection>> children = new HashMap<String, List<GwtSection>>();
				final SectionSelectionBox parentBox = selectionBoxes.get(0);
				for (GwtSection section : result) {
					if (!archived && section.getArchived()) {
						continue;
					}
					if (section.getParentKey() == null) {
						parentBox.addItem(section.getSectionName(), section
								.getKey().toString());

					} else {
						List<GwtSection> sections = children.get(section
								.getParentKey());
						if (sections == null) {
							sections = new ArrayList<GwtSection>();
							children.put(section.getParentKey(), sections);
						}
						sections.add(section);
					}
				}

				for (String parentKey : parentBox.getValues()) {
					final ArrayList<String[]> subSelectionItems = new ArrayList<String[]>();
					subSectionSelections.put(parentKey, subSelectionItems);
					addChildren(children, parentKey, 0, subSelectionItems);
				}

				for (int i = 1; i < parentBox.getItemCount(); i++) {
					final ArrayList<Integer> indexList = new ArrayList<Integer>(
							Arrays.asList(Integer.valueOf(i)));
					final String parentKey = parentBox.getValue(i);
					sectionSelectionMap.put(parentKey, indexList);
					addChildSectionIndices(parentKey, children, indexList, 0);
				}

				updated = true;
				if (selectedSectionKey != null) {
					setSelectedInternal(selectedSectionKey);
					selectedSectionKey = null;
				}
			}

			private void addChildSectionIndices(String parentKey,
					Map<String, List<GwtSection>> children,
					List<Integer> indexList, int preset) {
				final List<GwtSection> list = children.get(parentKey);
				if (list == null) {
					return;
				}
				for (int i = 0; i < list.size(); i++) {
					final List<Integer> childIndexList;
					childIndexList = new ArrayList<Integer>(indexList.subList(
							0,
							Utils.min(selectionBoxes.size() - 1,
									indexList.size())));
					final int pos = i + 1 + preset;
					childIndexList.add(Integer.valueOf(pos));
					final String newParentKey = list.get(i).getKey();
					sectionSelectionMap.put(newParentKey, childIndexList);
					addChildSectionIndices(
							newParentKey,
							children,
							childIndexList,
							childIndexList.size() == selectionBoxes.size() ? pos
									: 0);
				}

			}

			private void addChildren(Map<String, List<GwtSection>> children,
					String parentKey, int depth,
					List<String[]> subSelectionItems) {
				final List<GwtSection> sections = children.get(parentKey);
				if (sections != null) {
					for (GwtSection section : sections) {
						subSelectionItems.add(new String[] {
								createPrefix(depth) + section.getSectionName(),
								section.getKey().toString() });
						if (depth < selectionBoxes.size() - 2) {
							final ArrayList<String[]> nextSubSelectionItems = new ArrayList<String[]>();
							subSectionSelections.put(section.getKey(),
									nextSubSelectionItems);
							addChildren(children, section.getKey(), depth + 1,
									nextSubSelectionItems);
						} else {
							addChildren(children, section.getKey(), depth + 1,
									subSelectionItems);
						}
					}
				}
			}

			private String createPrefix(int depth) {
				depth = depth - selectionBoxes.size() + 2;
				final StringBuilder prefix = new StringBuilder();
				for (int i = 1; i < depth; i++) {
					prefix.append("--");
				}
				if (depth > 0) {
					prefix.append("->");
				}
				return prefix.toString();
			}
		});

	}

	private class SectionChangeHandler implements ChangeHandler {

		private final SectionSelectionBox main;
		private final SectionSelectionBox sub;

		private SectionChangeHandler(SectionSelectionBox sectionSelection,
				SectionSelectionBox subSectionSelection) {
			this.sub = subSectionSelection;
			this.main = sectionSelection;

		}

		@Override
		public void onChange(ChangeEvent event) {
			sub.clear();
			sub.setEnabled(false);
			sub.fireEvent(event);
			if (main.getSelectedIndex() != -1) {
				final String value = main.getSelectedValue();
				if (value != null) {
					final List<String[]> subSelectionItems = subSectionSelections
							.get(value);
					if (subSelectionItems != null
							&& !subSelectionItems.isEmpty()) {
						for (String[] entry : subSelectionItems) {
							sub.addItem(entry[0], entry[1]);
						}
						sub.setEnabled(true);
					}
				}
			}
		}

	}

	public void reset() {
		for (SectionSelectionBox box : selectionBoxes) {
			box.setSelectedIndex(0);
		}
	}

	public List<SectionSelectionBox> getSectionSelectionBoxes() {
		return Collections.unmodifiableList(selectionBoxes);
	}

	public void setSelected(String sectionKey) {

		if (updated) {
			setSelectedInternal(sectionKey);
		} else {
			selectedSectionKey = sectionKey;
		}
	}

	private void setSelectedInternal(String sectionKey) {
		final List<Integer> indices = sectionSelectionMap.get(sectionKey);
		if(indices == null || indices.isEmpty()) {
			return;
		}
		for (int i = 0; i < indices.size(); i++) {
			final SectionSelectionBox sectionSelectionBox = selectionBoxes
					.get(i);
			sectionSelectionBox.setEnabled(true);
			sectionSelectionBox.setSelectedIndex(indices.get(i).intValue());
			ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(),
					sectionSelectionBox);

		}
	}

	public void addChangeHandler(ChangeHandler changeHanlder) {
		for (SectionSelectionBox sectionSelectionBox : selectionBoxes) {
			sectionSelectionBox.addChangeHandler(changeHanlder);
		}
	}

	public boolean hasSelection() {
		return getSelectedSectionKey() != null;
	}


	public void setIncludeArchived(boolean archived) {

		if (!this.archived == archived) {
			this.archived = archived;
			createSectionSelections();
		}
	}

}
