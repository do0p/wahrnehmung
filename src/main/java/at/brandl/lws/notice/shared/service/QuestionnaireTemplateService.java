package at.brandl.lws.notice.shared.service;

import at.brandl.lws.notice.shared.model.GwtQuestionnaireTemplate;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("questionnaireTemplate")
public interface QuestionnaireTemplateService extends ModelService<GwtQuestionnaireTemplate>, RemoteService {

}
