package at.brandl.lws.notice.server.dao.ds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.model.GwtAnswer;
import at.brandl.lws.notice.model.GwtMultipleChoiceAnswer;
import at.brandl.lws.notice.model.GwtQuestionnaireAnswers;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.shared.util.Constants.Child;
import at.brandl.lws.notice.shared.util.Constants.Question;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire;
import at.brandl.lws.notice.shared.util.Constants.QuestionnaireAnswers.Cache;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;

public class QuestionnaireDsDaoTest extends AbstractDsDaoTest {

	private QuestionnaireDsDao questionnaireDao;
	private GwtQuestionnaireAnswers answers;

	private String childKey;
	private String questionnaireKey;
	private String questionKey1;
	private String questionKey2;

	@Before
	public void setUp() {
		questionnaireDao = DaoRegistry.get(QuestionnaireDsDao.class);
		childKey = KeyFactory.createKeyString(Child.KIND, 1);
		questionnaireKey = KeyFactory.createKeyString(Questionnaire.KIND, 1);
		questionKey1 = KeyFactory.createKeyString(Question.KIND, 1);
		questionKey2 = KeyFactory.createKeyString(Question.KIND, 2);
		answers = createQuestionnaireAnswers();
	}

	protected LocalDatastoreServiceTestConfig createDsConfig() {
		return new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy();
	}

	@Test
	public void createDateDoesNotChange() {
		questionnaireDao.storeAnswers(answers, null);
		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		GwtQuestionnaireAnswers storedAnswers = allAnswers.iterator().next();
		GwtAnswer storedAnswer = storedAnswers.getAnswers().iterator().next();
		Date createDate = storedAnswer.getCreateDate();
		Assert.assertNotNull(createDate);

		storedAnswer.setCreateDate(new Date(0));

		questionnaireDao.storeAnswers(storedAnswers, null);

		allAnswers = questionnaireDao.getAllAnswers(childKey);

		storedAnswers = allAnswers.iterator().next();
		storedAnswer = storedAnswers.getAnswers().iterator().next();
		Assert.assertEquals(createDate, storedAnswer.getCreateDate());

	}

	@Test
	public void insertAndRead() {

		questionnaireDao.storeAnswers(answers, null);

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		Assert.assertNotNull(allAnswers);
		Assert.assertEquals(1, allAnswers.size());
		GwtQuestionnaireAnswers storedAnswers = allAnswers.iterator().next();

		Assert.assertEquals(answers, storedAnswers);
	}

	@Test
	public void insert() {

		assertUpdated(answers, true);
		GwtQuestionnaireAnswers storedAnswers = questionnaireDao.storeAnswers(answers, null);
		Assert.assertEquals(answers, storedAnswers);
		assertUpdated(storedAnswers, false);

	}

	@Test
	public void notUpdatedIgnored() {

		GwtAnswer answer = createAnswer(questionKey2, false);
		answers.addAnswer(answer);

		questionnaireDao.storeAnswers(answers, null);

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		GwtQuestionnaireAnswers storedAnswers = allAnswers.iterator().next();

		Assert.assertEquals(1, storedAnswers.getAnswers().size());
		Assert.assertNotNull(storedAnswers.getAnswer(questionKey1));
		Assert.assertNull(storedAnswers.getAnswer(questionKey2));
	}

	@Test
	public void delete() {

		GwtAnswer answer = createAnswer(questionKey2, true);
		answers.addAnswer(answer);

		questionnaireDao.storeAnswers(answers, null);
		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		GwtQuestionnaireAnswers storedAnswers = allAnswers.iterator().next();
		Assert.assertEquals(2, storedAnswers.getAnswers().size());

		GwtAnswer answer2 = storedAnswers.getAnswer(questionKey2);
		answer2.setValue(Collections.emptyList());
		answer2.setUpdated(true);
		questionnaireDao.storeAnswers(storedAnswers, null);
		allAnswers = questionnaireDao.getAllAnswers(childKey);

		storedAnswers = allAnswers.iterator().next();
		Assert.assertEquals(1, storedAnswers.getAnswers().size());
		GwtAnswer answer1 = storedAnswers.getAnswer(questionKey1);
		answer1.setValue(Collections.emptyList());
		answer1.setUpdated(true);
		questionnaireDao.storeAnswers(storedAnswers, null);
		allAnswers = questionnaireDao.getAllAnswers(childKey);

		Assert.assertTrue(allAnswers.isEmpty());

	}

	private void assertUpdated(GwtQuestionnaireAnswers answers, boolean updated) {

		for (GwtAnswer answer : answers.getAnswers()) {
			Assert.assertEquals(updated, answer.isUpdated());
		}
	}

	@Test
	public void worksWithCache() {

		questionnaireDao.storeAnswers(answers, null);
		String key = answers.getKey();

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		assertCacheContainsStringKey(childKey);
		assertDatastoreContains(key);
		removeFromDatastore(key);
		assertDatastoreContainsNot(key);

		allAnswers = questionnaireDao.getAllAnswers(childKey);
		Assert.assertFalse(allAnswers.isEmpty());

		Assert.assertEquals(answers, allAnswers.iterator().next());

		removeFromCacheStringKey(childKey);
		assertCacheContainsNotStringKey(childKey);
	}

	@Test
	public void worksWithoutCache() {

		questionnaireDao.storeAnswers(answers, null);
		String key = answers.getKey();

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao.getAllAnswers(childKey);

		assertCacheContainsStringKey(childKey);
		assertDatastoreContains(key);
		removeFromCacheStringKey(childKey);
		assertCacheContainsNotStringKey(childKey);

		allAnswers = questionnaireDao.getAllAnswers(childKey);
		Assert.assertFalse(allAnswers.isEmpty());

		Assert.assertEquals(answers, allAnswers.iterator().next());
		assertCacheContainsStringKey(childKey);

	}

	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	private GwtQuestionnaireAnswers createQuestionnaireAnswers() {

		GwtAnswer answer = createAnswer(questionKey1, true);

		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setQuestionnaireKey(questionnaireKey);
		answers.setChildKey(childKey);
		answers.addAnswer(answer);

		return answers;
	}

	private GwtAnswer createAnswer(String questionKey, boolean updated) {
		GwtAnswer answer = new GwtMultipleChoiceAnswer();
		answer.setQuestionKey(questionKey);
		answer.setDate(new Date());
		answer.setValue(Arrays.asList("a"));
		answer.setUpdated(updated);
		return answer;
	}

}
