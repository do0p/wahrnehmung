package at.brandl.lws.notice.server.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.view.client.Range;

import at.brandl.lws.notice.dao.DaoRegistry;
import at.brandl.lws.notice.model.Authorization;
import at.brandl.lws.notice.model.BeobachtungsFilter;
import at.brandl.lws.notice.model.BeobachtungsResult;
import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtFileInfo;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;
import at.brandl.lws.notice.server.dao.ds.FileDsDao;
import at.brandl.lws.notice.shared.Config;
import at.brandl.lws.notice.shared.service.WahrnehmungsService;
import at.brandl.lws.notice.shared.validator.GwtBeobachtungValidator;

/**
 * The server side implementation of the RPC service.
 */
public class WahrnehmungsServiceImpl extends RemoteServiceServlet implements WahrnehmungsService {

	private static final String INTERACTION_SERVICE_URL = "/storeInteraction";
	private static final String INTERACTION_QUEUE_NAME = "interaction";
	private static final Logger LOGGER = Logger.getLogger(WahrnehmungsServiceImpl.class.getCanonicalName());
	private static final String UPLOAD_URL = "/wahrnehmung/upload";
	private static final long serialVersionUID = 6513086238987365801L;

	private final BlobstoreService blobstoreService;
	private final UploadOptions options;
	private final BeobachtungDsDao beobachtungsDao;
	private final UserService userService;
	private final AuthorizationServiceImpl authorizationService;
	private final FileDsDao fileDao;

	public WahrnehmungsServiceImpl() {
		beobachtungsDao = DaoRegistry.get(BeobachtungDsDao.class);
		authorizationService = new AuthorizationServiceImpl();
		fileDao = DaoRegistry.get(FileDsDao.class);
		userService = UserServiceFactory.getUserService();
		options = UploadOptions.Builder.withGoogleStorageBucketName(Config.getInstance().getBucketName());
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	}

	@Override
	public void storeBeobachtung(GwtBeobachtung beobachtung) {
		LOGGER.log(Level.FINE, "groupActivity: " + beobachtung.isGroupActivity());
		if (!GwtBeobachtungValidator.valid(beobachtung)) {
			throw new IllegalArgumentException("incomplete beobachtung");
		}
		final User currentUser = userService.getCurrentUser();

		if (beobachtung.isGroupActivity()) {
			registerInteractions(beobachtung);
		}

		storeBeobachtung(beobachtung, currentUser, null);
		final String masterBeobachtungsKey = beobachtung.getKey();
		for (String additionalChildKey : beobachtung.getAdditionalChildKeys()) {
			beobachtung.setKey(null);
			beobachtung.setChildKey(additionalChildKey);
			storeBeobachtung(beobachtung, currentUser, beobachtung.isGroupActivity() ? masterBeobachtungsKey : null);
		}
	}

	private void registerInteractions(GwtBeobachtung beobachtung) {
		final List<String> allChildKeys = new ArrayList<>();
		allChildKeys.add(beobachtung.getChildKey());
		allChildKeys.addAll(beobachtung.getAdditionalChildKeys());
		while (allChildKeys.size() > 1) {
			String child1 = allChildKeys.remove(0);
			for (String child2 : allChildKeys) {
				registerInteraction(child1, child2, beobachtung.getDate());
			}
		}
	}

	private void registerInteraction(String child1, String child2, Date date) {
		Queue queue = QueueFactory.getQueue(INTERACTION_QUEUE_NAME);
		queue.add(TaskOptions.Builder.withUrl(INTERACTION_SERVICE_URL).param("childKey", child1).param("childKey", child2).param("date",
			new	SimpleDateFormat("yyyy-MM-dd").format(date)));
	}

	@Override
	public GwtBeobachtung getBeobachtung(String beobachtungsKey) {
		GwtBeobachtung beobachtung;
		try {
			beobachtung = beobachtungsDao.getBeobachtung(beobachtungsKey, false);
		} catch (IllegalArgumentException e) {
			beobachtung = beobachtungsDao.getBeobachtung(beobachtungsKey, true);
		}
		addFiles(beobachtung);
		return beobachtung;
	}

	@Override
	public BeobachtungsResult getBeobachtungen(BeobachtungsFilter filter, Range range) {
		final User user = getUserForQuery();
		final BeobachtungsResult result = new BeobachtungsResult();

		if (filter.getChildKey() != null || filter.isOver12() || filter.isUnder12() || filter.getSectionKey() != null) {

			if (user != null) {
				filter.setUser(user.getEmail());
			}
			result.setBeobachtungen(beobachtungsDao.getBeobachtungen(filter, range));
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
		return blobstoreService.createUploadUrl(UPLOAD_URL, options);
	}

	@Override
	public boolean fileExists(String filename) {
		return fileDao.fileExists(filename);
	}

	private User getUserForQuery() {
		final User currentUser = userService.getCurrentUser();
		final Authorization authorization = authorizationService.getAuthorization(currentUser);
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

	private void storeBeobachtung(GwtBeobachtung beobachtung, final User currentUser,
			final String masterBeobachtungsKey) {
		beobachtungsDao.storeBeobachtung(beobachtung, currentUser, masterBeobachtungsKey);
		fileDao.storeFiles(beobachtung.getKey(), beobachtung.getFileInfos());
	}

}
