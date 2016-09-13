package at.brandl.lws.notice.client.utils;


abstract class DragablePanelFactory<T extends Dragable<T>> {

	static Dragable<?> create(String type, Data data, DragContainer parent) {
		switch(type){
		case DragableQuestion.QUESTION_LABEL:
			return DragableQuestion.valueOf(data, parent);
		case DragableQuestionGroup.QUESTION_GROUP_LABEL:
			return DragableQuestionGroup.valueOf(data, parent);
			
		}
		throw new RuntimeException("unkonwn type " + type);
	}
}