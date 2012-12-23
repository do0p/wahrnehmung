package at.lws.wnm.client.admin;

import at.lws.wnm.shared.model.Authorization;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminContent extends TabPanel
{
  public AdminContent(Authorization authorization, String width)
  {
    setSize("100%", width);
    if (authorization.isAdmin()) {
      add(new ChildAdmin(), "Kinder / Jugendliche");
    }
    if ((authorization.isAdmin()) || (authorization.isEditSections())) {
      add(new SectionAdmin(), "Bereiche");
    }
    if (authorization.isAdmin()) {
      add(new AuthorizationAdmin(), "Benutzer");
    }
    if ((authorization.isAdmin()) || (authorization.isEditSections()))
      selectTab(0);
  }
}