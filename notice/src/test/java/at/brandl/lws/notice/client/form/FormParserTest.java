package at.brandl.lws.notice.client.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.service.FormParser;

@Ignore
public class FormParserTest {

	private static final String NL = System.lineSeparator();
	private static final String FORM_TITLE = "Title mit ü";
	private static final String TEMPLATE_START = " :: ";
	private static final String GROUP_DELIMITER = "- ";
	private static final String QUESTION_START = "? ";
	private static final String ANSWER_TEMPLATE = "ja:j | nein:n | weiss nicht:v";
	private static final String GROUP_TITLE1 = "GroupTitle1 mit ß ü";
	private static final String GROUP_TITLE2 = "GroupTitle2";
	private static final String GROUP1 = GROUP_DELIMITER + GROUP_TITLE1;
	private static final String GROUP2 = GROUP_DELIMITER + GROUP_TITLE2;
	private static final String QUESTION_LABEL1 = "Frage1 * x + - / *üÜöÖäÄß:";
	private static final String QUESTION_LABEL2 = "Wort - Bild zuordnen";
	private static final String QUESTION2 = QUESTION_START + QUESTION_LABEL2
			+ TEMPLATE_START + ANSWER_TEMPLATE;
	private static final String QUESTION1 = QUESTION_START + QUESTION_LABEL1
			+ TEMPLATE_START + ANSWER_TEMPLATE;
	private FormParser parser;

	@Before
	public void setUp() {
		parser = new FormParser();
	}

	@Test
	public void parseEmptyForm() {

		String formText = "";

		GwtQuestionnaire form = parser.parse(formText);

		assertNotNull(form);
	}

	@Test
	public void parseFormTitle() {

		String formText = FORM_TITLE;

		GwtQuestionnaire form = parser.parse(formText);

		assertNotNull(form);
		assertEquals(FORM_TITLE, form.getTitle());
	}

	@Test
	public void parseGroupTitle() {

		String formText = FORM_TITLE + NL + GROUP1;

		GwtQuestionnaire form = parser.parse(formText);

		List<GwtQuestionGroup> groups = form.getGroups();
		assertNotNull(groups);
		assertEquals(1, groups.size());

		GwtQuestionGroup group = groups.get(0);
		assertNotNull(group);
		assertEquals(GROUP_TITLE1, group.getTitle());
	}

	@Test
	public void parseQuestion() {

		String formText = FORM_TITLE + NL + GROUP1 + NL + QUESTION1;

		GwtQuestionnaire form = parser.parse(formText);

		List<GwtQuestionGroup> groups = form.getGroups();
		GwtQuestionGroup group = groups.get(0);

		List<GwtQuestion> questions = group.getQuestions();
		assertEquals(1, questions.size());
		assertQuestion(questions.get(0), QUESTION_LABEL1);
	}

	@Test
	public void parseMultipleQuestions() {

		String formText = FORM_TITLE + NL + GROUP1 + NL + QUESTION1 + NL
				+ QUESTION2 + NL;

		GwtQuestionnaire form = parser.parse(formText);

		List<GwtQuestionGroup> groups = form.getGroups();
		GwtQuestionGroup group = groups.get(0);

		List<GwtQuestion> questions = group.getQuestions();
		assertEquals(2, questions.size());
		assertQuestion(questions.get(0), QUESTION_LABEL1);
		assertQuestion(questions.get(1), QUESTION_LABEL2);
	}

	@Test
	public void parseMultipleGroups() {

		String formText = FORM_TITLE + NL + GROUP1 + NL + QUESTION1 + NL + GROUP_DELIMITER + NL
				+ GROUP2 + NL + QUESTION2 + NL + GROUP_DELIMITER;

		GwtQuestionnaire form = parser.parse(formText);

		List<GwtQuestionGroup> groups = form.getGroups();
		assertEquals(2, groups.size());

		GwtQuestionGroup group1 = groups.get(0);
		assertEquals(GROUP_TITLE1, group1.getTitle());

		List<GwtQuestion> questions = group1.getQuestions();
		assertEquals(1, questions.size());
		assertQuestion(questions.get(0), QUESTION_LABEL1);

		GwtQuestionGroup group2 = groups.get(1);
		assertEquals(GROUP_TITLE2, group2.getTitle());

		questions = group2.getQuestions();
		assertEquals(1, questions.size());

		assertQuestion(questions.get(0), QUESTION_LABEL2);
	}

