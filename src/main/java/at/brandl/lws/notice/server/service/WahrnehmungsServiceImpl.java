package at.brandl.lws.notice.server.service;

import java.util.List;

import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.AuthorizationDsDao;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.FileDsDao;
import at.brandl.lws.notice.shared.Utils;
import at.brandl.lws.notice.shared.model.Authorization;
import at.brandl.lws.notice.shared.model.BeobachtungsFilter;
import at.brandl.lws.notice.shared.model.BeobachtungsResult;
import at.brandl.lws.notice.shared.model.GwtBeobachtung;
import at.brandl.lws.notice.shared.model.GwtFileInfo;
import at.brandl.lws.notice.shared.service.WahrnehmungsService;
import at.brandl.lws.notice.shared.validator.GwtBeobachtungValidator;

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
	private final FileDsDao fileDao;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		authorizationDao = DaoRegistry.get(AuthorizationDsDao.class);
		fileDao = DaoRegistry.get(FileDsDao.class);
		userService = UserServiceFactory.getUserService();
		options = UploadOptions.Builder
				.withGoogleStorageBucketName(Utils.GS_BUCKET_NAME);
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		if (!GwtBeobachtungValidator.valid(beobachtung)) {
			throw new IllegalArgumentException("incomplete beobachtung");
		}
		final User currentUser = userService.getCurrentUser();
		storeBeobachtung(beobachtung, currentUser, null);
		final String masterBeobachtungsKey = beobachtung.getKey();
		for (String additionalChildKey : beobachtung.getAdditionalChildKeys()) {
			beobachtung.setKey(null);
			beobachtung.setChildKey(additionalChildKey);
			storeBeobachtung(beobachtung, currentUser, masterBeobachtungsKey);
		}
	}

	@Override
	public GwtBeobachtung getBeobachtung(String beobachtungsKey) {
		GwtBeobachtung beobachtung;
		try {
			beobachtung = beobachtungsDao
					.getBeobachtung(beobachtungsKey, false);
		} catch (IllegalArgumentException e) {
			beobachtung = beobachtungsDao.getBeobachtung(beobachtungsKey, true);
		}
		addFiles(beobachtung);
		return beobachtung;
	}

	@Override
	public BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter,
			Range range) {
		final User user = getUserForQuery();
		final BeobachtungsResult result = new BeobachtungsResult();

		if (filter.getChildKey() != null || filter.isOver12()
				|| filter.isUnder12()) {

			if (user != null) {
				filter.setUser(user.getEmail());
			}
			result.setBeobachtungen(beobachtungsDao.getBeobachtungen(filter,
					range));
			result.setRowCount(beobachtungsDao.getRowCount(filter));

			addFilenames(result);
		}
		return result;
	}

	@Override
	public void deleteBeobachtung(String beobachtungsKey) {
		beobachtungsDao.deleteBeobachtung(beobachtungsKey);
		fileDao.deleteFiles(beobachtungsKey);
	}

	@Override
	public String getFileUploadUrl() {
		String uploadUrl = blobstoreService
				.createUploadUrl(UPLOAD_URL, options);
		return uploadUrl;
	}

	@Override
	public boolean fileExists(String filename) {
		return fileDao.fileExists(filename);
	}

	private User getUserForQuery() {
		final User currentUser = userService.getCurrentUser();
		final Authorization authorization = authorizationDao
				.getAuthorization(currentUser);
		final User user = authorization.isSeeAll() ? null : currentUser;
		return user;
	}

	private void addFilenames(BeobachtungsResult result) {
		for (GwtBeobachtung beobachtung : result.getBeobachtungen()) {
			addFiles(beobachtung);
		}
	}

	private void addFiles(GwtBeobachtung beobachtung) {
		String key = beobachtung.getKey();
		if (key != null) {
			List<GwtFileInfo> filenames = fileDao.getFileInfos(key);
			beobachtung.setFileInfos(filenames);
		}
	}

	private void storeBeobachtung(GwtBeobachtung beobachtung,
			final User currentUser, final String masterBeobachtungsKey) {
		beobachtungsDao.storeBeobachtung(beobachtung, currentUser,
				masterBeobachtungsKey);
		fileDao.storeFiles(beobachtung.getKey(), beobachtung.getFileInfos());
	}

}
