package at.brandl.lws.notice.client.service;

import java.util.Date;

import at.brandl.lws.notice.shared.model.GwtChild;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChildServiceAsync extends ModelServiceAsync<GwtChild>{


	void addDevelopementDialogueDate(String key, Date date,
			AsyncCallback<Void> callback);

	void deleteDevelopementDialogueDate(String key,
			Date date, AsyncCallback<Void> callback);

}
