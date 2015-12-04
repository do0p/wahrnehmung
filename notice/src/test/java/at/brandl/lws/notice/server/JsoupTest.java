package at.brandl.lws.notice.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

public class JsoupTest {

	private static final String TEXT = "PRIMARIA-SCHNEEWOCHE in KLAFFER am Hochficht OÖ<div><br></div><div>26.-31.1.<br><br><br>, Vanja Roehle, Felix Gaisrucker, Jakob Fromhund, Paul Ecker, Jelja Stängl, Míra Süss, Taria Bickel, Tamika Höllerer, Fiona Brandl, Luka Steirer, Carina Pilgerstorfer, David Köttstorfer, David Lerch, Kilian Mayr, Florian Sonntag, Benjamin Sonntag, Benjamin Köttsdorfer, Serafina Engelhart<div><br></div><div><gs id=\"2367a6ca-33ca-48ba-b33f-8c3f3d7ec3d8\" ginger_software_uiphraseguid=\"d8fa65d1-69a8-48d6-8714-68b4ddecfcd4\" class=\"GINGER_SOFTWARE_mark\">begleitet</gs> von FLO, VALENTINA, DAVID</div></div><div><br></div><div>* Serafina springt&nbsp;knapp&nbsp;vor&nbsp;Abreise ab,&nbsp;mag nicht&nbsp;mitfahren</div><div>* Paul will &amp; hat eigenes&nbsp;Zimmer, von sozialer&nbsp;Situation u Dynamik gestresst,&nbsp;sehr ruhebedürftig und&nbsp;gereizt,&nbsp;Mittwoch ist \"Tiefpunkt\", packt bereits den Koffer, will heim. <gs id=\"0872cfbf-43a2-46ae-b49c-26759bfe36ee\" ginger_software_uiphraseguid=\"31dbe623-73fe-45c2-93eb-186768eca85a\" class=\"GINGER_SOFTWARE_mark\">Intensiver</gs> Prozess, <gs id=\"a8c531da-4dc5-471b-83c1-8ef7bc5b7c4e\" ginger_software_uiphraseguid=\"31dbe623-73fe-45c2-93eb-186768eca85a\" class=\"GINGER_SOFTWARE_mark\">bleibt</gs>. <gs id=\"d22708a4-4c21-4000-a873-293bf6a10b74\" ginger_software_uiphraseguid=\"cea35dbd-ad15-45a1-8572-1d0e492e31c3\" class=\"GINGER_SOFTWARE_mark\">Kopfweh</gs>, Müde. Abends beim Nachtschilauf hat Paul in der&nbsp;Hütte Panikattacke -&nbsp;hyperventiliert&nbsp;(\"ich bekomm&nbsp;keine&nbsp;Luft\", \"ich seh&nbsp;nichts&nbsp;mehr\")&nbsp;- Paul kann sich selbst nichtmehr beruhigen, Rettung wird verständigt, Notarzt bringt ihn ins Krankenhaus Rohrbach, dort wird hohes Fieber festgestellt - sonst physisch nichts, Paul entspannt sich, verbringt Nacht im KH - wird von Eltern abgeholt.</div><div>* Vanja organisiert 2x Sauna, nachdem erster Abend eskaliert (keine Zeiten ausgemacht,&nbsp;es&nbsp;gibt&nbsp;Uneinigkeit zwischen den Gruppen, Buben-Mädchen)</div><div>* Carina fährt 2x mit&nbsp;auf die Piste - Mittwochs beim Nachtschifahren kommt sie (trotz eines Sturzes) voll auf&nbsp;den Geschmack und&nbsp;fährt bis zur letzten Fahrt (zwischen Davids Beinen)</div>";

	@Test
	public void testParser() {

		String clean = Jsoup.clean(TEXT, Whitelist.relaxed());
		Document document = Jsoup.parseBodyFragment(clean);
		Element body = document.body();
		System.out.println(String.format("Berichte %d/%d", 2015, 2015+1));

	}

	
}
