package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.server.dao.ds.DsUtil.toKey;

import java.util.Date;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.dao.ds.DsUtil;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GwtQuestionnaireConverter {

	public static Entity toEntity(GwtQuestionnaire gwtForm) {
	
		Entity form;
		if (gwtForm.getKey() != null) {
			form = new Entity(toKey(gwtForm.getKey()));
			form.setProperty(Questionnaire.UPDATE_DATE, new Date());
		} else {
			form = new Entity(Questionnaire.KIND);
			form.setProperty(Questionnaire.CREATE_DATE, new Date());
		}
		form.setProperty(Questionnaire.TITLE, gwtForm.getTitle());
		form.setProperty(Questionnaire.SECTION, toKey(gwtForm.getSection()));
		return form;
	}

	public static GwtQuestionnaire toGwtQuestionnaire(Entity entity) {
	
		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle((String) entity.getProperty(Questionnaire.TITLE));
		form.setSection(DsUtil.toString((Key) entity
				.getProperty(Questionnaire.SECTION)));
		form.setKey(DsUtil.toString(entity.getKey()));
		return form;
	}

}
