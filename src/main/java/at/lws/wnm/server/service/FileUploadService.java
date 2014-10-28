package at.lws.wnm.server.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;

public class FileUploadService extends HttpServlet {
	private static final long serialVersionUID = 2354405300918894928L;
	private BlobstoreService blobstoreService;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		Map<String, List<FileInfo>> blobs = blobstoreService.getFileInfos(req);

		resp.setHeader("Content-Type", "text/html");
		PrintWriter writer = resp.getWriter();
		for (List<FileInfo> files : blobs.values()) {
			for (FileInfo file : files) {
				writer.print(file.getFilename());
				writer.print(":");
				writer.println(file.getGsObjectName());
				writer.print(":");
				writer.println(file.getContentType());
			}
		}
	}

}
