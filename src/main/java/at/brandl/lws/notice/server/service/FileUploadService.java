package at.brandl.lws.notice.server.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

public class FileUploadService extends HttpServlet {

	private static final long serialVersionUID = 2354405300918894928L;
	private BlobstoreService blobstoreService = BlobstoreServiceFactory
			.getBlobstoreService();
	private ImagesService imageService = ImagesServiceFactory
			.getImagesService();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Map<String, List<FileInfo>> blobs = blobstoreService.getFileInfos(req);
		resp.setHeader("Content-Type", "text/html");
		PrintWriter writer = resp.getWriter();
		for (List<FileInfo> files : blobs.values()) {
			for (FileInfo file : files) {

				String filename = file.getFilename();
				String contentType = file.getContentType();
				String gsObjectName = file.getGsObjectName();
				String imageUrl = "-";
				System.out.println(gsObjectName);
				if (isImage(contentType)) {
					BlobKey blobKey = blobstoreService
							.createGsBlobKey(gsObjectName);
					ServingUrlOptions options = ServingUrlOptions.Builder
							.withBlobKey(blobKey);
					imageUrl = imageService.getServingUrl(options);
				}
				System.out.println(imageUrl);

				writer.print(filename);
				writer.print("::");
				writer.print(gsObjectName);
				writer.print("::");
				writer.print(contentType);
				writer.print("::");
				writer.println(imageUrl);
			}
		}
	}

	private boolean isImage(String contentType) {
		return contentType.toLowerCase().startsWith("image");
	}
}
