package at.brandl.lws.notice.server.service;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.server.dao.ds.SectionDsDao;
import at.brandl.lws.notice.shared.service.SectionService;

public class SectionServiceImpl extends RemoteServiceServlet implements SectionService {

	private static final long serialVersionUID = -4141659112765911287L;
	private final SectionDsDao sectionDao;
	private AuthorizationServiceImpl authorizationService;

	public SectionServiceImpl() {
		sectionDao = DaoRegistry.get(SectionDsDao.class);
		authorizationService = new AuthorizationServiceImpl();

	}

	@Override
	public List<GwtSection> querySections() {

		return sectionDao.getAllSections();
	}

	@Override
	public void storeSection(List<GwtSection> sections) throws IllegalArgumentException {
		authorizationService.assertCurrentUserIsSectionAdmin();
		for (GwtSection section : sections) {
			sectionDao.storeSection(section);
		}
	}

	@Override
	public void deleteSection(List<GwtSection> sections) throws IllegalStateException {
		authorizationService.assertCurrentUserIsSectionAdmin();
		for (GwtSection section : sections) {
			final Collection<String> allSectionKeysToDelete = sectionDao.getAllChildKeys(section.getKey());
			allSectionKeysToDelete.add(section.getKey());
			sectionDao.deleteSections(allSectionKeysToDelete);
		}
	}
}
