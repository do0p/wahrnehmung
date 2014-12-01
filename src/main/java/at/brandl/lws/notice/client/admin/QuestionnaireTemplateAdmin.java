package at.brandl.lws.notice.client.admin;

import at.brandl.lws.notice.shared.model.GwtQuestionnaireTemplate;
import at.brandl.lws.notice.shared.service.QuestionnaireTemplateService;
import at.brandl.lws.notice.shared.service.QuestionnaireTemplateServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class QuestionnaireTemplateAdmin extends AbstractModelAdminTab<GwtQuestionnaireTemplate> {

	QuestionnaireTemplateAdmin() {
		super(true, (QuestionnaireTemplateServiceAsync) GWT.create(QuestionnaireTemplateService.class));

	}

	@Override
	protected int getListCount() {
		return 10;
	}

	@Override
	protected String getDelWarning() {
		return labels().templateDelWarning();
	}

	@Override
	protected GwtQuestionnaireTemplate createModel() {
		return new GwtQuestionnaireTemplate();
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Widget createContentLayout() {
		// TODO Auto-generated method stub
		return new VerticalPanel();
	}

	@Override
	protected void updateFields() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getKey(GwtQuestionnaireTemplate template) {
		return template.getKey();
	}

	@Override
	protected String getDisplayName(GwtQuestionnaireTemplate template) {
		return template.getTitle();
	}



}
