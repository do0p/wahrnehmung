package at.lws.wnm.shared.model;

public class GwtSummary extends GwtBeobachtung {

	private static final long serialVersionUID = -1897793359951575090L;
	int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int compareTo(GwtBeobachtung other) {

		if (!(other instanceof GwtSummary)) {
			return -1;
		}

		int result = ((GwtSummary) other).getCount()
				- ((GwtSummary) this).getCount();

		if (result != 0) {
			return result;
		}

		return super.compareTo(other);
	}
}
