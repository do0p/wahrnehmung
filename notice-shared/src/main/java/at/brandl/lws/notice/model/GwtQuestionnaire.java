package at.brandl.lws.notice.model;

import java.util.ArrayList;
import java.util.List;

public class GwtQuestionnaire {

	private String title;
	private GwtSection section;
	private List<GwtQuestionGroup> groups = new ArrayList<GwtQuestionGroup>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public GwtSection getSection() {
		return section;
	}

	public void setSection(GwtSection section) {
		this.section = section;
	}

	public List<GwtQuestionGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<GwtQuestionGroup> groups) {
		this.groups = groups;
	}

	public void addQuestionGroup(GwtQuestionGroup group) {
		groups.add(group);

	}

}
