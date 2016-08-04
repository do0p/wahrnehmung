package at.brandl.lws.notice.client.utils;


public class DragableQuestion extends Dragable<DragableQuestion> {

	static final String QUESTION_LABEL = "question";

	private ChangeableLabel label;

	private DragableQuestion(Data data, DragContainer parent) {
		super(data.getKey(), parent);
		label = new ChangeableLabel(data.getValue());
		add(label);
	}

	@Override
	Data getData() {
		return new Data(getKey(), label.getText());
	}

	@Override
	String getType() {
		return QUESTION_LABEL;
	}

	public String getLabel() {
		return label.getText();
	}
	
	public static DragableQuestion valueOf(Data data, DragContainer parent) {

		return new DragableQuestion(data, parent);
	}

	@Override
	public String toString() {
		return "DragableQuestion: " + label.getText();
	}
}
