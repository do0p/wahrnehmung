package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.dao.DsUtil.toKey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.shared.util.Constants.MultipleChoiceOption;

public class GwtMultipleChoiceOptionConverter {

	public static Entity toEntity(GwtMultipleChoiceOption gwtOption, Key parent,
			int order) {
	
		Entity option;
		if (gwtOption.getKey() != null) {
			option = new Entity(toKey(gwtOption.getKey()));
		} else {
			option = new Entity(MultipleChoiceOption.KIND, parent);
		}
		option.setProperty(MultipleChoiceOption.LABEL, gwtOption.getLabel());
		option.setProperty(MultipleChoiceOption.VALUE, gwtOption.getValue());
		option.setProperty(MultipleChoiceOption.ORDER, order);
		return option;
	}

	public static GwtMultipleChoiceOption toGwtMultipleChoiceOption(Entity entity) {
	
		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setLabel((String) entity.getProperty(MultipleChoiceOption.LABEL));
		option.setValue((String) entity.getProperty(MultipleChoiceOption.VALUE));
		option.setKey(DsUtil.toString(entity.getKey()));
		return option;
	}

}
