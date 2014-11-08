package at.brandl.lws.notice.server.dao.ds;

import java.util.List;

import at.brandl.lws.notice.shared.model.GwtTemplate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

public class TemplateDsDao extends AbstractDsDao {

	public static final String SECTION_KIND = "TemplateDs";
	public static final String TEMPLATE_NAME_FIELD = "templateName";
	public static final String TEMPLATE_TEXT_FIELD = "templateText";
	public static final String TEMPLATE_MEMCACHE = "template";
	
	@Override
	protected String getMemcacheServiceName() {
		return TEMPLATE_MEMCACHE;
	}

	public List<GwtTemplate> getTemplates() {
		return null;
	}

	public GwtTemplate getTemplate(String id) {
		return toGwt(getCachedEntity(toKey(id)));
	}

	private Entity toEntity(GwtTemplate template) {
		return null;
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
