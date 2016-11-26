package at.brandl.lws.notice.server.dao.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswerTemplate;
import at.brandl.lws.notice.model.GwtMultipleChoiceOption;
import at.brandl.lws.notice.model.GwtQuestion;
import at.brandl.lws.notice.model.GwtQuestionGroup;
import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire.Cache;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;

public class FormDsDaoTest extends AbstractDsDaoTest {

	private static final String NEW_TITLE = "neuer title";
	private static final String NL = System.lineSeparator();

	private FormDsDao formDao;
	private GwtQuestionnaire form;
	private Key sectionKey1;
	private Key sectionKey2;

	@Before
	public void setUp() {
		sectionKey1 = KeyFactory.createKey(SectionDsDao.SECTION_KIND, 1);
		sectionKey2 = KeyFactory.createKey(SectionDsDao.SECTION_KIND, 2);
		form = createQuestionnaire();
		formDao = DaoRegistry.get(FormDsDao.class);
	}

	@Test
	public void insertAndRead() {

		formDao.storeQuestionnaire(form);
		String key = form.getKey();

		GwtQuestionnaire storedForm = formDao.getQuestionnaire(key);
		Assert.assertNotNull(storedForm);
		assertForm(storedForm);
		Assert.assertEquals(form, storedForm);
	}

	@Test
	public void worksWithCache() {

		formDao.storeQuestionnaire(form);
		String key = form.getKey();

		assertCacheContainsStringKey(key);
		assertDatastoreContains(key);
		removeFromDatastore(key);
		assertDatastoreContainsNot(key);

		GwtQuestionnaire storedForm = formDao.getQuestionnaire(key);
		Assert.assertNotNull(storedForm);
		assertForm(storedForm);
		Assert.assertEquals(form, storedForm);

		removeFromCacheStringKey(key);
		assertCacheContainsNotStringKey(key);

		try {
			storedForm = formDao.getQuestionnaire(key);
			Assert.fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected exception
		}

	}
	
	@Test
	public void worksWithoutCache() {

		formDao.storeQuestionnaire(form);
		String key = form.getKey();

		assertCacheContainsStringKey(key);
		assertDatastoreContains(key);
		removeFromCacheStringKey(key);
		assertCacheContainsNotStringKey(key);

		GwtQuestionnaire storedForm = formDao.getQuestionnaire(key);
		Assert.assertNotNull(storedForm);
		assertForm(storedForm);
		Assert.assertEquals(form, storedForm);

		assertCacheContainsStringKey(key);
		Assert.assertNotNull(storedForm);

		Assert.assertEquals(form, storedForm);
	}

	@Test
	public void cacheIsUnique() {

		List<GwtQuestionnaire> allQuestionnaires = formDao
				.getAllQuestionnaires();
		Assert.assertTrue(allQuestionnaires.isEmpty());
		Assert.assertNull(form.getKey());

		formDao.storeQuestionnaire(form);
		allQuestionnaires = formDao.getAllQuestionnaires();
		Assert.assertEquals(1, allQuestionnaires.size());
		String formKey = form.getKey();
		Assert.assertNotNull(formKey);

		formDao.storeQuestionnaire(form);
		allQuestionnaires = formDao.getAllQuestionnaires();
		Assert.assertEquals(1, allQuestionnaires.size());
		Assert.assertEquals(formKey, form.getKey());
	}

	@Test
	public void updateQuestionnaire() {

		formDao.storeQuestionnaire(form);

		String sectionKey = KeyFactory.keyToString(sectionKey2);
		form.setSection(sectionKey);
		form.setTitle(NEW_TITLE);
		formDao.storeQuestionnaire(form);

		form = formDao.getQuestionnaire(form.getKey());

		Assert.assertEquals(NEW_TITLE, form.getTitle());
		Assert.assertEquals(sectionKey, form.getSection());
	}

