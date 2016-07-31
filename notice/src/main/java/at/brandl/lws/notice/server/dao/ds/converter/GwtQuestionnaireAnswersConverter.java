package at.brandl.lws.notice.server.dao.ds.converter;

import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.server.dao.ds.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswers;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GwtQuestionnaireAnswersConverter {

	public static Entity toEntity(GwtQuestionnaireAnswers answers) {
		Entity entity = new Entity(QuestionnaireAnswers.KIND,
				DsUtil.toKey(answers.getChildKey()));
		entity.setProperty(QuestionnaireAnswers.QUESTIONNAIRE_KEY,
				DsUtil.toKey(answers.getQuestionnaireKey()));
		return entity;
	}

	public static GwtQuestionnaireAnswers toGwtQuestionnaireAnswers(Entity entity) {
	
		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setKey(DsUtil.toString(entity.getKey()));
		answers.setChildKey(DsUtil.toString(entity.getParent()));
		answers.setQuestionnaireKey(DsUtil.toString((Key) entity
				.getProperty(QuestionnaireAnswers.QUESTIONNAIRE_KEY)));
		return answers;
	}

}
