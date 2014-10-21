package at.lws.wnm.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.server.dao.ds.ChildDsDao;
import at.lws.wnm.shared.model.GwtChild;
import au.com.bytecode.opencsv.CSVReader;

public class CsvUploadService extends HttpServlet {
	private static final int NOT_SET = -1;
	private static final long serialVersionUID = 2354405300918894928L;

	private static final String FN_HEADER = "vorname";
	private static final String LN_HEADER = "famname";
	private static final String BD_HEADER = "gebdate";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			final FileItemIterator iter = new ServletFileUpload()
					.getItemIterator(req);

			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				InputStream stream = item.openStream();
				final CSVReader csvReader = new CSVReader(
						new InputStreamReader(stream), ';');
				final String[] firstLine = csvReader.readNext();
				int fnPos = NOT_SET;
				int lnPos = NOT_SET;
				int bdPos = NOT_SET;
				for (int i = 0; i < firstLine.length; i++) {
					final String header = firstLine[i];
					if (FN_HEADER.equals(header)) {
						fnPos = i;
					} else if (LN_HEADER.equals(header)) {
						lnPos = i;
					} else if (BD_HEADER.equals(header)) {
						bdPos = i;
					}
				}
				if (fnPos == NOT_SET || lnPos == NOT_SET || bdPos == NOT_SET) {
					csvReader.close();
					throw new IllegalArgumentException("missing fields");
				}

				String[] line = csvReader.readNext();
				final ChildDsDao childDao = DaoRegistry.get(ChildDsDao.class);
				final SimpleDateFormat dateFormat = new SimpleDateFormat(
						at.lws.wnm.shared.model.Utils.DATE_FORMAT_STRING);
				int inserted = 0;
				int duplicate = 0;
				while (line != null) {
					final GwtChild child = new GwtChild();
					child.setFirstName(line[fnPos]);
					child.setLastName(line[lnPos]);
					child.setBirthDay(dateFormat.parse(line[bdPos]));
					try {
						childDao.storeChild(child);
						inserted++;
					} catch (IllegalArgumentException e) {
						duplicate++;
					}
					line = csvReader.readNext();
				}
				resp.getOutputStream().print("" + inserted + " Kinder hinzugef�gt, " + duplicate + " waren schon angelegt.");
				csvReader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
}
