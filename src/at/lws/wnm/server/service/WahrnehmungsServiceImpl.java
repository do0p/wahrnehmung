package at.lws.wnm.server.service;

import at.lws.wnm.client.WahrnehmungsService;
import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.shared.model.GwtBeobachtung;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class WahrnehmungsServiceImpl extends RemoteServiceServlet implements
		WahrnehmungsService {
	

	private BeobachtungDao beobachtungsDao;

	public WahrnehmungsServiceImpl()
	{
		beobachtungsDao = new BeobachtungDao();
	}
	

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		beobachtungsDao.storeBeobachtung(beobachtung);
		
	}




}
