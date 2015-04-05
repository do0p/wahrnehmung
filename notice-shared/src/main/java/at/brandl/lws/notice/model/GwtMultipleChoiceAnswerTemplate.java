package at.brandl.lws.notice.model;

import java.util.ArrayList;
import java.util.List;

public class GwtMultipleChoiceAnswerTemplate implements GwtAnswerTemplate{

	private List<GwtMultipleChoiceOption> options = new ArrayList<GwtMultipleChoiceOption>();

	public List<GwtMultipleChoiceOption> getOptions() {
		return options;
	}

	public void setOptions(List<GwtMultipleChoiceOption> options) {
		this.options = options;
	}
	
	public void addOption(GwtMultipleChoiceOption option) {
		options.add(option);
	}
}
