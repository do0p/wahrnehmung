package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.AuthorizationService;
import at.lws.wnm.server.dao.AuthorizationDao;
import at.lws.wnm.shared.model.Authorization;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthorizationServiceImpl extends RemoteServiceServlet implements
		AuthorizationService {

	private static final long serialVersionUID = 3902038151789960035L;
	private final AuthorizationDao authorizationDao;

	public AuthorizationServiceImpl()
	{
		authorizationDao = new AuthorizationDao();
	}
	
	@Override
	public List<Authorization> queryAuthorizations() {

		return authorizationDao.queryAuthorizations();
	}

	@Override
	public void storeAuthorization(Authorization aut) {
		authorizationDao.storeAuthorization(aut);
	}

	@Override
	public void deleteAuthorization(String email) {
		authorizationDao.deleteAuthorization(email);
	}

}
