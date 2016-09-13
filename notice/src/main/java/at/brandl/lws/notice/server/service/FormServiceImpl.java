package at.brandl.lws.notice.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.FormDsDao;
import at.brandl.lws.notice.server.dao.ds.QuestionnaireDsDao;
import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.service.FormService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormServiceImpl extends RemoteServiceServlet implements
		FormService {

	private static final long serialVersionUID = -1963083487479617418L;
	private final FormDsDao formDao;
	private final QuestionnaireDsDao questionnaireDao;

	public FormServiceImpl() {
		formDao = DaoRegistry.get(FormDsDao.class);
		questionnaireDao = DaoRegistry.get(QuestionnaireDsDao.class);
	}

	@Override
	public GwtQuestionnaire storeForm(GwtQuestionnaire form) {

		return formDao.storeQuestionnaire(form);
	}

	@Override
	public List<GwtQuestionnaire> getAllForms(String childKey) {

		Map<String, GwtQuestionnaire> titles = new HashMap<String, GwtQuestionnaire>();

		for (GwtQuestionnaire form : formDao.getAllQuestionnaires()) {
			titles.put(form.getTitle(), form);
		}

		if (Utils.isNotEmpty(childKey)) {
			Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao
					.getAllAnswers(childKey);
			for (GwtQuestionnaireAnswers answers : allAnswers) {
				GwtQuestionnaire form = formDao.getQuestionnaire(answers);
				titles.put(form.getTitle(), form);
			}
		}

		return new ArrayList<GwtQuestionnaire>(titles.values());
	}
}
