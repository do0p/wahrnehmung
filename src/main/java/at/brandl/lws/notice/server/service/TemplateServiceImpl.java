package at.brandl.lws.notice.server.service;

import java.util.List;

import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.TemplateDsDao;
import at.brandl.lws.notice.shared.model.GwtTemplate;
import at.brandl.lws.notice.shared.service.TemplateService;
import at.brandl.lws.notice.shared.validator.GwtTemplateValidator;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TemplateServiceImpl extends RemoteServiceServlet implements
		TemplateService {

	private static final long serialVersionUID = -957423477681211702L;
	private final AuthorizationServiceImpl authorizationService;

	private final TemplateDsDao templateDao;

	public TemplateServiceImpl() {
		templateDao = DaoRegistry.get(TemplateDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
	}

	@Override
	public List<GwtTemplate> getAll() {
		return templateDao.getTemplates();
	}

	@Override
	public GwtTemplate get(String id) {
		return templateDao.getTemplate(id);
	}

	@Override
	public void delete(GwtTemplate template) {
		authorizationService.assertCurrentUserIsAdmin();
		templateDao.deleteTemplate(template);
	}

	@Override
	public void store(GwtTemplate template) {
		if (!GwtTemplateValidator.validate(template)) {
			throw new IllegalArgumentException("template is not complete");
		}
		authorizationService.assertCurrentUserIsAdmin();
		templateDao.storeTemplate(template);
	}

}
