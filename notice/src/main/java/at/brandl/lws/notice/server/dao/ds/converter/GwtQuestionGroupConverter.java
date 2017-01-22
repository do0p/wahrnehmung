package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.dao.DsUtil.toKey;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.shared.util.Constants.Question;
import at.brandl.lws.notice.shared.util.Constants.QuestionGroup;

public class GwtQuestionGroupConverter {

	public static Entity toEntity(GwtQuestionGroup gwtGroup, Key parent, int order) {
	
		Entity group;
		if (gwtGroup.getKey() != null) {
			group = new Entity(toKey(gwtGroup.getKey()));
		} else {
			group = new Entity(QuestionGroup.KIND, parent);
		}
		group.setProperty(QuestionGroup.TITLE, gwtGroup.getTitle());
		group.setProperty(QuestionGroup.ORDER, order);
		group.setProperty(QuestionGroup.ARCHIVE_DATE, gwtGroup.getArchiveDate());
		return group;
	}

	public static GwtQuestionGroup toGwtQuestionGroup(Entity entity) {
	
		GwtQuestionGroup group = new GwtQuestionGroup();
		group.setTitle((String) entity.getProperty(QuestionGroup.TITLE));
		group.setKey(DsUtil.toString(entity.getKey()));
		group.setArchiveDate((Date) entity.getProperty(Question.ARCHIVE_DATE));
		return group;
	}

}
