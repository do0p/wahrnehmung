package at.lws.wnm.shared.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Section {

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

}
