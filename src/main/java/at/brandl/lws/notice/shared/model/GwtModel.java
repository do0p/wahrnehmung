package at.brandl.lws.notice.shared.model;

import java.io.Serializable;

public abstract class GwtModel implements Serializable {

	private static final long serialVersionUID = 8576918235293830742L;
	private boolean changed;

	public boolean hasChanges() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public abstract boolean isNew();

	public abstract boolean isValid();

	protected boolean equals(Object that, Object other) {
		return that == null ? other == null : that.equals(other);
	}
	
}