	@Test
	public void parseNoGroupTitleQuestions() {

		String formText = FORM_TITLE + NL + GROUP_DELIMITER + NL + QUESTION1;

		GwtQuestionnaire form = parser.parse(formText);

		List<GwtQuestionGroup> groups = form.getGroups();
		GwtQuestionGroup group = groups.get(0);
		assertNull(group.getTitle());

		List<GwtQuestion> questions = group.getQuestions();
		assertEquals(1, questions.size());
		assertQuestion(questions.get(0), QUESTION_LABEL1);
	}

	@Test
	public void emptyFormToString() {

		String formText = parser.toString(new GwtQuestionnaire());

		assertEquals("null" + NL, formText);
	}

	@Test
	public void formTitleToString() {

		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle(FORM_TITLE);

		String formText = parser.toString(form);

		assertEquals(FORM_TITLE + NL, formText);
	}

	@Test
	public void groupTitleToString() {

		GwtQuestionGroup group = new GwtQuestionGroup();
		group.setTitle(GROUP_TITLE1);

		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle(FORM_TITLE);
		form.addQuestionGroup(group);

		String formText = parser.toString(form);

		assertEquals(FORM_TITLE + NL + GROUP1 + NL + "-" + NL, formText);
	}

	@Test
	public void questionToString() {

		GwtMultipleChoiceOption option1 = new GwtMultipleChoiceOption();
		option1.setLabel("ja");
		option1.setValue("j");

		GwtMultipleChoiceOption option2 = new GwtMultipleChoiceOption();
		option2.setLabel("nein");
		option2.setValue("n");

		GwtMultipleChoiceOption option3 = new GwtMultipleChoiceOption();
		option3.setLabel("weiss nicht");
		option3.setValue("v");

		GwtMultipleChoiceAnswerTemplate template = new GwtMultipleChoiceAnswerTemplate();
		template.addOption(option1);
		template.addOption(option2);
		template.addOption(option3);

		GwtQuestion question = new GwtQuestion();
		question.setLabel(QUESTION_LABEL1);
		question.setTemplate(template);

		GwtQuestionGroup group = new GwtQuestionGroup();
		group.setTitle(GROUP_TITLE1);
		group.addQuestion(question);

		GwtQuestionnaire form = new GwtQuestionnaire();
		form.setTitle(FORM_TITLE);
		form.addQuestionGroup(group);

		String formText = parser.toString(form);

		assertEquals(FORM_TITLE + NL + GROUP1 + NL + QUESTION1 + NL + "-" + NL, formText);
	}

	@Test
	public void parseCompleteForm() {
		String formText = readFromFile("form.txt");
		GwtQuestionnaire form = parser.parse(formText);
		assertNotNull(form);
		assertEquals("Mathematische Grundlagen", form.getTitle());
		List<GwtQuestionGroup> groups = form.getGroups();
		assertEquals(13, groups.size());
		
		formText = parser.toString(form);
		GwtQuestionnaire formNew = parser.parse(formText);
		
		assertEquals(form, formNew);
		assertEquals(formText, parser.toString(formNew));
	}

	private String readFromFile(String fileName) {
		StringBuilder text = new StringBuilder();
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(fileName);
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));

			while (bufferedReader.ready()) {
				text.append(bufferedReader.readLine());
				text.append(NL);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return text.toString();
	}

	private void assertQuestion(GwtQuestion question, String questionLabel) {

		assertNotNull(question);
		assertEquals(questionLabel, question.getLabel());
		GwtAnswerTemplate template = question.getAnswerTemplate();
		assertNotNull(template);
		assertTrue(template instanceof GwtMultipleChoiceAnswerTemplate);
		List<GwtMultipleChoiceOption> options = ((GwtMultipleChoiceAnswerTemplate)template).getOptions();
		assertNotNull(options);
		assertEquals(3, options.size());

		assertOption(options.get(0), "ja", "j");
		assertOption(options.get(1), "nein", "n");
		assertOption(options.get(2), "weiss nicht", "v");
	}

	private void assertOption(GwtMultipleChoiceOption option, String label,
			String value) {
		assertEquals(label, option.getLabel());
		assertEquals(value, option.getValue());
	}
}
