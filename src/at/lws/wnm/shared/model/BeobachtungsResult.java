package at.lws.wnm.shared.model;

import java.io.Serializable;
import java.util.List;

public class BeobachtungsResult implements Serializable {
	private static final long serialVersionUID = 4974219726001956959L;
	private int rowCount;
	private List<GwtBeobachtung> beobachtungen;

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public List<GwtBeobachtung> getBeobachtungen() {
		return beobachtungen;
	}

	public void setBeobachtungen(List<GwtBeobachtung> beobachtungen) {
		this.beobachtungen = beobachtungen;
	}
}
