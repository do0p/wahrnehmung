package at.brandl.lws.notice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GwtQuestion implements Serializable{

	private static final long serialVersionUID = 4489992873551905987L;
	private String label;
	private GwtAnswerTemplate template;
	private String key;
	private List<String> archived = new ArrayList<>();
	private Date archiveDate;
	
	public static GwtQuestion valueOf(GwtQuestion question) {
		GwtQuestion gwtQuestion = new GwtQuestion();
		gwtQuestion.setLabel(question.getLabel());
		gwtQuestion.setTemplate(question.getAnswerTemplate());
		gwtQuestion.setArchived(question.getArchived());
		return gwtQuestion;
	}
	
	public Date getArchiveDate() {
		return archiveDate;
	}

	public void setArchiveDate(Date archived) {
		this.archiveDate = archived;
	}

	public List<String> getArchived() {
		return archived;
	}

	public void addArchived(String archived) {
		this.archived.add(archived);
	}
	
	public void setArchived(List<String> archived) {
		if(archived != null) {
			this.archived = archived;
		}
	}

	public GwtAnswerTemplate getAnswerTemplate() {
		return template;
	}

	public void setTemplate(GwtAnswerTemplate template) {
		this.template = template;
		
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public boolean isArchived() {
		return archiveDate != null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GwtQuestion)) {
			return false;
		}
		GwtQuestion other = (GwtQuestion) obj;
		boolean result = ObjectUtils.equals(label, other.label);
		result &= ObjectUtils.equals(template, other.template);
		return result;
	}
	
	@Override
	public int hashCode() {
		int result = 37;
		result = result * 17 + ObjectUtils.hashCode(label);
		result = result * 17 + ObjectUtils.hashCode(template);
		return result;
	}



}