	@Test
	public void updateQuestionGroup() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();
		questionGroup.setTitle(NEW_TITLE);
		formDao.storeQuestionnaire(form);

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());
		Assert.assertEquals(NEW_TITLE, questionGroup.getTitle());
	}

	@Test
	public void updateQuestionGroupWithoutCache() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();
		questionGroup.setTitle(NEW_TITLE);
		formDao.storeQuestionnaire(form);

		removeFromCacheStringKey(form.getKey());

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());
		Assert.assertEquals(NEW_TITLE, questionGroup.getTitle());
	}

	@Test
	public void updateQuestion() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		GwtQuestion question = questions.get(0);
		String questionKey = question.getKey();
		GwtAnswerTemplate answerTemplate = question.getAnswerTemplate();

		question.setLabel(NEW_TITLE);
		formDao.storeQuestionnaire(form);

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize, questions.size());
		question = questions.get(0);
		Assert.assertEquals(NEW_TITLE, question.getLabel());
		Assert.assertNotEquals(questionKey, question.getKey());
		Assert.assertEquals(answerTemplate, question.getAnswerTemplate());
		Assert.assertNotEquals(answerTemplate.getKey(), question.getAnswerTemplate().getKey());
	}

	@Test
	public void updateQuestionWithoutCache() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		GwtQuestion question = questions.get(0);
		String questionKey = question.getKey();
		GwtAnswerTemplate answerTemplate = question.getAnswerTemplate();

		question.setLabel(NEW_TITLE);
		formDao.storeQuestionnaire(form);
		removeFromCacheStringKey(form.getKey());

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize, questions.size());
		question = questions.get(0);
		Assert.assertEquals(NEW_TITLE, question.getLabel());
		Assert.assertNotEquals(questionKey, question.getKey());
		Assert.assertEquals(answerTemplate, question.getAnswerTemplate());
		Assert.assertNotEquals(answerTemplate.getKey(), question.getAnswerTemplate().getKey());
	}
	
	@Test
	public void deleteQuestion() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		questions.remove(0);
		
		formDao.storeQuestionnaire(form);

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize - 1, questions.size());
	}
	
	@Test
	public void deleteQuestionWithoutCache() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		questions.remove(0);
		
		formDao.storeQuestionnaire(form);
		removeFromCacheStringKey(form.getKey());

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize - 1, questions.size());
	}

	@Test
	public void deleteAnsweredQuestion() {
		
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		GwtQuestion question = questions.get(0);
		String questionKey = question.getKey();
		String label = question.getLabel();
		String answerTemplateKey = question.getAnswerTemplate().getKey();

		GwtAnswer answer = new GwtMultipleChoiceAnswer();
		answer.setQuestionKey(questionKey);
		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setQuestionnaireKey(form.getKey());
		answers.addAnswer(answer);

		questions.remove(0);
		formDao.storeQuestionnaire(form);
		
		form = formDao.getQuestionnaire(answers);
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize - 1, questions.size());
		
		GwtQuestionGroup archivedQuestionGroup = form.getArchivedQuestionGroup();
		List<GwtQuestion> archivedQuestions = archivedQuestionGroup.getQuestions();
		Assert.assertEquals(1, archivedQuestions.size());
		question = archivedQuestions.get(0);
		Assert.assertEquals(label, question.getLabel());
		Assert.assertEquals(questionKey, question.getKey());
		Assert.assertEquals(answerTemplateKey, question.getAnswerTemplate().getKey());
		
	}
	
	@Test
	public void moveQuestionToOtherGroup() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup1 = groups.get(0);
		String groupKey1 = questionGroup1.getKey();
		List<GwtQuestion> questionsOfGroup1 = questionGroup1.getQuestions();
		int questionsSizeOfGroup1 = questionsOfGroup1.size();

		GwtQuestionGroup questionGroup2 = groups.get(1);
		String groupKey2 = questionGroup2.getKey();
		List<GwtQuestion> questionsOfGroup2 = questionGroup2.getQuestions();
		int questionsSizeOfGroup2 = questionsOfGroup1.size();
		
		GwtQuestion movedQuestion = questionsOfGroup1.remove(0);
		questionsOfGroup2.add(movedQuestion);
		
		formDao.storeQuestionnaire(form);

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());

		questionGroup1 = groups.get(0);
		Assert.assertEquals(groupKey1, questionGroup1.getKey());
		questionsOfGroup1 = questionGroup1.getQuestions();
		Assert.assertEquals(questionsSizeOfGroup1 - 1, questionsOfGroup1.size());
		
		questionGroup2 = groups.get(1);
		Assert.assertEquals(groupKey2, questionGroup2.getKey());
		questionsOfGroup2 = questionGroup2.getQuestions();
		Assert.assertEquals(questionsSizeOfGroup2 + 1, questionsOfGroup2.size());
		
		GwtQuestion question = questionsOfGroup2.get(questionsSizeOfGroup2);
		Assert.assertEquals(movedQuestion, question);
	}
	
	
	@Test
	public void moveQuestionToOtherGroupWithoutCache() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup1 = groups.get(0);
		String groupKey1 = questionGroup1.getKey();
		List<GwtQuestion> questionsOfGroup1 = questionGroup1.getQuestions();
		int questionsSizeOfGroup1 = questionsOfGroup1.size();

		GwtQuestionGroup questionGroup2 = groups.get(1);
		String groupKey2 = questionGroup2.getKey();
		List<GwtQuestion> questionsOfGroup2 = questionGroup2.getQuestions();
		int questionsSizeOfGroup2 = questionsOfGroup1.size();
		
		GwtQuestion movedQuestion = questionsOfGroup1.remove(0);
		questionsOfGroup2.add(movedQuestion);
		
		formDao.storeQuestionnaire(form);
		removeFromCacheStringKey(form.getKey());

		form = formDao.getQuestionnaire(form.getKey());
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());

		questionGroup1 = groups.get(0);
		Assert.assertEquals(groupKey1, questionGroup1.getKey());
		questionsOfGroup1 = questionGroup1.getQuestions();
		Assert.assertEquals(questionsSizeOfGroup1 - 1, questionsOfGroup1.size());
		
		questionGroup2 = groups.get(1);
		Assert.assertEquals(groupKey2, questionGroup2.getKey());
		questionsOfGroup2 = questionGroup2.getQuestions();
		Assert.assertEquals(questionsSizeOfGroup2 + 1, questionsOfGroup2.size());
		
		GwtQuestion question = questionsOfGroup2.get(questionsSizeOfGroup2);
		Assert.assertEquals(movedQuestion, question);
	}
	
	@Test
	public void getFormWithArchivedQuestion() {
		formDao.storeQuestionnaire(form);

		List<GwtQuestionGroup> groups = form.getGroups();
		int groupSize = groups.size();
		GwtQuestionGroup questionGroup = groups.get(0);
		String groupKey = questionGroup.getKey();

		List<GwtQuestion> questions = questionGroup.getQuestions();
		int questionsSize = questions.size();
		GwtQuestion question = questions.get(0);
		String questionKey = question.getKey();
		String label = question.getLabel();
		String answerTemplateKey = question.getAnswerTemplate().getKey();

		GwtAnswer answer = new GwtMultipleChoiceAnswer();
		answer.setQuestionKey(questionKey);
		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setQuestionnaireKey(form.getKey());
		answers.addAnswer(answer);

		question.setLabel(NEW_TITLE);
		formDao.storeQuestionnaire(form);
		
		form = formDao.getQuestionnaire(answers);
		groups = form.getGroups();
		Assert.assertEquals(groupSize, groups.size());
		questionGroup = groups.get(0);
		Assert.assertEquals(groupKey, questionGroup.getKey());

		questions = questionGroup.getQuestions();
		Assert.assertEquals(questionsSize, questions.size());
		question = questions.get(0);
		Assert.assertEquals(label, question.getLabel());
		Assert.assertEquals(questionKey, question.getKey());
		Assert.assertEquals(answerTemplateKey, question.getAnswerTemplate().getKey());
		
	}

	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	@Override
	protected void assertCacheContainsStringKey(String key) {
		Assert.assertTrue(getAllFormsMapFromCache().containsKey(key));
	}

	@Override
	protected void assertCacheContainsNotStringKey(String key) {
		Assert.assertFalse(getAllFormsMapFromCache().containsKey(key));
	}

	@Override
	protected void removeFromCacheStringKey(String key) {
		Map<String, GwtQuestionnaire> questionnaires = getAllFormsMapFromCache();
		questionnaires.remove(key);
		putCacheEntry(FormDsDao.ALL_FORMS, questionnaires);
	}

	@SuppressWarnings("unchecked")
	private Map<String, GwtQuestionnaire> getAllFormsMapFromCache() {
		return (Map<String, GwtQuestionnaire>) getCacheEntry(FormDsDao.ALL_FORMS);
	}

	private GwtQuestionnaire createQuestionnaire() {
		String formText = readFromFile("form.txt");
		GwtQuestionnaire form = new FormParser().parse(formText);
		form.setSection(KeyFactory.keyToString(sectionKey1));
		assertForm(form);
		return form;
	}

	private void assertForm(GwtQuestionnaire form) {
		Assert.assertFalse(form.getTitle().isEmpty());
		Assert.assertFalse(form.getGroups().isEmpty());
		Assert.assertFalse(form.getSection().isEmpty());
		for (GwtQuestionGroup group : form.getGroups()) {
			assertGroup(group);
		}
	}

	private void assertGroup(GwtQuestionGroup group) {
		Assert.assertFalse(group.getQuestions().isEmpty());
		for (GwtQuestion question : group.getQuestions()) {
			assertQuestion(question);
		}
	}

	private void assertQuestion(GwtQuestion question) {
		Assert.assertFalse(question.getLabel().isEmpty());
		assertMultipleChoiceAnswerTemplate(question);
	}

	private void assertMultipleChoiceAnswerTemplate(GwtQuestion question) {
		Assert.assertTrue(question.getAnswerTemplate() instanceof GwtMultipleChoiceAnswerTemplate);
		for (GwtMultipleChoiceOption option : ((GwtMultipleChoiceAnswerTemplate) question
				.getAnswerTemplate()).getOptions()) {
			assertOption(option);
		}
	}

	private void assertOption(GwtMultipleChoiceOption option) {
		Assert.assertFalse(option.getLabel().isEmpty());
		Assert.assertFalse(option.getValue().isEmpty());
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
	
	protected LocalDatastoreServiceTestConfig createDsConfig() {
		return new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy();
	}
	
}
