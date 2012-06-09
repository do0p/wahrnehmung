package at.lws.wnm.server.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import at.lws.wnm.shared.model.GwtSection;


@Entity
public class Section implements Serializable{

	private static final long serialVersionUID = 3795496391939419016L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;

	private String sectionName;

	public Long getKey() {
		return key;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public GwtSection toGwt()
	{
		final GwtSection section = new GwtSection();
		section.setKey(key);
		section.setSectionName(sectionName);
		return section;
	}
	
	public static Section valueOf(GwtSection gwtSection) {
		final Section section = new Section();
		section.key = gwtSection.getKey();
		section.sectionName = gwtSection.getSectionName();
		return section;
		
	}

}
