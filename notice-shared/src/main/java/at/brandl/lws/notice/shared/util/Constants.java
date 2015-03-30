package at.brandl.lws.notice.shared.util;

public class Constants {

	public static class Notice {

		public static final String KIND = "BeobachtungDs";
		public static final String DATE = "date";
		public static final String SECTION = "sectionKey";
		public static final String USER = "user";
		public static final String DURATION = "duration";
		public static final String TEXT = "text";
		public static final String SOCIAL = "social";
		
		public static class Cache {

			public static final String NAME = "beobachtungsDao";
		}
	}
	
	public static class ArchiveNotice {

		public static final String KIND = "BeobachtungArchiveDs";
		
		public static class Cache {

			public static final String NAME = "beobachtungsArchiveDao";
		}
	}

	public static class Child {

		public static final String KIND = "ChildDs";
		public static final String FIRSTNAME = "firstname";
		public static final String LASTNAME = "lastname";
		public static final String BIRTHDAY = "birthday";
		public static final String ARCHIVED = "archived";
		public static final String LAST_DEVELOPEMENT_DIALOGUE_DATE = "lastDialogueDate";
		public static final String DEVELOPEMENT_DIALOGUE_DATES = "dialogueDates";
		
		public static class Cache {

			public static final String NAME = "childDao";
			public static final String ALL_CHILDREN = "allChildren";
		}
	}
	
	public static class NoticeGroup {

		public static final String KIND = "BeobachtungsGroup";
		public static final String BEOBACHTUNG = "beobachtungsKey";
	}
	
	public static class ArchiveNoticeGroup {

		public static final String KIND = "BeobachtungsGroupArchiveDs";
	}

	public static class MigrationKeyMapping {
		
		public static final String KIND = "MigrationKeyMapping";
		public static final String TYPE = "type";
		public static final String NEW_KEY = "new";
		
		public static enum KeyMappingType {
			ARCHIVE_NOTICE, ARCHIVE_GROUP
		}
	}
}
