package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.List;

import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

public class TemplateDsDao extends AbstractDsDao {

	public static final String TEMPLATE_KIND = "TemplateDs";
	public static final String TEMPLATE_NAME_FIELD = "templateName";
	public static final String TEMPLATE_TEXT_FIELD = "templateText";
	public static final String TEMPLATE_MEMCACHE = "template";
	private static final Object ALL_TEMPLATES = "allTemplates";
	private boolean dirty = true;

	@Override
	protected String getMemcacheServiceName() {
		return TEMPLATE_MEMCACHE;
	}

	public List<GwtTemplate> getTemplates() {
		List<GwtTemplate> templates;

		if (dirty) {
			templates = updateCacheFromDatastore();
		} else {
			templates = (List<GwtTemplate>) getCache().get(ALL_TEMPLATES);
			if (templates == null) {
				templates = updateCacheFromDatastore();
			}
		}
		return templates;
	}

	public GwtTemplate getTemplate(String id) {
		return toGwt(getCachedEntity(toKey(id)));
	}

	public void storeTemplate(GwtTemplate template) {
		Entity entity = toEntity(template);
		getDatastoreService().put(entity);
		insertIntoCache(entity);
		dirty = true;
	}

	public void deleteTemplate(GwtTemplate template) {
		deleteEntity(toKey(template.getKey()));
		dirty = true;
	}

	private List<GwtTemplate> updateCacheFromDatastore() {
		List<GwtTemplate> allTemplates = getAllTemplatesFromDatastore();
		getCache().put(ALL_TEMPLATES, allTemplates);
		dirty = false;
		return allTemplates;
	}

	private List<GwtTemplate> getAllTemplatesFromDatastore() {
		final Query query = new Query(TEMPLATE_KIND)
				.addSort(TEMPLATE_NAME_FIELD);
		return mapToGwtTemplates(execute(query, withDefaults()));
	}

	private List<GwtTemplate> mapToGwtTemplates(Iterable<Entity> resultList) {
		final List<GwtTemplate> result = new ArrayList<GwtTemplate>();
		for (Entity template : resultList) {
			result.add(toGwt(template));
		}
		return result;
	}

	private Entity toEntity(GwtTemplate gwtTemplate) {
		final String key = gwtTemplate.getKey();
		final Entity template;
		if (key == null) {
			template = new Entity(TEMPLATE_KIND);
		} else {
			template = new Entity(toKey(key));
		}
		template.setProperty(TEMPLATE_NAME_FIELD, gwtTemplate.getName());
		template.setProperty(TEMPLATE_TEXT_FIELD,
				new Text(gwtTemplate.getTemplate()));
		return template;
	}

	private GwtTemplate toGwt(Entity entity) {
		String name = (String) entity.getProperty(TEMPLATE_NAME_FIELD);
		Text text = (Text) entity.getProperty(TEMPLATE_TEXT_FIELD);

		GwtTemplate template = new GwtTemplate();
		template.setName(name);
		template.setTemplate(text.getValue());
		template.setId(toString(entity.getKey()));
		return template;
	}

}
