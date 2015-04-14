package at.brandl.lws.notice.server.dao.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.service.FormParser;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire.Cache;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class FormDsDaoTest extends AbstractDsDaoTest {

	private static final String TITLE = "title";

	private static final String NL = System.lineSeparator();

	private FormDsDao formDao;
	private GwtQuestionnaire form;

	private Key sectionKey ;

	@Before
	public void setUp() {
		sectionKey = KeyFactory.createKey(SectionDsDao.SECTION_KIND, 1);
		form = createQuestionnaire();
		formDao = DaoRegistry.get(FormDsDao.class);
	}



	@Test
	public void insertAndRead() {
		
		formDao.storeQuestionnaire(form);
		String key = form.getKey();
	
		GwtQuestionnaire storedForm = formDao.getQuestionnaire(key);
		Assert.assertNotNull(storedForm);
		
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
		
		Assert.assertEquals(form, storedForm);
		
		removeFromCacheStringKey(key);
		assertCacheContainsNotStringKey(key);
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
		assertCacheContainsStringKey(key);
		Assert.assertNotNull(storedForm);
		
		Assert.assertEquals(form, storedForm);
	}

	@Test
	public void getOldestFormFirst() {
		
		GwtQuestionnaire form1 = new GwtQuestionnaire();
		form1.setTitle(TITLE );
		form1.setSection(KeyFactory.keyToString(sectionKey));

		GwtQuestionnaire form2 = new GwtQuestionnaire();
		form2.setTitle(TITLE);
		form2.setSection(KeyFactory.keyToString(sectionKey));
		
		formDao.storeQuestionnaire(form1);
		formDao.storeQuestionnaire(form2);
		String form1Key = form1.getKey();
		String form2Key = form2.getKey();
		Assert.assertNotEquals(form1Key, form2Key);
		
		List<GwtQuestionnaire> allQuestionnaires = formDao.getAllQuestionnaires();
		Assert.assertEquals(2, allQuestionnaires.size());
		Assert.assertEquals(form1Key, allQuestionnaires.get(0).getKey());
		Assert.assertEquals(form2Key, allQuestionnaires.get(1).getKey());
	}
	
	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	private GwtQuestionnaire createQuestionnaire() {
		String formText = readFromFile("form.txt");
		GwtQuestionnaire form = new FormParser().parse(formText);
		form.setSection(KeyFactory.keyToString(sectionKey ));
		return form;
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

}
