package at.lws.wnm.client;

import at.lws.wnm.client.utils.Navigation;
import at.lws.wnm.shared.model.Authorization;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Wahrnehmung extends SecuredContent
  implements ValueChangeHandler<String>
{
  private Navigation navigation;

  protected void onLogin(Authorization authorization)
  {
    this.navigation = new Navigation(authorization);
    RootPanel.get("navigation").add(this.navigation);
    History.addValueChangeHandler(this);
    changePage(History.getToken());
    RootPanel.get("logout").add(
      new Anchor("logout", authorization.getLogoutUrl()));
    RootPanel.get("title").add(new HTML("Wahrnehmung"));
  }

  protected void onLogOut(Authorization authorization)
  {
    RootPanel rootPanel = RootPanel.get("content");
    rootPanel.clear();
    rootPanel.add(new LoginFailedContent(authorization.getLoginUrl()));
  }

  public void onValueChange(ValueChangeEvent<String> event)
  {
    changePage(History.getToken());
  }

  private void changePage(String token) {
    Widget content = this.navigation.getContent(token);
    if (content != null) {
      RootPanel rootPanel = RootPanel.get("content");
      rootPanel.clear();
      rootPanel.add(content);
    }
  }
}