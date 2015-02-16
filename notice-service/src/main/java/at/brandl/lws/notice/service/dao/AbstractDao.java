package at.brandl.lws.notice.service.dao;


public class AbstractDao {

	public AbstractDao() {
		DaoRegistry.register(this);
	}


}
