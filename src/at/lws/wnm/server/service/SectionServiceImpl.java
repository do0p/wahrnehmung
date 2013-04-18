package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.service.SectionService;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.server.dao.SectionDao;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SectionServiceImpl extends RemoteServiceServlet implements
		SectionService {

	private final SectionDao sectionDao;
	private AuthorizationServiceImpl authorizationService;


	public SectionServiceImpl() {
		sectionDao = DaoRegistry.get(SectionDao.class);
		authorizationService = new AuthorizationServiceImpl();
	
	}

	@Override
	public List<GwtSection> querySections() {

		return sectionDao.getAllSections();

	}

	@Override
	public void storeSection(GwtSection section)
			throws IllegalArgumentException {
		authorizationService.assertCurrentUserIsSectionAdmin();
		sectionDao.storeSection(section);
	}

	@Override
	public void deleteSection(GwtSection section)
			throws IllegalStateException {
		authorizationService.assertCurrentUserIsSectionAdmin();
		final List<Long> allSectionKeysToDelete = sectionDao
				.getAllChildKeys(section.getKey());
		allSectionKeysToDelete.add(section.getKey());
		sectionDao.deleteSections(allSectionKeysToDelete);
	}

}
