package at.lws.wnm.server.service;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import at.lws.wnm.client.service.SectionService;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.server.dao.ds.SectionDsDao;
import at.lws.wnm.shared.model.GwtSection;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SectionServiceImpl extends RemoteServiceServlet implements
		SectionService {

	private static final long serialVersionUID = -4141659112765911287L;
	private final SectionDsDao sectionDao;
	private AuthorizationServiceImpl authorizationService;


	public SectionServiceImpl() {
		sectionDao = DaoRegistry.get(SectionDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	
	}

	@Override
	public List<GwtSection> querySections() {

		List<GwtSection> allSections = sectionDao.getAllSections();
		sort(allSections);
		return allSections;

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
		final Collection<String> allSectionKeysToDelete = sectionDao
				.getAllChildKeys(section.getKey());
		allSectionKeysToDelete.add(section.getKey());
		sectionDao.deleteSections(allSectionKeysToDelete);
	}

	private void sort(List<GwtSection> allSections) {
		Collections.sort(allSections, new Comparator<GwtSection>() {
			
			@Override
			public int compare(GwtSection o1, GwtSection o2) {
				
				return Collator.getInstance(Locale.GERMAN).compare(o1.getSectionName(),o2.getSectionName());
			}
		});
	}
}
