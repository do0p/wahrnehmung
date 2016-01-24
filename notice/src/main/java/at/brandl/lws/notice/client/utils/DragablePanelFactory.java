package at.brandl.lws.notice.client.utils;

import com.google.gwt.user.client.ui.VerticalPanel;

abstract class DragablePanelFactory<T extends DragablePanel<T>> {

	static DragablePanel<?> create(String type, String data, VerticalPanel panel, boolean insideGroup) {
		switch(type){
		case DragableQuestion.QUESTION_LABEL:
			return DragableQuestion.valueOf(data, panel, insideGroup);
		case DragableQuestionGroup.QUESTION_GROUP_LABEL:
			return DragableQuestionGroup.valueOf(data, panel, insideGroup);
			
		}
		throw new RuntimeException("unkonwn type " + type);
	}
}