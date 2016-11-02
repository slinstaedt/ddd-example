package ddd.aggregate.buch;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import ddd.aggregate.buch.BuchEvent.BuchAusgeliehen;
import ddd.aggregate.buch.BuchEvent.BuchImBestandAufgenommen;
import ddd.aggregate.buch.BuchEvent.BuchZurückGegeben;

public class BuchTest {
	
	private Buch buch;
	
	@Before
	public void prepare() {
		buch = new Buch();
	}
	
	@Test
	public void sollBuchAusleihen() {
		// given
		String mitglied = "mitglied";
		LocalDate ausleihDatum = LocalDate.now();
		
		// when
		buch.leiheAus(mitglied, ausleihDatum);
		
		// then
        buch.testChangesContain(new BuchAusgeliehen(mitglied, ausleihDatum));
	}
	
	@Test(expected=IllegalStateException.class)
	public void verliehenesBuchKannNichtVerliehenWerden() {
		// given
		String mitglied = "mitglied";
		buch.leiheAus(mitglied, LocalDate.now());
		
		// when
		buch.leiheAus(mitglied, LocalDate.now());				

		// then
		// -> exception
	}
	
	@Test(expected=IllegalStateException.class)
	public void vorhandenesBuchKannNichtZurückgegebenWerden()  {
		//given
		LocalDate rückgabeDatum = LocalDate.now();
		//when
		buch.gebeZurück(rückgabeDatum);
		//then
		buch.testChangesContain(new BuchZurückGegeben(rückgabeDatum ));
	}
	
	@Test()
	public void gebeBuchZurück()  {
		//given
		LocalDate rückgabeDatum = LocalDate.now();
		buch.applyEvent(new BuchAusgeliehen("mitglied", LocalDate.now()));
		//when
		buch.gebeZurück(rückgabeDatum);
		//then
		buch.testChangesContain(new BuchZurückGegeben(rückgabeDatum));
	}
	
	@Test
	public void buchWirdImBestandAufgenommen() {
		// given
		String titel = "Titel";

		//when
		Buch buch = Buch.nehmImBestandAuf(titel);
		
		//then
		buch.testChangesContain(new BuchImBestandAufgenommen(titel));
	}
}
