package at.brandl.lws.notice.server.service;

import java.util.List;

import at.brandl.lws.notice.client.service.TemplateService;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.TemplateDsDao;
import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TemplateServiceImpl extends RemoteServiceServlet implements
		TemplateService {

	private static final long serialVersionUID = -957423477681211702L;

	private final TemplateDsDao templateDao;

	public TemplateServiceImpl() {
		templateDao = DaoRegistry.get(TemplateDsDao.class);
	}

	@Override
	public List<GwtTemplate> getTemplates() {
		return templateDao.getTemplates();
	}

	@Override
	public GwtTemplate getTemplate(String id) {
		return templateDao.getTemplate(id);
	}

}
