package at.brandl.lws.notice.server.dao.ds.converter;

import java.util.Date;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.server.dao.ds.DsUtil;
import at.brandl.lws.notice.shared.util.Constants;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswer;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

public class GwtAnswerConverter {

	public static Entity toEntity(GwtAnswer answer, Key parent, User user) {
	
		String key = answer.getKey();
		Entity entity;
		if (key == null) {
			entity = new Entity(QuestionnaireAnswer.KIND, parent);
			entity.setProperty(QuestionnaireAnswer.CREATE_DATE, new Date());
		} else {
			entity = new Entity(DsUtil.toKey(key));
		}
		entity.setProperty(QuestionnaireAnswer.DATE, answer.getDate());
		entity.setProperty(QuestionnaireAnswer.QUESTION_KEY,
				DsUtil.toKey(answer.getQuestionKey()));
		entity.setProperty(QuestionnaireAnswer.VALUE, answer.getValue());
		entity.setProperty(QuestionnaireAnswer.USER, user);
		entity.setProperty(QuestionnaireAnswer.TYPE, Constants.MULTIPLE_CHOICE);
	
		return entity;
	}

	public static GwtAnswer toGwtAnswer(Entity entity) {
	
		String type = (String) entity.getProperty(QuestionnaireAnswer.TYPE);
		if (Constants.MULTIPLE_CHOICE.equals(type)) {
			GwtMultipleChoiceAnswer answer = new GwtMultipleChoiceAnswer();
			answer.setKey(DsUtil.toString(entity.getKey()));
			answer.setDate((Date) entity.getProperty(QuestionnaireAnswer.DATE));
			answer.setCreateDate((Date) entity.getProperty(QuestionnaireAnswer.CREATE_DATE));
			answer.setQuestionKey(DsUtil.toString((Key) entity
					.getProperty(QuestionnaireAnswer.QUESTION_KEY)));
			answer.setValue(entity.getProperty(QuestionnaireAnswer.VALUE));
			return answer;
		}
		throw new IllegalStateException("unknown type of answer: " + type);
	
	}

}
