package at.lws.wnm.server.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BeobachtungGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;

	private Long masterBeobachtungsKey;

	private Long beobachtungsKey;

	public BeobachtungGroup() {

	}

	public BeobachtungGroup(Long masterBeobachtungsKey, Long beobachtungsKey) {
		this.masterBeobachtungsKey = masterBeobachtungsKey;
		this.beobachtungsKey = beobachtungsKey;
	}



}
