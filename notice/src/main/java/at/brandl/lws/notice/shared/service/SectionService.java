package at.brandl.lws.notice.shared.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.brandl.lws.notice.model.GwtSection;

@RemoteServiceRelativePath("section")
public interface SectionService extends RemoteService {

	List<GwtSection> querySections();

	void storeSection(List<GwtSection> sections) throws IllegalArgumentException;

	void deleteSection(GwtSection section) throws IllegalStateException;
}
