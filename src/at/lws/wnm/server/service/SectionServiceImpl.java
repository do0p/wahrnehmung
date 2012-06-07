package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.SectionService;
import at.lws.wnm.server.dao.SectionDao;
import at.lws.wnm.shared.model.Section;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SectionServiceImpl extends RemoteServiceServlet implements SectionService {

	private final SectionDao sectionDao;
	
	public SectionServiceImpl()
	{
		sectionDao = new SectionDao();
	}
	
	@Override
	public List<Section> querySections() {
		
		return sectionDao.getAllSections();
		
	}

	@Override
	public void storeSection(Section section) {
		sectionDao.storeSection(section);
		
	}

}
