package at.lws.wnm.client.utils;

import java.util.HashMap;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RichTextToolbar extends Composite
{
  private static final String HTTP_STATIC_ICONS_GIF = "/icons.gif";
  private static final String CSS_ROOT_NAME = "RichTextToolbar";
  public static final HashMap<String, String> GUI_COLORLIST = new HashMap<String, String>();
  public static final HashMap<String, String> GUI_FONTLIST;
  private static final String HTML_STYLE_CLOSE_SPAN = "</span>";
  private static final String HTML_STYLE_CLOSE_DIV = "</div>";
  private static final String HTML_STYLE_OPEN_BOLD = "<span style=\"font-weight: bold;\">";
  private static final String HTML_STYLE_OPEN_ITALIC = "<span style=\"font-weight: italic;\">";
  private static final String HTML_STYLE_OPEN_UNDERLINE = "<span style=\"font-weight: underline;\">";
  private static final String HTML_STYLE_OPEN_LINETHROUGH = "<span style=\"font-weight: line-through;\">";
  private static final String HTML_STYLE_OPEN_ALIGNLEFT = "<div style=\"text-align: left;\">";
  private static final String HTML_STYLE_OPEN_ALIGNCENTER = "<div style=\"text-align: center;\">";
  private static final String HTML_STYLE_OPEN_ALIGNRIGHT = "<div style=\"text-align: right;\">";
  private static final String HTML_STYLE_OPEN_INDENTRIGHT = "<div style=\"margin-left: 40px;\">";
  private static final String HTML_STYLE_OPEN_SUBSCRIPT = "<sub>";
  private static final String HTML_STYLE_CLOSE_SUBSCRIPT = "</sub>";
  private static final String HTML_STYLE_OPEN_SUPERSCRIPT = "<sup>";
  private static final String HTML_STYLE_CLOSE_SUPERSCRIPT = "</sup>";
  private static final String HTML_STYLE_OPEN_ORDERLIST = "<ol><li>";
  private static final String HTML_STYLE_CLOSE_ORDERLIST = "</ol></li>";
  private static final String HTML_STYLE_OPEN_UNORDERLIST = "<ul><li>";
  private static final String HTML_STYLE_CLOSE_UNORDERLIST = "</ul></li>";
  private static final String HTML_STYLE_HLINE = "<hr style=\"width: 100%; height: 2px;\">";
  private static final String GUI_DIALOG_INSERTURL = "Enter a link URL:";
  private static final String GUI_DIALOG_IMAGEURL = "Enter an image URL:";
  private static final String GUI_LISTNAME_COLORS = "Colors";
  private static final String GUI_LISTNAME_FONTS = "Fonts";
  private static final String GUI_HOVERTEXT_SWITCHVIEW = "Switch View HTML/Source";
  private static final String GUI_HOVERTEXT_REMOVEFORMAT = "Remove Formatting";
  private static final String GUI_HOVERTEXT_IMAGE = "Insert Image";
  private static final String GUI_HOVERTEXT_HLINE = "Insert Horizontal Line";
  private static final String GUI_HOVERTEXT_BREAKLINK = "Break Link";
  private static final String GUI_HOVERTEXT_LINK = "Generate Link";
  private static final String GUI_HOVERTEXT_IDENTLEFT = "Ident Left";
  private static final String GUI_HOVERTEXT_IDENTRIGHT = "Ident Right";
  private static final String GUI_HOVERTEXT_UNORDERLIST = "Unordered List";
  private static final String GUI_HOVERTEXT_ORDERLIST = "Ordered List";
  private static final String GUI_HOVERTEXT_ALIGNRIGHT = "Align Right";
  private static final String GUI_HOVERTEXT_ALIGNCENTER = "Align Center";
  private static final String GUI_HOVERTEXT_ALIGNLEFT = "Align Left";
  private static final String GUI_HOVERTEXT_SUPERSCRIPT = "Superscript";
  private static final String GUI_HOVERTEXT_SUBSCRIPT = "Subscript";
  private static final String GUI_HOVERTEXT_STROKE = "Stroke";
  private static final String GUI_HOVERTEXT_UNDERLINE = "Underline";
  private static final String GUI_HOVERTEXT_ITALIC = "Italic";
  private static final String GUI_HOVERTEXT_BOLD = "Bold";
  private VerticalPanel outer;
  private HorizontalPanel topPanel;
  private HorizontalPanel bottomPanel;
  private RichTextArea styleText;
  private RichTextArea.Formatter styleTextFormatter;
  private EventHandler evHandler;
  private ToggleButton bold;
  private ToggleButton italic;
  private ToggleButton underline;
  private ToggleButton stroke;
  private ToggleButton subscript;
  private ToggleButton superscript;
  private PushButton alignleft;
  private PushButton alignmiddle;
  private PushButton alignright;
  private PushButton orderlist;
  private PushButton unorderlist;
  private PushButton indentleft;
  private PushButton indentright;
  private PushButton generatelink;
  private PushButton breaklink;
  private PushButton insertline;
  private PushButton insertimage;
  private PushButton removeformatting;
  private ToggleButton texthtml;
  private ListBox fontlist;
  private ListBox colorlist;

  static
  {
    GUI_COLORLIST.put("White", "#FFFFFF");
    GUI_COLORLIST.put("Black", "#000000");
    GUI_COLORLIST.put("Red", "red");
    GUI_COLORLIST.put("Green", "green");
    GUI_COLORLIST.put("Yellow", "yellow");
    GUI_COLORLIST.put("Blue", "blue");

    GUI_FONTLIST = new HashMap<String, String>();

    GUI_FONTLIST.put("Times New Roman", "Times New Roman");
    GUI_FONTLIST.put("Arial", "Arial");
    GUI_FONTLIST.put("Courier New", "Courier New");
    GUI_FONTLIST.put("Georgia", "Georgia");
    GUI_FONTLIST.put("Trebuchet", "Trebuchet");
    GUI_FONTLIST.put("Verdana", "Verdana");
  }

  public RichTextToolbar(RichTextArea richtext)
  {
    this.outer = new VerticalPanel();

    this.topPanel = new HorizontalPanel();
    this.bottomPanel = new HorizontalPanel();
    this.topPanel.setStyleName("RichTextToolbar");
    this.bottomPanel.setStyleName("RichTextToolbar");

    this.styleText = richtext;
    this.styleTextFormatter = this.styleText.getFormatter();

    this.topPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
    this.bottomPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

    this.outer.add(this.topPanel);
    this.outer.add(this.bottomPanel);

    this.outer.setWidth("100%");
    this.outer.setStyleName("RichTextToolbar");
    initWidget(this.outer);

    this.evHandler = new EventHandler();

    this.styleText.addKeyUpHandler(this.evHandler);
    this.styleText.addClickHandler(this.evHandler);

    buildTools();
  }

  public static native JsArrayString getSelection(Element paramElement);

  private void changeHtmlStyle(String startTag, String stopTag)
  {
    JsArrayString tx = getSelection(this.styleText.getElement());
    String txbuffer = this.styleText.getText();
    Integer startpos = Integer.valueOf(Integer.parseInt(tx.get(1)));
    String selectedText = tx.get(0);
    this.styleText.setText(txbuffer.substring(0, startpos.intValue()) + startTag + selectedText + stopTag + txbuffer.substring(startpos.intValue() + selectedText.length()));
  }

  private Boolean isHTMLMode()
  {
    return Boolean.valueOf(this.texthtml.isDown());
  }

  private void updateStatus()
  {
    if (this.styleTextFormatter != null) {
      this.bold.setDown(this.styleTextFormatter.isBold());
      this.italic.setDown(this.styleTextFormatter.isItalic());
      this.underline.setDown(this.styleTextFormatter.isUnderlined());
      this.subscript.setDown(this.styleTextFormatter.isSubscript());
      this.superscript.setDown(this.styleTextFormatter.isSuperscript());
      this.stroke.setDown(this.styleTextFormatter.isStrikethrough());
    }

    if (isHTMLMode().booleanValue()) {
      this.removeformatting.setEnabled(false);
      this.indentleft.setEnabled(false);
      this.breaklink.setEnabled(false);
    } else {
      this.removeformatting.setEnabled(true);
      this.indentleft.setEnabled(true);
      this.breaklink.setEnabled(true);
    }
  }

  private void buildTools()
  {
    this.topPanel.add(this.bold = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(20), Integer.valueOf(20), "Bold"));
    this.topPanel.add(this.italic = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(60), Integer.valueOf(20), Integer.valueOf(20), "Italic"));
    this.topPanel.add(this.underline = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(140), Integer.valueOf(20), Integer.valueOf(20), "Underline"));
    this.topPanel.add(this.stroke = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(120), Integer.valueOf(20), Integer.valueOf(20), "Stroke"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.subscript = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(600), Integer.valueOf(20), Integer.valueOf(20), "Subscript"));
    this.topPanel.add(this.superscript = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(620), Integer.valueOf(20), Integer.valueOf(20), "Superscript"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.alignleft = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(460), Integer.valueOf(20), Integer.valueOf(20), "Align Left"));
    this.topPanel.add(this.alignmiddle = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(420), Integer.valueOf(20), Integer.valueOf(20), "Align Center"));
    this.topPanel.add(this.alignright = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(480), Integer.valueOf(20), Integer.valueOf(20), "Align Right"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.orderlist = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(80), Integer.valueOf(20), Integer.valueOf(20), "Ordered List"));
    this.topPanel.add(this.unorderlist = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(20), Integer.valueOf(20), Integer.valueOf(20), "Unordered List"));
    this.topPanel.add(this.indentright = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(400), Integer.valueOf(20), Integer.valueOf(20), "Ident Right"));
    this.topPanel.add(this.indentleft = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(540), Integer.valueOf(20), Integer.valueOf(20), "Ident Left"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.generatelink = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(500), Integer.valueOf(20), Integer.valueOf(20), "Generate Link"));
    this.topPanel.add(this.breaklink = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(640), Integer.valueOf(20), Integer.valueOf(20), "Break Link"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.insertline = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(360), Integer.valueOf(20), Integer.valueOf(20), "Insert Horizontal Line"));
    this.topPanel.add(this.insertimage = createPushButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(380), Integer.valueOf(20), Integer.valueOf(20), "Insert Image"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.removeformatting = createPushButton("/icons.gif", Integer.valueOf(20), Integer.valueOf(460), Integer.valueOf(20), Integer.valueOf(20), "Remove Formatting"));
    this.topPanel.add(new HTML("&nbsp;"));
    this.topPanel.add(this.texthtml = createToggleButton("/icons.gif", Integer.valueOf(0), Integer.valueOf(260), Integer.valueOf(20), Integer.valueOf(20), "Switch View HTML/Source"));

    this.bottomPanel.add(this.fontlist = createFontList());
    this.bottomPanel.add(new HTML("&nbsp;"));
    this.bottomPanel.add(this.colorlist = createColorList());
  }

  private ToggleButton createToggleButton(String url, Integer top, Integer left, Integer width, Integer height, String tip)
  {
    Image extract = new Image(url, left.intValue(), top.intValue(), width.intValue(), height.intValue());
    ToggleButton tb = new ToggleButton(extract);
    tb.setHeight(height + "px");
    tb.setWidth(width + "px");
    tb.addClickHandler(this.evHandler);
    if (tip != null) {
      tb.setTitle(tip);
    }
    return tb;
  }

  private PushButton createPushButton(String url, Integer top, Integer left, Integer width, Integer height, String tip)
  {
    Image extract = new Image(url, left.intValue(), top.intValue(), width.intValue(), height.intValue());
    PushButton tb = new PushButton(extract);
    tb.setHeight(height + "px");
    tb.setWidth(width + "px");
    tb.addClickHandler(this.evHandler);
    if (tip != null) {
      tb.setTitle(tip);
    }
    return tb;
  }

  private ListBox createFontList()
  {
    ListBox mylistBox = new ListBox();
    mylistBox.addChangeHandler(this.evHandler);
    mylistBox.setVisibleItemCount(1);

    mylistBox.addItem("Fonts");
    for (String name : GUI_FONTLIST.keySet()) {
      mylistBox.addItem(name, (String)GUI_FONTLIST.get(name));
    }

    return mylistBox;
  }

  private ListBox createColorList()
  {
    ListBox mylistBox = new ListBox();
    mylistBox.addChangeHandler(this.evHandler);
    mylistBox.setVisibleItemCount(1);

    mylistBox.addItem("Colors");
    for (String name : GUI_COLORLIST.keySet()) {
      mylistBox.addItem(name, (String)GUI_COLORLIST.get(name));
    }

    return mylistBox;
  }

  private class EventHandler
    implements ClickHandler, KeyUpHandler, ChangeHandler
  {
    private EventHandler()
    {
    }

    public void onClick(ClickEvent event)
    {
      if (event.getSource().equals(RichTextToolbar.this.bold)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"font-weight: bold;\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleBold();
      }
      else if (event.getSource().equals(RichTextToolbar.this.italic)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"font-weight: italic;\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleItalic();
      }
      else if (event.getSource().equals(RichTextToolbar.this.underline)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"font-weight: underline;\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleUnderline();
      }
      else if (event.getSource().equals(RichTextToolbar.this.stroke)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"font-weight: line-through;\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleStrikethrough();
      }
      else if (event.getSource().equals(RichTextToolbar.this.subscript)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<sub>", "</sub>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleSubscript();
      }
      else if (event.getSource().equals(RichTextToolbar.this.superscript)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<sup>", "</sup>");
        else
          RichTextToolbar.this.styleTextFormatter.toggleSuperscript();
      }
      else if (event.getSource().equals(RichTextToolbar.this.alignleft)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<div style=\"text-align: left;\">", "</div>");
        else
          RichTextToolbar.this.styleTextFormatter.setJustification(RichTextArea.Justification.LEFT);
      }
      else if (event.getSource().equals(RichTextToolbar.this.alignmiddle)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<div style=\"text-align: center;\">", "</div>");
        else
          RichTextToolbar.this.styleTextFormatter.setJustification(RichTextArea.Justification.CENTER);
      }
      else if (event.getSource().equals(RichTextToolbar.this.alignright)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<div style=\"text-align: right;\">", "</div>");
        else
          RichTextToolbar.this.styleTextFormatter.setJustification(RichTextArea.Justification.RIGHT);
      }
      else if (event.getSource().equals(RichTextToolbar.this.orderlist)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<ol><li>", "</ol></li>");
        else
          RichTextToolbar.this.styleTextFormatter.insertOrderedList();
      }
      else if (event.getSource().equals(RichTextToolbar.this.unorderlist)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<ul><li>", "</ul></li>");
        else
          RichTextToolbar.this.styleTextFormatter.insertUnorderedList();
      }
      else if (event.getSource().equals(RichTextToolbar.this.indentright)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<div style=\"margin-left: 40px;\">", "</div>");
        else
          RichTextToolbar.this.styleTextFormatter.rightIndent();
      }
      else if (event.getSource().equals(RichTextToolbar.this.indentleft)) {
        if (!RichTextToolbar.this.isHTMLMode().booleanValue())
        {
          RichTextToolbar.this.styleTextFormatter.leftIndent();
        }
      } else if (event.getSource().equals(RichTextToolbar.this.generatelink)) {
        String url = Window.prompt("Enter a link URL:", "http://");
        if (url != null) {
          if (RichTextToolbar.this.isHTMLMode().booleanValue())
            RichTextToolbar.this.changeHtmlStyle("<a href=\"" + url + "\">", "</a>");
          else
            RichTextToolbar.this.styleTextFormatter.createLink(url);
        }
      }
      else if (event.getSource().equals(RichTextToolbar.this.breaklink)) {
        if (!RichTextToolbar.this.isHTMLMode().booleanValue())
        {
          RichTextToolbar.this.styleTextFormatter.removeLink();
        }
      } else if (event.getSource().equals(RichTextToolbar.this.insertimage)) {
        String url = Window.prompt("Enter an image URL:", "http://");
        if (url != null) {
          if (RichTextToolbar.this.isHTMLMode().booleanValue())
            RichTextToolbar.this.changeHtmlStyle("<img src=\"" + url + "\">", "");
          else
            RichTextToolbar.this.styleTextFormatter.insertImage(url);
        }
      }
      else if (event.getSource().equals(RichTextToolbar.this.insertline)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<hr style=\"width: 100%; height: 2px;\">", "");
        else
          RichTextToolbar.this.styleTextFormatter.insertHorizontalRule();
      }
      else if (event.getSource().equals(RichTextToolbar.this.removeformatting)) {
        if (!RichTextToolbar.this.isHTMLMode().booleanValue())
        {
          RichTextToolbar.this.styleTextFormatter.removeFormat();
        }
      } else if (event.getSource().equals(RichTextToolbar.this.texthtml)) {
        if (RichTextToolbar.this.texthtml.isDown())
          RichTextToolbar.this.styleText.setText(RichTextToolbar.this.styleText.getHTML());
        else
          RichTextToolbar.this.styleText.setHTML(RichTextToolbar.this.styleText.getText());
      } else {
        event.getSource().equals(RichTextToolbar.this.styleText);
      }

      RichTextToolbar.this.updateStatus();
    }

    public void onKeyUp(KeyUpEvent event) {
      RichTextToolbar.this.updateStatus();
    }

    public void onChange(ChangeEvent event) {
      if (event.getSource().equals(RichTextToolbar.this.fontlist)) {
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"font-family: " + RichTextToolbar.this.fontlist.getValue(RichTextToolbar.this.fontlist.getSelectedIndex()) + ";\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.setFontName(RichTextToolbar.this.fontlist.getValue(RichTextToolbar.this.fontlist.getSelectedIndex()));
      }
      else if (event.getSource().equals(RichTextToolbar.this.colorlist))
        if (RichTextToolbar.this.isHTMLMode().booleanValue())
          RichTextToolbar.this.changeHtmlStyle("<span style=\"color: " + RichTextToolbar.this.colorlist.getValue(RichTextToolbar.this.colorlist.getSelectedIndex()) + ";\">", "</span>");
        else
          RichTextToolbar.this.styleTextFormatter.setForeColor(RichTextToolbar.this.colorlist.getValue(RichTextToolbar.this.colorlist.getSelectedIndex()));
    }
  }
}