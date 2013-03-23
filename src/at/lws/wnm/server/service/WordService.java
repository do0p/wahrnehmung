package at.lws.wnm.server.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;

import at.lws.wnm.server.dao.BeobachtungDao;
import at.lws.wnm.server.dao.DaoRegistry;
import at.lws.wnm.shared.model.GwtBeobachtung;
import at.lws.wnm.shared.model.Utils;

public class WordService {

	private BeobachtungDao beobachtungsDao;

	public WordService() {
		beobachtungsDao = DaoRegistry.get(BeobachtungDao.class);
	}

	public void createWordFile(Collection<Long> beobachtungsKeys, OutputStream os)
			throws JAXBException, Docx4JException {

		final List<GwtBeobachtung> beobachtungen = new ArrayList<GwtBeobachtung>();
		for (Long beobachtungsKey : beobachtungsKeys) {
			final GwtBeobachtung beobachtung = beobachtungsDao
					.getBeobachtung(beobachtungsKey);
			if (beobachtung != null) {
				beobachtungen.add(beobachtung);
			}
		}

		if (beobachtungen.isEmpty()) {
			return;
		}

		final String beobachtungenHtml = Utils.createPrintHtml(beobachtungen);

		final OpcPackage opcPackage = createWordFile(beobachtungenHtml);
		new SaveToZipFile(opcPackage).save(os);
	}

	OpcPackage createWordFile(final String beobachtungenHtml)
			throws InvalidFormatException, JAXBException, Docx4JException {
		final WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.createPackage();

		final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();

		wordMLPackage
				.getMainDocumentPart()
				.getContent()
				.addAll(XHTMLImporter.convert(beobachtungenHtml, "baseurl",
						wordMLPackage));
		
		return wordMLPackage;
		
		
	}

}
