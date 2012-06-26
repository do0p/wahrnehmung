package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.SectionService;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.server.dao.SectionDao;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SectionServiceImpl extends RemoteServiceServlet implements SectionService {

	private final SectionDao sectionDao;
	private final BeobachtungDao beobachtungDao;
	
	public SectionServiceImpl()
	{
		sectionDao = new SectionDao();
		beobachtungDao = new BeobachtungDao();
	}
	
	@Override
	public List<GwtSection> querySections() {
		
		return sectionDao.getAllSections();
		
	}

	@Override
	public void storeSection(GwtSection section) {
		sectionDao.storeSection(section);
		
	}

	@Override
	public void deleteSection(GwtSection section) {

		final List<Long> allSectionKeysToDelete = sectionDao.getAllChildKeys(section.getKey());
		allSectionKeysToDelete.add(section.getKey());
		if(beobachtungDao.getBeobachtungen(allSectionKeysToDelete).isEmpty())
		{
			sectionDao.deleteSections(allSectionKeysToDelete);
		}
		else
		{
			throw new IllegalStateException("Kann den Bereich " + section.getSectionName() + " nicht löschen, da es noch Wahrnehmungen in dem Bereich (oder Subbereichen) gibt.");
		}
		
	}

}
