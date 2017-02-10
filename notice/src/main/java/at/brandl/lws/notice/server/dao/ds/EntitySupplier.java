package at.brandl.lws.notice.server.dao.ds;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class EntitySupplier<T> implements Supplier<T> {

		private final Key key;
		private final Function<Entity, T> entityConverter;

		public EntitySupplier(Key key, Function<Entity, T> entityConverter) {
			this.entityConverter = entityConverter;
			this.key = key;
		}

		@Override
		public T get() {
			try {
				Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
				return entityConverter.apply(entity);
			} catch (EntityNotFoundException e) {
				return null;
			}
		}
	}