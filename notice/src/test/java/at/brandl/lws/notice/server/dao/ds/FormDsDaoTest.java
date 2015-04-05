package at.brandl.lws.notice.server.dao.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.brandl.lws.notice.model.GwtQuestionnaire;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.service.FormParser;
import at.brandl.lws.notice.shared.util.Constants.Questionnaire.Cache;

public class FormDsDaoTest extends AbstractDsDaoTest {

	private static final String NL = System.lineSeparator();

	private FormDsDao formDao;
	private GwtQuestionnaire form;

	@Before
	public void setUp() {
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

	@Override
	protected String getMemCacheServiceName() {
		return Cache.NAME;
	}

	private GwtQuestionnaire createQuestionnaire() {
		String formText = readFromFile("form.txt");
		GwtQuestionnaire form = new FormParser().parse(formText);
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
