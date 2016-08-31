package at.brandl.lws.notice.server.dao.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;
import java.util.regex.Pattern;

import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.shared.service.FormPrinter;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

public class FormParser extends FormPrinter {

	private static final String GROUP_DELIMITER = "-";
	private static final String OPTION_DELIMITER_PATTERN = "\\"
			+ OPTION_DELIMITER;
	private static final String TEXT = "[-+*/\\w\\s.?!,üÜöÖäÄß():]+";
	private static final String WHITE_SPACE = "\\s*";
	private static final String QUESTION_MARK_PATTERN = "\\" + QUESTION_MARK;
	private static final String VALUE = WHITE_SPACE + "\\w+" + WHITE_SPACE;

	private static final Pattern QUESTION_LABEL_PATTERN = Pattern
			.compile("(?<=" + QUESTION_MARK_PATTERN + ")" + TEXT + "(?="
					+ ANSWER_MARK + ")");
	private static final Pattern MULTIPLE_CHOICE_ANSWER_PATTERN = Pattern
			.compile(TEXT + SEPERATOR + VALUE + "(" + OPTION_DELIMITER_PATTERN
					+ TEXT + SEPERATOR + VALUE + ")+");

	public GwtQuestionnaire parse(String formText) {

		if (StringUtil.isEmptyOrWhitespace(formText)) {
			return new GwtQuestionnaire();
		}

		try (StringReader reader = new StringReader(formText)) {

			return parseForm(new BufferedReader(reader));

		} catch (IOException e) {
			throw new IllegalArgumentException("could not parse input: "
					+ formText, e);
		}

	}


	private GwtQuestionnaire parseForm(BufferedReader reader)
			throws IOException {

		GwtQuestionnaire form = new GwtQuestionnaire();

		form.setTitle(reader.readLine().trim());

		while (reader.ready()) {

			GwtQuestionGroup group = parseQuestionGroup(reader);
			if (group == null) {
				break;
			}

			form.addQuestionGroup(group);
		}

		return form;
	}

	private GwtQuestionGroup parseQuestionGroup(BufferedReader reader)
			throws IOException {

		GwtQuestionGroup group = new GwtQuestionGroup();

		String line = reader.readLine();
		if (line == null) {
			return null;
		}

		group.setTitle(parseGroupTitle(line));

		while (reader.ready()) {

			String readLine = reader.readLine();
			if (readLine == null) {
				break;
			}

			try (Scanner scanner = new Scanner(readLine)) {

				if (scanner.hasNext(GROUP_DELIMITER)) {
					break;
				}

				group.addQuestion(parseQuestion(scanner));
			}
		}

		return group;
	}

	private String parseGroupTitle(String line) throws IOException {

		String title = line.substring(GROUP_DELIMITER.length()).trim();
		if (!title.isEmpty()) {
			return title;
		}
		return null;
	}

	private GwtQuestion parseQuestion(Scanner scanner) {

		scanner.skip(QUESTION_MARK_PATTERN);

		GwtQuestion question = new GwtQuestion();
		question.setLabel(parseQuestionLabel(scanner));
		question.setTemplate(parseAnswerTemplate(scanner));
		return question;
	}

	private String parseQuestionLabel(Scanner scanner) {

		String label = scanner.findInLine(QUESTION_LABEL_PATTERN);
		if (label == null) {
			throw new IllegalArgumentException(
					"invalid input: no question label defined at "
							+ scanner.nextLine());
		}

		return label.trim();
	}

	private GwtAnswerTemplate parseAnswerTemplate(Scanner scanner) {

		scanner.skip(ANSWER_MARK);
		String answerTemplateText = scanner
				.findInLine(MULTIPLE_CHOICE_ANSWER_PATTERN);

		if (answerTemplateText != null) {
			return parseMultipleChoiceAnswerTemplate(answerTemplateText);
		}

		throw new IllegalArgumentException("could not parse input "
				+ scanner.nextLine());
	}

	private GwtAnswerTemplate parseMultipleChoiceAnswerTemplate(
			String answerTemplateText) {

		GwtMultipleChoiceAnswerTemplate multipleChoiceAnswerTemplate = new GwtMultipleChoiceAnswerTemplate();

		Scanner answerTemplateScanner = new Scanner(answerTemplateText);
		answerTemplateScanner.useDelimiter("[" + SEPERATOR + OPTION_DELIMITER
				+ "]");

		while (answerTemplateScanner.hasNext()) {

			GwtMultipleChoiceOption option = parseMultipleChoiceOption(answerTemplateScanner);
			multipleChoiceAnswerTemplate.addOption(option);
		}

		return multipleChoiceAnswerTemplate;
	}

	private GwtMultipleChoiceOption parseMultipleChoiceOption(Scanner scanner) {

		GwtMultipleChoiceOption option = new GwtMultipleChoiceOption();
		option.setLabel(scanner.next().trim());
		option.setValue(scanner.next().trim());
		return option;
	}

}
