package at.lws.wnm.server.service;

import at.lws.wnm.client.service.WahrnehmungsService;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.server.dao.ds.AuthorizationDsDao;
import at.lws.wnm.server.dao.ds.BeobachtungDsDao;
import at.lws.wnm.shared.model.Authorization;
import at.lws.wnm.shared.model.BeobachtungsFilter;
import at.lws.wnm.shared.model.BeobachtungsResult;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.Utils;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

/**
 * The server side implementation of the RPC service.
 */
public class WahrnehmungsServiceImpl extends RemoteServiceServlet implements
		WahrnehmungsService {

	private static final String UPLOAD_URL = "/wahrnehmung/upload";
	private static final long serialVersionUID = 6513086238987365801L;
	private final BlobstoreService blobstoreService;
	private final UploadOptions options;
	private final BeobachtungDsDao beobachtungsDao;
	private final UserService userService;
	private final AuthorizationDsDao authorizationDao;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
		userService = UserServiceFactory.getUserService();
		options = UploadOptions.Builder
				.withGoogleStorageBucketName(Utils.GS_BUCKET_NAME);
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		final User currentUser = userService.getCurrentUser();
		beobachtungsDao.storeBeobachtung(beobachtung, currentUser, null);
		final String masterBeobachtungsKey = beobachtung.getKey();
		for (String additionalChildKey : beobachtung.getAdditionalChildKeys()) {
			beobachtung.setKey(null);
			beobachtung.setChildKey(additionalChildKey);
			beobachtungsDao.storeBeobachtung(beobachtung, currentUser,
					masterBeobachtungsKey);
		}
	}

	@Override
	public GwtBeobachtung getBeobachtung(String beobachtungsKey) {
		return beobachtungsDao.getBeobachtung(beobachtungsKey);
	}

	@Override
	public BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter,
			Range range) {
		final User user = getUserForQuery();
		final BeobachtungsResult result = new BeobachtungsResult();
		if (filter.getChildKey() != null) {
			result.setBeobachtungen(beobachtungsDao.getBeobachtungen(filter,
					range, user, true));
			result.setRowCount(beobachtungsDao.getRowCount(filter, user, true));
		}

		return result;
	}

	private User getUserForQuery() {
		final User currentUser = userService.getCurrentUser();
		final Authorization authorization = authorizationDao
				.getAuthorization(currentUser);
		final User user = authorization.isSeeAll() ? null : currentUser;
		return user;
	}

	@Override
	public void deleteBeobachtung(String beobachtungsKey) {
		beobachtungsDao.deleteBeobachtung(beobachtungsKey);

	}

	@Override
	public String getFileUploadUrl() {
		return blobstoreService.createUploadUrl(UPLOAD_URL, options);
	}

}
