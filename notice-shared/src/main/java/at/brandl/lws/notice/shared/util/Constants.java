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
		public static final String BEGIN_YEAR = "beginYear";
		public static final String BEGIN_GRADE = "beginGrade";
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

	public static class Questionnaire {

		public static final String KIND = "QuestionnaireDs";
		public static final String TITLE = "title";
		public static final String SECTION = "sectionKey";
		public static final String CREATE_DATE = "createDate";
		public static final String UPDATE_DATE = "updateDate";

		public static class Cache {

			public static final String NAME = "questionnaire";
		}
	}

	public static class QuestionGroup {

		public static final String KIND = "QuestionGroupDs";
		public static final String TITLE = "title";
		public static final String ORDER = "order";
		public static String ARCHIVE_DATE = "archived";
	}

	public static class Question {

		public static final String KIND = "QuestionDs";
		public static final String LABEL = "label";
		public static final String ORDER = "order";
		public static final String REPLACED = "replaced";
		public static final String ARCHIVE_DATE = "archived";
	}

	public static class AnswerTemplate {

		public static final String KIND = "AnswerTemplateDs";
		public static final String Type = "type";
	}

	public static class MultipleChoiceOption {

		public static final String KIND = "MultipleChoiceOptionDs";
		public static final String LABEL = "label";
		public static final String VALUE = "value";
		public static final String ORDER = "order";
	}

	public static class QuestionnaireAnswers {

		public static final String KIND = "QuestionnaireAnswersDs";
		public static final String QUESTIONNAIRE_KEY = "questionnaireKey";

		public static class Cache {

			public static final String NAME = "questionnaireAnswers";
		}
	}

	public static class QuestionnaireAnswer {

		public static final String KIND = "QuestionnaireAnswerDs";
		public static final String QUESTION_KEY = "questionKey";
		public static final String DATE = "date";
		public static final String USER = "user";
		public static final String VALUE = "value";
		public static final String TYPE = "type";
		public static final String CREATE_DATE = "createDate";
	}
	
	public static class Interaction {
		
		public static final String KIND = "InteractionDs";
		public static final String DATE = "date";
		public static final String PARTNER = "partner";
		public static final String COUNT = "count";
		
		public static class Cache {

			public static final String NAME = "interactionDao";
		}

	}

	public static final String MULTIPLE_CHOICE = "multipleChoice";
	public static final String DOCUMENTATION_FOLDER_NAME = "Berichte";
	public static final String NOTICE_ROOT_FOLDER_NAME = "Wahrnehmung Dokumente";
}
