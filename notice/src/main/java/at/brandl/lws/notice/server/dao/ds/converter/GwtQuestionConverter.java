package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.server.dao.ds.DsUtil.toKey;

import java.util.Date;
import java.util.List;

import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.server.dao.ds.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Question;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GwtQuestionConverter {

	public static Entity toEntity(GwtQuestion gwtQuestion, Key parent, int order) {
	
		Entity question;
		if (gwtQuestion.getKey() != null) {
			question = new Entity(toKey(gwtQuestion.getKey()));
		} else {
			question = new Entity(Question.KIND, parent);
		}
		question.setProperty(Question.LABEL, gwtQuestion.getLabel());
		question.setProperty(Question.ORDER, order);
		question.setProperty(Question.REPLACED, gwtQuestion.getArchived());
		question.setProperty(Question.ARCHIVE_DATE, gwtQuestion.getArchiveDate());
		return question;
	}

	@SuppressWarnings("unchecked")
	public static GwtQuestion toGwtQuestion(Entity entity) {
	
		GwtQuestion question = new GwtQuestion();
		question.setLabel((String) entity.getProperty(Question.LABEL));
		question.setKey(DsUtil.toString(entity.getKey()));
		question.setArchived((List<String>) entity.getProperty(Question.REPLACED));
		question.setArchiveDate((Date) entity.getProperty(Question.ARCHIVE_DATE));
		return question;
	}

}
