package at.brandl.lws.notice.server.dao.ds.converter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import at.brandl.lws.notice.dao.DsUtil;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.ObjectUtils;
import at.brandl.lws.notice.shared.util.Constants.Child;

public class GwtChildConverter {

	public static Entity toEntity(GwtChild gwtChild) {
		final String key = gwtChild.getKey();
		final Entity child;
		if (key == null) {
			child = new Entity(Child.KIND);
		} else {
			child = new Entity(DsUtil.toKey(key));
		}
		child.setProperty(Child.FIRSTNAME, gwtChild.getFirstName());
		child.setProperty(Child.LASTNAME, gwtChild.getLastName());
		child.setProperty(Child.BIRTHDAY, gwtChild.getBirthDay());
		child.setProperty(Child.BEGIN_YEAR, gwtChild.getBeginYear());
		child.setProperty(Child.BEGIN_GRADE, gwtChild.getBeginGrade());
		child.setProperty(Child.ARCHIVED, gwtChild.getArchived());
		List<Date> developementDialogueDates = gwtChild.getDevelopementDialogueDates();
		if (developementDialogueDates != null && !developementDialogueDates.isEmpty()) {
			Collections.sort(developementDialogueDates);
			child.setProperty(Child.DEVELOPEMENT_DIALOGUE_DATES, developementDialogueDates);
			child.setProperty(Child.LAST_DEVELOPEMENT_DIALOGUE_DATE,
					developementDialogueDates.get(developementDialogueDates.size() - 1));
		}
		return child;
	}

	@SuppressWarnings("unchecked")
	public static GwtChild toGwtChild(Entity child) {
		final GwtChild gwtChild = new GwtChild();
		gwtChild.setKey(DsUtil.toString(child.getKey()));
		gwtChild.setFirstName((String) child.getProperty(Child.FIRSTNAME));
		gwtChild.setLastName((String) child.getProperty(Child.LASTNAME));
		gwtChild.setBirthDay((Date) child.getProperty(Child.BIRTHDAY));
		gwtChild.setBeginYear((Long) child.getProperty(Child.BEGIN_YEAR));
		gwtChild.setBeginGrade((Long) child.getProperty(Child.BEGIN_GRADE));
		gwtChild.setArchived((Boolean) child.getProperty(Child.ARCHIVED));
		gwtChild.setDevelopementDialogueDates((List<Date>) child.getProperty(Child.DEVELOPEMENT_DIALOGUE_DATES));
		return gwtChild;
	}

	public static Function<Entity, GwtChild> getEntityConverter() {
		return new Function<Entity, GwtChild>() {
			@Override
			public GwtChild apply(Entity entity) {
				return toGwtChild(entity);
			}
		};
	}

	public static class KeySelector implements Predicate<GwtChild> {
		private final String key;

		public KeySelector(String key) {
			this.key = key;
		}

		@Override
		public boolean apply(GwtChild child) {
			return key != null && child != null && key.equals(child.getKey());
		}
	}

	public static class ChildSelector implements Predicate<GwtChild> {
		private final GwtChild child;

		public ChildSelector(GwtChild child) {
			this.child = child;
		}

		@Override
		public boolean apply(GwtChild child) {
			return this.child != null && child != null
					&& ObjectUtils.equals(this.child.getFirstName(), child.getFirstName())
					&& ObjectUtils.equals(this.child.getLastName(), child.getLastName())
					&& ObjectUtils.equals(this.child.getBirthDay(), child.getBirthDay());
		}
	}
	
	public static class BirthdaySelector implements Predicate<GwtChild> {

		private final Date date;
		private final boolean after;
		
		public BirthdaySelector(Date birthDay, boolean after) {
			date = birthDay;
			this.after = after;
		}
		
		@Override
		public boolean apply(GwtChild child) {
			if(date == null || child == null || child.getBirthDay() == null) {
				return false;
			}
			return after ? child.getBirthDay().after(date) : !child.getBirthDay().after(date);
		}
		
	}

}
