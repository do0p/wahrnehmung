package at.lws.wnm.server.service;

import java.util.List;

import at.lws.wnm.client.ChildService;
import at.lws.wnm.server.dao.ChildDao;
import at.lws.wnm.shared.model.Child;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ChildServiceImpl extends RemoteServiceServlet implements ChildService {

	private final ChildDao childDao;
	
	public ChildServiceImpl()
	{
		childDao = new ChildDao();
	}
	
	@Override
	public List<Child> queryChildren() {
		
		return childDao.getAllChildren();
		
	}

}
