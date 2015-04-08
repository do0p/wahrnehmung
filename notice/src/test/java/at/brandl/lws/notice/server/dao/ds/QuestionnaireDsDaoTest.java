package at.brandl.lws.notice.server.dao.ds;

import java.util.Arrays;
import java.util.Collection;
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
	private String questionKey;

	@Before
	public void setUp() {
		questionnaireDao = DaoRegistry.get(QuestionnaireDsDao.class);
		childKey = KeyFactory.createKeyString(Child.KIND, 1);
		questionnaireKey = KeyFactory.createKeyString(
				Questionnaire.KIND, 1);
		questionKey = KeyFactory.createKeyString(Question.KIND, 1);
		answers = createQuestionnaireAnswers();
	}

	protected LocalDatastoreServiceTestConfig createDsConfig() {
		return new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy();
	}
	
	@Test
	public void insertAndRead() {

		questionnaireDao.storeAnswers(answers, null);

		Collection<GwtQuestionnaireAnswers> allAnswers = questionnaireDao
				.getAllAnswers(childKey);
		Assert.assertNotNull(allAnswers);

		Assert.assertEquals(1, allAnswers.size());
	}

	// @Test
	// public void worksWithCache() {
	//
	// questionnaireDao.storeQuestionnaire(answers);
	// String key = answers.getKey();
	//
	// assertCacheContainsStringKey(key);
	// assertDatastoreContains(key);
	// removeFromDatastore(key);
	// assertDatastoreContainsNot(key);
	//
	// GwtQuestionnaire storedForm = questionnaireDao.getQuestionnaire(key);
	// Assert.assertNotNull(storedForm);
	//
	// Assert.assertEquals(answers, storedForm);
	//
	// removeFromCacheStringKey(key);
	// assertCacheContainsNotStringKey(key);
	// }
	//
	// @Test
	// public void worksWithoutCache() {
	//
	// questionnaireDao.storeQuestionnaire(answers);
	// String key = answers.getKey();
	//
	// assertCacheContainsStringKey(key);
	// assertDatastoreContains(key);
	// removeFromCacheStringKey(key);
	// assertCacheContainsNotStringKey(key);
	//
	// GwtQuestionnaire storedForm = questionnaireDao.getQuestionnaire(key);
	// assertCacheContainsStringKey(key);
	// Assert.assertNotNull(storedForm);
	//
	// Assert.assertEquals(answers, storedForm);
	// }
	//
	// @Test
	// public void getOldestFormFirst() {
	//
	// GwtQuestionnaire form1 = new GwtQuestionnaire();
	// GwtQuestionnaire form2 = new GwtQuestionnaire();
	// form1.setTitle(TITLE );
	// form2.setTitle(TITLE);
	// questionnaireDao.storeQuestionnaire(form1);
	// questionnaireDao.storeQuestionnaire(form2);
	// String form1Key = form1.getKey();
	// String form2Key = form2.getKey();
	// Assert.assertNotEquals(form1Key, form2Key);
	//
	// List<GwtQuestionnaire> allQuestionnaires =
	// questionnaireDao.getAllQuestionnaires();
	// Assert.assertEquals(2, allQuestionnaires.size());
	// Assert.assertEquals(form1Key, allQuestionnaires.get(0).getKey());
	// Assert.assertEquals(form2Key, allQuestionnaires.get(1).getKey());
	// }

	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	private GwtQuestionnaireAnswers createQuestionnaireAnswers() {

		GwtAnswer answer = new GwtMultipleChoiceAnswer();
		answer.setQuestionKey(questionKey);
		answer.setDate(new Date());
		answer.setValue(Arrays.asList("a"));
		answer.setUpdated(true);

		GwtQuestionnaireAnswers answers = new GwtQuestionnaireAnswers();
		answers.setQuestionnaireKey(questionnaireKey);
		answers.setChildKey(childKey);
		answers.addAnswer(answer);

		return answers;
	}

}
