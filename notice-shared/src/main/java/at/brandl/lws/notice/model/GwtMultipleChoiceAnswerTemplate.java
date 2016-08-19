package at.brandl.lws.notice.model;

import java.util.ArrayList;
import java.util.List;

public class GwtMultipleChoiceAnswerTemplate implements GwtAnswerTemplate{

	private static final long serialVersionUID = 3663485519202332470L;
	private List<GwtMultipleChoiceOption> options = new ArrayList<GwtMultipleChoiceOption>();
	private String key;


	public static GwtMultipleChoiceAnswerTemplate valueOf(GwtMultipleChoiceAnswerTemplate template) {
		GwtMultipleChoiceAnswerTemplate gwtTemplate = new GwtMultipleChoiceAnswerTemplate();
		gwtTemplate.setOptions(template.getOptions());
		return gwtTemplate;
	}
	
	public List<GwtMultipleChoiceOption> getOptions() {
		return options;
	}

	public void setOptions(List<GwtMultipleChoiceOption> options) {
		this.options = options;
	}
	
	public void addOption(GwtMultipleChoiceOption option) {
		options.add(option);
	}
	

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GwtMultipleChoiceAnswerTemplate)) {
			return false;
		}
		GwtMultipleChoiceAnswerTemplate other = (GwtMultipleChoiceAnswerTemplate) obj;
		return ObjectUtils.equals(options, other.options);
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(options);
	}

	@Override
	public GwtAnswer createAnswer() {
		
		return new GwtMultipleChoiceAnswer();
	}

	@Override
	public GwtMultipleChoiceAnswerTemplate clone() {
		try {
			GwtMultipleChoiceAnswerTemplate clone = (GwtMultipleChoiceAnswerTemplate) super.clone();
			List<GwtMultipleChoiceOption> clonedOptions = new ArrayList<>(clone.options.size());
			for(GwtMultipleChoiceOption option : clone.options) {
				clonedOptions.add(option.clone());
			}
			clone.options = clonedOptions;
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("clone is supported", e);
		}
	}
	
}
