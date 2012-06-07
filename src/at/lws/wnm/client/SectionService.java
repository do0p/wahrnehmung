package at.lws.wnm.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import at.lws.wnm.shared.model.Section;

@RemoteServiceRelativePath("section")
public interface SectionService extends RemoteService {

	List<Section> querySections();

	void storeSection(Section section);

}
