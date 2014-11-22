package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtSection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SectionServiceAsync {

	void querySections(AsyncCallback<List<GwtSection>> callback);

	void storeSection(GwtSection section, AsyncCallback<Void> callback);

	void deleteSection(GwtSection section, AsyncCallback<Void> callback);


}
