package at.lws.wnm.client.utils;

import at.lws.wnm.client.EditContent;
import at.lws.wnm.client.Search;
import at.lws.wnm.client.admin.AdminContent;
import at.lws.wnm.shared.model.Authorization;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

public class Navigation extends HorizontalPanel
{
  private static final String ADMIN = "admin";
  static final String NEW_ENTRY = "new";
  private static final String LIST_ENTRY = "list";
  private final Authorization authorization;

  public Navigation(Authorization authorization)
  {
    this.authorization = authorization;
    setSpacing(10);
    add(new Hyperlink("erfassen", NEW_ENTRY));
    add(new Hyperlink("anzeigen", LIST_ENTRY));
    if ((authorization.isAdmin()) || (authorization.isEditSections()))
      add(new Hyperlink("administrieren", ADMIN));
  }

  public Widget getContent(String token)
  {
    if (token.isEmpty()) {
      token = NEW_ENTRY;
    }
    if (token.equals(NEW_ENTRY)) {
      return new EditContent(this.authorization, 850, null);
    }
    if (token.equals(LIST_ENTRY)) {
      return new Search(this.authorization, 850);
    }
    if (token.equals(ADMIN)) {
      return new AdminContent(this.authorization, "550px");
    }
    return null;
  }
}