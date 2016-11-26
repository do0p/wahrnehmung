package at.brandl.lws.notice.server.service;

import java.util.Collection;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.server.dao.ds.QuestionnaireDsDao;
import at.brandl.lws.notice.shared.service.QuestionnaireService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class QuestionnaireServiceImpl extends RemoteServiceServlet implements
		QuestionnaireService {


	private static final long serialVersionUID = -9078251135422786453L;
	private final QuestionnaireDsDao questionnaireDao;
	private UserService userService;

	public QuestionnaireServiceImpl() {

		userService = UserServiceFactory.getUserService();
		questionnaireDao = DaoRegistry.get(QuestionnaireDsDao.class);
	}

	
	@Override
	public Collection<GwtQuestionnaireAnswers> getQuestionnaireAnswers(String childKey) {

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);
		return allAnswers;
	}

	@Override
	public GwtQuestionnaireAnswers storeQuestionnaireAnswers(GwtQuestionnaireAnswers answers) {

		User user = userService.getCurrentUser();
		return questionnaireDao.storeAnswers(answers, user);
	}

}
