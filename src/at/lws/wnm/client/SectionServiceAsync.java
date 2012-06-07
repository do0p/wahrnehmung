package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.Section;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SectionServiceAsync {

	void querySections(AsyncCallback<List<Section>> callback);

	void storeSection(Section section, AsyncCallback<Void> callback);

}
