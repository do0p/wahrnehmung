package at.brandl.lws.notice.shared.validator;

import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.Utils;

public class GwtQuestionnaireValidator {

	public static boolean valid(GwtQuestionnaire questionnaire) {
		boolean result = true;
		result &= Utils.isNotEmpty(questionnaire.getTitle());
		result &= Utils.isNotEmpty(questionnaire.getSection());

		if (result) {
			for (GwtQuestionGroup group : questionnaire.getGroups()) {
				for (GwtQuestion question : group.getQuestions()) {
					result &= Utils.isNotEmpty(question.getLabel());
					result &= question.getAnswerTemplate() != null;
					if (!result) {
						break;
					}
				}
				if (!result) {
					break;
				}
			}
		}
		return result;
	}
}
