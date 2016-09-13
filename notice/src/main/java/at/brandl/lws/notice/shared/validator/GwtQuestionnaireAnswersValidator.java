package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

public class GwtQuestionnaireAnswersValidator {

	public static boolean valid(GwtQuestionnaireAnswers answers) {
		boolean result = true;
		result &= answers.getChildKey() != null;
		result &= answers.getQuestionnaireKey() != null;
		if(result) {
			for(GwtAnswer answer : answers.getAnswers()) {
				result &= answer.getQuestionKey() != null;
				result &= answer.getDate() != null;
				if(!result) {
					break;
				}
			}
		}
		return result;
	}
}
