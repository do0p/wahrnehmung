package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import at.brandl.lws.notice.model.GwtBeobachtung;
import at.brandl.lws.notice.model.GwtChild;
import at.brandl.lws.notice.model.GwtSection;
import at.brandl.lws.notice.server.dao.DaoRegistry;
import at.brandl.lws.notice.server.dao.ds.BeobachtungDsDao;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class DocsServiceImpl {

	private static final JacksonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();
	private static final List<String> SCOPES = Arrays
			.asList(DriveScopes.DRIVE_METADATA_READONLY);

	private static final NetHttpTransport NEW_TRUSTED_TRANSPORT;
	static {
		try {
			NEW_TRUSTED_TRANSPORT = GoogleNetHttpTransport
					.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final BeobachtungDsDao noticeDao;
	
	public DocsServiceImpl() {
		
		noticeDao = DaoRegistry.get(BeobachtungDsDao.class);
	}

	public List<String> printDocumentation(Collection<String> ids) {
		
		List<String> urls = new ArrayList<String>();
		Iterator<GwtBeobachtung> allNotices = fetchNotices(ids);
		Multimap<String, GwtBeobachtung> childNoticeMap = groupNoticesPerChild(allNotices);
		
		for(String childKey : childNoticeMap.keySet()) {
		
			File file = createDocument();
			urls.add(file.getAlternateLink());
			updateDocument(getChild(childKey), file);
			
			Collection<GwtBeobachtung> childNotices = childNoticeMap.get(childKey);
			Multimap<String, GwtBeobachtung> sectionNotices = groupNoticesPerSection(childNotices);
			
			for(String sectionKey : sectionNotices.keySet()) {
				
				Collection<GwtBeobachtung> notices = sectionNotices.get(sectionKey);
				updateDocument(getSection(sectionKey), notices);
			}
		}
		
		return urls;
	}


	private void updateDocument(GwtSection section,
			Collection<GwtBeobachtung> notices) {
		// TODO Auto-generated method stub
		
	}


	private GwtSection getSection(String sectionKey) {
		// TODO Auto-generated method stub
		return null;
	}


	private Multimap<String, GwtBeobachtung> groupNoticesPerSection(
			Collection<GwtBeobachtung> childNotices) {
		// TODO Auto-generated method stub
		return null;
	}


	private void updateDocument(GwtChild child, File file) {
		// TODO Auto-generated method stub
		
	}


	private GwtChild getChild(String childKey) {
		// TODO Auto-generated method stub
		return null;
	}


	private File createDocument() {
		// TODO Auto-generated method stub
		return null;
	}


	public Multimap<String, GwtBeobachtung> groupNoticesPerChild(
			Iterator<GwtBeobachtung> allNotices) {
		
//		Multimap<String, GwtBeobachtung> childNoticeMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
//		Spliterator<GwtBeobachtung> spliterator = Spliterators
//				.spliteratorUnknownSize(allNotices, Spliterator.CONCURRENT
//						& Spliterator.NONNULL & Spliterator.DISTINCT
//						& Spliterator.IMMUTABLE);
//		StreamSupport.<GwtBeobachtung>stream(spliterator , true).forEach( notice -> childNoticeMap.put(notice.getChildKey(), notice));
//		return childNoticeMap;
		return null;
	}
	
	
	private Iterator<GwtBeobachtung> fetchNotices(Collection<String> ids) {
		// TODO Auto-generated method stub
		return null;
	}


	public String uploadFile(File file) throws IOException {
		
		
		File newFile = new File();
		Drive drive = createDriveClient();

		AbstractInputStreamContent content = null;// new ByteArrayContent(type, array);
		file = drive.files().insert(file, content).execute();
		return file.getId();
	}
	
	private String listFiles() throws IOException {
		
		Drive drive = createDriveClient();

		FileList fileList = drive.files().list().execute();
		return fileList.toPrettyString();
	}

	private Drive createDriveClient() {
		HttpRequestInitializer httpRequestInitializer = new AppIdentityCredential(
				SCOPES);
		return new Drive.Builder(NEW_TRUSTED_TRANSPORT, JSON_FACTORY,
				httpRequestInitializer).build();
	}

}
