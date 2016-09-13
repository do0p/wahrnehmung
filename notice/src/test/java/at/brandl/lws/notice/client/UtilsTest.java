package at.brandl.lws.notice.client;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.brandl.lws.notice.client.utils.Utils;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

@Ignore
public class UtilsTest {

	private LocalServiceTestHelper helper;

	@Before
	public void setUp() {
	helper = new LocalServiceTestHelper();
	helper.setUp();
	}
	
	@Test
	public void printQuestionnaire() {
		
		GwtQuestionnaire questionnaire = new GwtQuestionnaire();
		questionnaire.setTitle("title");
		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		String html = Utils.createPrintQuestionnaire(questionnaire, answers);
		System.out.println(html);
	}
	
}
