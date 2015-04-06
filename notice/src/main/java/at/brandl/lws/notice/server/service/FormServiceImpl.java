package at.brandl.lws.notice.server.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.FormDsDao;
import at.brandl.lws.notice.shared.service.FormService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormServiceImpl extends RemoteServiceServlet implements
		FormService {

	private static final long serialVersionUID = -1963083487479617418L;
	private final FormParser formParser;
	private final FormDsDao formDao;

	public FormServiceImpl() {
		formParser = new FormParser();
		formDao = DaoRegistry.get(FormDsDao.class);
	}

	@Override
	public String storeFormAsString(String formText) {

		GwtQuestionnaire questionnaire = formParser.parse(formText);
		formDao.storeQuestionnaire(questionnaire);
		return formParser.toString(questionnaire);
	}

	@Override
	public String getFormAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GwtQuestionnaire> getAllForms() {
		Set<String> titles = new HashSet<String>();
		List<GwtQuestionnaire> result = new ArrayList<GwtQuestionnaire>();

		for (GwtQuestionnaire form : formDao.getAllQuestionnaires()) {
			String title = form.getTitle();
			if (titles.contains(title)) {
				continue;
			}

			titles.add(title);
			result.add(form);
		}

		return result;
	}

}
