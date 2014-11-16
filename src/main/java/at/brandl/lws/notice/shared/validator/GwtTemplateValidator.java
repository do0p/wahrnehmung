package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.model.GwtTemplate;

public class GwtTemplateValidator {
	public static boolean validate(GwtTemplate template) {
		return Utils.isNotEmpty(template.getName())
				&& Utils.isNotEmpty(template.getTemplate());
	}
}
