package at.lws.wnm.server.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import at.lws.wnm.shared.model.GwtSection;

@Entity
public class Section implements Serializable {

	private static final long serialVersionUID = 3795496391939419016L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;

	private String sectionName;

	private Long parentKey;

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public GwtSection toGwt() {
		final GwtSection section = new GwtSection();
		section.setKey(key == null ? null : key.toString());
		section.setSectionName(sectionName);
		section.setParentKey(parentKey == null ? null : parentKey.toString());
		return section;
	}

	public static Section valueOf(GwtSection gwtSection) {
		final Section section = new Section();
		final String sectionKey = gwtSection.getKey();
		if(sectionKey != null) {
			section.key = Long.valueOf(sectionKey);
		}
		section.sectionName = gwtSection.getSectionName();
		if (gwtSection.getParentKey() != null) {
			section.parentKey = Long.valueOf(gwtSection.getParentKey());
		}
		return section;

	}

	public Long getParentKey() {
		return parentKey;
	}

	public void setParentKey(Long parentKey) {
		this.parentKey = parentKey;
	}

}
