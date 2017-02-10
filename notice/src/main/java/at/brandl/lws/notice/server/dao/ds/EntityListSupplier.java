package at.brandl.lws.notice.server.dao.ds;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class EntityListSupplier<T extends Comparable<T>> implements Supplier<List<T>> {

	private final Query query;
	private final Function<Entity, T> entityConverter;

	public EntityListSupplier(Query query, Function<Entity, T> entityConverter) {
		this.entityConverter = entityConverter;
		this.query = query;
	}

	@Override
	public List<T> get() {
		List<T> result = new ArrayList<T>();
		Iterable<Entity> entities = DatastoreServiceFactory.getDatastoreService().prepare(query)
				.asIterable(withDefaults());
		for (Entity entity : entities) {
			result.add(entityConverter.apply(entity));
		}
		Collections.sort(result);
		return result;
	}
}