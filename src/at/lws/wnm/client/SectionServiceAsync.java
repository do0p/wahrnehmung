package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SectionServiceAsync {

	void querySections(AsyncCallback<List<GwtSection>> callback);

	void storeSection(GwtSection section, AsyncCallback<Void> callback);

	void deleteSection(GwtSection section, AsyncCallback<Void> callback);


}
