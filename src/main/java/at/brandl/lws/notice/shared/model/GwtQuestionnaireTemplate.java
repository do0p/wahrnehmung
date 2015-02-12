package at.brandl.lws.notice.shared.model;

import java.util.List;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.validator.GwtQuestionnaireTemplateValidator;

public class GwtQuestionnaireTemplate extends GwtModel {

	private String key;
	private Questionnaire questionnaire;
	private List<QuestionGroupTemplate> questionGroups;

	@Override
	public boolean isNew() {
		return Utils.isEmpty(key);
	}

	@Override
	public boolean isValid() {
		return GwtQuestionnaireTemplateValidator.validate(this);
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return questionnaire.getTitle();
	}

}
