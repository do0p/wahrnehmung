package at.brandl.lws.notice.server.dao.ds.converter;

import static at.brandl.lws.notice.shared.util.Constants.Section.ARCHIVED;
import static at.brandl.lws.notice.shared.util.Constants.Section.KIND;
import static at.brandl.lws.notice.shared.util.Constants.Section.NAME;
import static at.brandl.lws.notice.shared.util.Constants.Section.POS;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.model.ObjectUtils;

public class GwtSectionConverter {

	public static Entity toEntity(GwtSection gwtSection) {
		final String key = gwtSection.getKey();
		final Entity entity;
		if (key == null) {
			final String parentKey = gwtSection.getParentKey();
			if (parentKey == null) {
				entity = new Entity(KIND);
			} else {
				entity = new Entity(KIND, DsUtil.toKey(parentKey));
			}
		} else {
			entity = new Entity(DsUtil.toKey(key));
		}
		entity.setProperty(POS, gwtSection.getPos());
		entity.setProperty(NAME, gwtSection.getSectionName());
		entity.setProperty(ARCHIVED, gwtSection.getArchived());
		return entity;
	}

	public static GwtSection toGwtSection(Entity entity) {
		final GwtSection section = new GwtSection();
		section.setKey(DsUtil.toString(entity.getKey()));
		section.setSectionName((String) entity.getProperty(NAME));
		Long pos = (Long) entity.getProperty(POS);
		if (pos == null) {
			pos = 0l;
		}
		section.setPos(pos);
		Boolean archived = (Boolean) entity.getProperty(ARCHIVED);
		if (archived == null) {
			archived = Boolean.FALSE;
		}
		section.setArchived(archived);
		final Key parentKey = entity.getParent();
		if (parentKey != null) {
			section.setParentKey(DsUtil.toString(parentKey));
		}
		return section;
	}

	public static Function<Entity, GwtSection> getEntityConverter() {
		return new Function<Entity, GwtSection>() {

			@Override
			public GwtSection apply(Entity input) {
				return toGwtSection(input);
			}
		};
	}

	public static class KeySelector implements Predicate<GwtSection> {
		private final String key;

		public KeySelector(String key) {
			this.key = key;
		}

		@Override
		public boolean apply(GwtSection section) {
			return key != null && section != null && key.equals(section.getKey());
		}
	}

	public static class SectionSelector implements Predicate<GwtSection> {
		private final GwtSection section;

		public SectionSelector(GwtSection section) {
			this.section = section;
		}

		@Override
		public boolean apply(GwtSection section) {
			return this.section != null && section != null
					&& ObjectUtils.equals(this.section.getSectionName(), section.getSectionName())
					&& ObjectUtils.equals(this.section.getParentKey(), section.getParentKey());
		}
	}
}
