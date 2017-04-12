package at.brandl.lws.notice.shared.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import at.brandl.lws.notice.model.GwtSection;

public interface SectionServiceAsync {

	void querySections(AsyncCallback<List<GwtSection>> callback);

	void storeSection(List<GwtSection> sections, AsyncCallback<Void> callback);

	void deleteSection(List<GwtSection> sections, AsyncCallback<Void> callback);


}
