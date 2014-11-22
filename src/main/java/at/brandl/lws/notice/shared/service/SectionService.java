package at.brandl.lws.notice.shared.service;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtSection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("section")
public interface SectionService extends RemoteService {

	List<GwtSection> querySections();

	void storeSection(GwtSection section) throws IllegalArgumentException;

	void deleteSection(GwtSection section) throws IllegalStateException;
}
