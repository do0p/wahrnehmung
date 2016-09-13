package at.brandl.lws.notice.client.utils;

public interface QuestionnaireAnswerListener {

	void notifyAnswer(String questionKey, Object optionValue);

}
