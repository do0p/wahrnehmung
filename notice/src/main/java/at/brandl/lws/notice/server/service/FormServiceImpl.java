package at.brandl.lws.notice.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.FormDsDao;
import at.brandl.lws.notice.shared.service.FormService;

public class FormServiceImpl extends RemoteServiceServlet implements FormService {


	private static final long serialVersionUID = -1963083487479617418L;
	private final FormParser formParser;
	private final FormDsDao formDao;
	
	public  FormServiceImpl() {
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

}
