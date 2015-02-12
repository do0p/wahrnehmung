package at.brandl.lws.notice.server.service;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.shared.model.GwtQuestionnaireTemplate;
import at.brandl.lws.notice.shared.service.QuestionnaireTemplateService;

public class QuestionnaireTemplateServiceImpl extends RemoteServiceServlet implements
		QuestionnaireTemplateService {

	private static final long serialVersionUID = -9210059245583387395L;

	@Override
	public List<GwtQuestionnaireTemplate> getAll() {
		return new ArrayList<GwtQuestionnaireTemplate>();
	}

	@Override
	public GwtQuestionnaireTemplate get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void store(GwtQuestionnaireTemplate model) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(GwtQuestionnaireTemplate model) {
		// TODO Auto-generated method stub

	}

}
