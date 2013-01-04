package at.lws.wnm.client;

import java.util.Set;

import at.lws.wnm.client.utils.BeobachtungsTable;
import at.lws.wnm.client.utils.NameSelection;
import at.lws.wnm.client.utils.PopUp;
import at.lws.wnm.client.utils.Print;
import at.lws.wnm.client.utils.SectionSelection;
import at.lws.wnm.client.utils.Utils;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class Search extends VerticalPanel
{
  private final Labels labels = (Labels) GWT.create(Labels.class);	
	
  private final BeobachtungsTable table;
  private final BeobachtungsFilter filter;
  private final NameSelection nameSelection;
  private final SectionSelection sectionSelection;
  private final MultiSelectionModel<GwtBeobachtung> selectionModel;

  public Search(Authorization authorization, int width)
  {
    PopUp dialogBox = new PopUp();
    final RichTextArea textArea = new RichTextArea();
    this.filter = new BeobachtungsFilter();
    this.nameSelection = new NameSelection(dialogBox);
    this.sectionSelection = new SectionSelection(dialogBox, null);
    Button sendButton = new Button(labels.filter());
    sendButton.addClickHandler(new FilterButtonHandler());
    sendButton.addStyleName(Utils.SEND_BUTTON_STYLE);
    this.selectionModel = createSelectionModel(textArea);
    this.table = new BeobachtungsTable(authorization, this.selectionModel, this.filter, 
      dialogBox);
    this.table.addCellPreviewHandler(new CellPreviewEvent.Handler<GwtBeobachtung>()
    {
      public void onCellPreview(CellPreviewEvent<GwtBeobachtung> event) {
        textArea.setHTML(((GwtBeobachtung)event.getValue()).getText());
      }
    });
    layout(width, textArea, sendButton);
  }

  private void layout(int width, RichTextArea textArea, Button sendButton)
  {
    CellPanel filterBox = new HorizontalPanel();
    filterBox.setSpacing(5);
    add(filterBox);
    Utils.formatLeftCenter(filterBox, this.nameSelection, NameSelection.WIDTH, 
      20);

    for (ListBox selectionBox : this.sectionSelection.getSectionSelectionBoxes()) {
      Utils.formatLeftCenter(filterBox, selectionBox, 
        135, 20);
    }
    Utils.formatLeftCenter(filterBox, sendButton, 80, 
      40);
    add(this.table);
    SimplePager pager = new SimplePager();
    pager.setDisplay(this.table);
    add(pager);
    textArea.setSize(width + Utils.PIXEL, Utils.TEXT_AREA_WIDTH + Utils.PIXEL);
    add(textArea);
    Utils.formatLeftCenter(this, createButtonContainer(), width, 
      40);
  }

  private MultiSelectionModel<GwtBeobachtung> createSelectionModel(final RichTextArea textArea) {
    final MultiSelectionModel<GwtBeobachtung> selectionModel = new MultiSelectionModel<GwtBeobachtung>();
    selectionModel
      .addSelectionChangeHandler(new SelectionChangeEvent.Handler()
    {
      public void onSelectionChange(SelectionChangeEvent event)
      {
        Set<GwtBeobachtung> selectedObjects = selectionModel.getSelectedSet();
        if (!selectedObjects.isEmpty())
          textArea.setHTML(((GwtBeobachtung)selectedObjects.iterator().next())
            .getText());
      }
    });
    return selectionModel;
  }

  private HorizontalPanel createButtonContainer() {
    HorizontalPanel buttonContainer = new HorizontalPanel();
    buttonContainer.setWidth(Utils.BUTTON_CONTAINER_WIDTH + Utils.PIXEL);

    Button printButton = new Button(labels.print());
    printButton.addClickHandler(new ClickHandler()
    {
      public void onClick(ClickEvent event)
      {
        Set<GwtBeobachtung> selectedSet = Search.this.selectionModel
          .getSelectedSet();
        if (!selectedSet.isEmpty())
        {
          Print.it(Utils.createPrintHtml(selectedSet));
        }
      }
    });
    printButton.addStyleName(Utils.SEND_BUTTON_STYLE);

    Utils.formatLeftCenter(buttonContainer, printButton, 
      80, 40);

    return buttonContainer;
  }
  public class FilterButtonHandler implements ClickHandler {
    public FilterButtonHandler() {
    }

    public void onClick(ClickEvent event) {
      Search.this.filter.setChildKey(Search.this.nameSelection.getSelectedChildKey());
      Search.this.filter.setSectionKey(Search.this.sectionSelection.getSelectedSectionKey());
      Search.this.table.updateTable();
    }
  }
}