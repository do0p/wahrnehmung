package at.brandl.lws.notice.shared.model;

import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.validator.GwtTemplateValidator;

public class GwtTemplate extends GwtModel {

	private static final long serialVersionUID = -5601175608073078955L;

	private String key;
	private String name;
	private String template;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTemplate() {
		return template == null ? "" : template;
	}

	public void setTemplate(String template) {
		if (!equals(template, this.template)) {
			setChanged(true);
			this.template = template;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!equals(name, this.name)) {
			setChanged(true);
			this.name = name;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GwtTemplate other = (GwtTemplate) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

	@Override
	public boolean isNew() {
		return Utils.isEmpty(key);
	}

	@Override
	public boolean isValid() {
		return GwtTemplateValidator.validate(this);
	}

}
