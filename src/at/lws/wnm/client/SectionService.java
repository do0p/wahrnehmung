package at.lws.wnm.client;

import java.util.List;

import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("section")
public interface SectionService extends RemoteService {

	List<GwtSection> querySections();

	String storeSection(GwtSection section) throws IllegalArgumentException;

	String deleteSection(GwtSection section);
}
