package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.dao.DsUtil.toKey;
import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.shared.util.Constants;
import at.brandl.lws.notice.shared.util.Constants.AnswerTemplate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GwtAnswerTemplateConverter {

	public static Entity toEntity(GwtAnswerTemplate gwtTemplate, Key parent) {
		String type;
		if (gwtTemplate instanceof GwtMultipleChoiceAnswerTemplate) {
			type = Constants.MULTIPLE_CHOICE;
		} else {
			throw new RuntimeException("no mapping for templateclass "
					+ gwtTemplate.getClass());
		}
	
		Entity template;
		if (gwtTemplate.getKey() != null) {
			template = new Entity(toKey(gwtTemplate.getKey()));
		} else {
			template = new Entity(AnswerTemplate.KIND, parent);
		}
		template.setProperty(AnswerTemplate.Type, type);
		return template;
	}

	public static GwtAnswerTemplate toGwtAnswerTemplate(Entity entity) {
	
		String type = (String) entity.getProperty(AnswerTemplate.Type);
		if (Constants.MULTIPLE_CHOICE.equals(type)) {
	
			GwtMultipleChoiceAnswerTemplate template = new GwtMultipleChoiceAnswerTemplate();
			template.setKey(DsUtil.toString(entity.getKey()));
			return template;
		}
		throw new IllegalStateException("unknown type of answer template "
				+ type);
	}

}
