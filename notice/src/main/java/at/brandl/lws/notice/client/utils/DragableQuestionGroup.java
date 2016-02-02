package at.brandl.lws.notice.client.utils;


public class DragableQuestionGroup extends Dragable<DragableQuestionGroup> {

	public static final String QUESTION_GROUP_LABEL = "question group";
	private static final String SEPARATOR = "°°";

	private ChangeableLabel title;
	private DragContainer vPanel;

	private DragableQuestionGroup(Data data, DragContainer parent) {
		super(data.getKey(), parent);
		vPanel = new DragContainer(parent);
		vPanel.setWidth("100%");

		String[] dataArray = data.getValue().split(SEPARATOR);
		title = new ChangeableLabel(dataArray[0]);
		vPanel.add(title);

		for (int i = 1; i < dataArray.length; i++) {

			DragableQuestion question = DragableQuestion.valueOf(
					Data.valueOf(dataArray[i]), vPanel);
			vPanel.add(question);
		}

		vPanel.add(DragTargetLabel.valueOf("end", vPanel));
		add(vPanel);
	}

	@Override
	Data getData() {

		StringBuilder result = new StringBuilder();
		result.append(title.getText());
		int count = vPanel.getWidgetCount();
		for (int i = 1; i < count - 1; i++) {
			result.append(SEPARATOR);
			result.append(((DragableQuestion) vPanel.getWidget(i)).getData()
					.toString());
		}
		return new Data(getKey(), result.toString());
	}

	@Override
	String getType() {
		return QUESTION_GROUP_LABEL;
	}

	public static DragableQuestionGroup valueOf(Data data, DragContainer parent) {

		return new DragableQuestionGroup(data, parent);
	}

	public void addQuestion(Data data) {

		int widgetCount = vPanel.getWidgetCount();
		vPanel.insert(DragableQuestion.valueOf(data, vPanel), widgetCount - 1);
	}

	@Override
	public String toString() {
		return "DragableQuestionGroup: " + title.getText();
	}
}
