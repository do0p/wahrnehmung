package at.brandl.lws.notice.shared.service;

import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.model.GwtChild;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChildServiceAsync {

	void queryChildren(AsyncCallback<List<GwtChild>> callback);

	void storeChild(GwtChild child, AsyncCallback<Void> callback);

	void deleteChild(String key, AsyncCallback<Void> callback);

	void getChild(String key, AsyncCallback<GwtChild> callback);

	void addDevelopementDialogueDate(String key, Date date,
			AsyncCallback<Void> callback);

	void deleteDevelopementDialogueDate(String key,
			Date date, AsyncCallback<Void> callback);

}
