package at.brandl.lws.notice.server.service;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormService;

public class FormServiceImpl extends RemoteServiceServlet implements FormService {


	private static final long serialVersionUID = -1963083487479617418L;
	private FormParser formParser;
	
	public  FormServiceImpl() {
		formParser = new FormParser();
	}
	
	@Override
	public void storeFormAsString(String formText) {
		
		GwtQuestionnaire questionnaire = formParser.parse(formText);
		System.out.println(formParser.toString(questionnaire));
	}

	@Override
	public String getFormAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
