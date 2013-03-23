package at.lws.wnm.server.service;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

public class WordServiceTest {

	private static final String TEST_HTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><div><table border=\"0\" style=\"border-style:none;\"><tbody border=\"0\" style=\"border-style:none;\"><tr><td colspan=\"2\"><b>Fiona Brandl</b></td></tr><tr><td>Datum:</td><td>23.2.13</td></tr><tr><td>Bereich:</td><td>Sprachbereich</td></tr><tr><td>Dauer:</td><td>mittel</td></tr><tr><td>Sozialform:</td><td>zu zweit</td></tr><tr><td>Begleiter:</td><td>dbrandl72@gmail.com</td></tr><tr><td colspan=\"2\"><br/><b>Hallo<br/></b><i><br/>sowas</i><b> <u>aber</u></b> auch<br/><br/><ul><li>liste1</li><li>liste2</li></ul><ol><li>ziffer 1</li><li>ziffer 2</li></ol><p align=\"left\">rechts zentriert</p><p align=\"center\">mittig</p><p align=\"right\">links</p><p align=\"left\"><font face=\"Courier New\">andere schrift</font></p><p align=\"left\"><font face=\"Arial\">andere größe</font></p><p align=\"left\"><font face=\"Arial\">a<font color=\"red\">ndere farbe</font></font><br/></p></td></tr></tbody></table><hr/></div></html>";

	@Test
	public void createWordFile() throws JAXBException, Docx4JException {
		OpcPackage wordMLPackage = new WordService().createWordFile(TEST_HTML);
	

		wordMLPackage.save(new java.io.File(System.getProperty("user.dir")
				+ "/html_output.docx"));
	}
}
