package at.lws.wnm.client.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.lws.wnm.client.service.SectionService;
import at.lws.wnm.client.service.SectionServiceAsync;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SectionSelection {

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final Map<Long, List<String[]>> subSectionSelections = new HashMap<Long, List<String[]>>();
	private final PopUp dialogBox;
	private final List<SectionSelectionBox> selectionBoxes;

	public SectionSelection(PopUp dialogBox) {
		this.dialogBox = dialogBox;

		selectionBoxes = new ArrayList<SectionSelectionBox>();
		selectionBoxes.add(new SectionSelectionBox("- Kategorie -"));
		selectionBoxes.add(new SectionSelectionBox("- Bereich -"));
		selectionBoxes.add(new SectionSelectionBox("- Subbereich -"));

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

	public Long getSelectedSectionKey() {

		for (int i = selectionBoxes.size() - 1; i > -1; i--) {
			final Long value = selectionBoxes.get(i).getSelectedValue();
			if (value != null) {
				return value;
			}
		}
		return null;
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

				final Map<Long, List<GwtSection>> children = new HashMap<Long, List<GwtSection>>();
				final SectionSelectionBox parentBox = selectionBoxes.get(0);
				for (GwtSection section : result) {
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

				for (Long parentKey : parentBox.getValues()) {
					final ArrayList<String[]> subSelectionItems = new ArrayList<String[]>();
					subSectionSelections.put(parentKey, subSelectionItems);
					addChildren(children, parentKey, 0, subSelectionItems);
				}
			}

			private void addChildren(Map<Long, List<GwtSection>> children,
					Long parentKey, int depth, List<String[]> subSelectionItems) {
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
				final Long value = main.getSelectedValue();
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
		selectionBoxes.get(0).setSelectedIndex(0);
	}

	public List<SectionSelectionBox> getSectionSelectionBoxes() {
		return Collections.unmodifiableList(selectionBoxes);
	}

}
