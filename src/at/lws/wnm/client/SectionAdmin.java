package at.lws.wnm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SectionAdmin extends VerticalPanel {

	private final SectionServiceAsync sectionService = GWT
			.create(SectionService.class);

	private final Button saveButton;
	private final PopUp dialogBox;
	private final SaveSuccess saveSuccess;
	private Tree tree;

	private Button cancelButton;
	private List<SaveItem> saveItems = new ArrayList<SaveItem>();

	public SectionAdmin() {

		saveButton = new Button(Utils.SAVE);
		cancelButton = new Button(Utils.CANCEL);
		dialogBox = new PopUp();
		saveSuccess = new SaveSuccess();

		tree = new Tree();
		add(tree);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		buttonPanel.setSpacing(Utils.BUTTON_SPACING);
		add(buttonPanel);

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

	private void resetForm() {
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
			final Map<Long, List<GwtSection>> groupedSections = groupSections(sections);
			final List<GwtSection> rootSections = groupedSections.get(null);

			final HasTreeItems parent = tree;
			addChildSections(rootSections, parent, groupedSections);

		}

		private void addChildSections(final List<GwtSection> sections,
				final HasTreeItems parent,
				Map<Long, List<GwtSection>> groupedSections) {
			if (sections == null) {
				return;
			}
			for (final GwtSection section : sections) {
				final TreeItem sectionItem = new TreeItem(
						section.getSectionName());
				final InlineLabel addLabel = new InlineLabel("+");
				final TreeItem addItem = new TreeItem(addLabel);
				sectionItem.addItem(addItem);
				addLabel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
							final SaveItem saveItem = new SaveItem(section
									.getKey());
							saveItems.add(saveItem);
							sectionItem.insertItem(1, saveItem);
						}
					}
				});
				addChildSections(groupedSections.get(section.getKey()),
						sectionItem, groupedSections);
				parent.addItem(sectionItem);
			}
		}

		private Map<Long, List<GwtSection>> groupSections(
				List<GwtSection> sections) {
			final Map<Long, List<GwtSection>> result = new HashMap<Long, List<GwtSection>>();
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
			final List<String> errors = new ArrayList<String>();
			for (SaveItem saveItem : saveItems) {

				final String value = saveItem.getValue();
				if(Utils.isEmpty(value))
				{
					continue;
				}
				final GwtSection section = new GwtSection();
				section.setParentKey(saveItem.parentKey);
				section.setSectionName(value);
				sectionService.storeSection(section, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						errors.add(caught.getLocalizedMessage());
					}

					@Override
					public void onSuccess(Void result) {
						
					}

				});
			}
			if(errors.isEmpty())
			{
				saveSuccess.center();
				saveSuccess.show();
			}
			else
			{
				dialogBox.setErrorMessage(buildErrorMessage(errors));
				dialogBox.setDisableWhileShown(saveButton);
				dialogBox.center();
			}
			resetForm();
		}

		private String buildErrorMessage(List<String> errors) {
			final StringBuilder errorMsg = new StringBuilder();
			errorMsg.append(errors.size()).append(" Fehler sind aufgetreten:<br/><br/>");
			for(String error : errors)
			{
				errorMsg.append(error).append("<br/><br/>");
			}
			return errorMsg.toString();
		}

	}

	public class SaveItem extends TextBox {

		private final Long parentKey;

		public SaveItem(Long parentKey) {
			this.parentKey = parentKey;
		}

	}

}
