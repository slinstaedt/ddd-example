package ddd.aggregate.report;

import java.util.HashMap;
import java.util.Map;

import ddd.aggregate.EventStore;
import ddd.aggregate.FileBasedEventStore;
import ddd.aggregate.buch.BuchEvent;
import ddd.aggregate.buch.BuchEvent.BuchAusgeliehen;

public class WelcheBücherHatWelchesMitgliedAusgeliehen {

	private Map<String,Integer> ausleihenProMitglied = new HashMap<>();
	
	
	public static void main(String[] args) {
		EventStore eventStore = new FileBasedEventStore();
		WelcheBücherHatWelchesMitgliedAusgeliehen report = new  WelcheBücherHatWelchesMitgliedAusgeliehen();
		eventStore.get(BuchEvent.class).events().filter(BuchAusgeliehen.class::isInstance).map(BuchAusgeliehen.class::cast).forEach(report::registriereAusgeliehenesBuchFürMitglied);
		System.out.println(report.ausleihenProMitglied);
	}
	
	
	public void registriereAusgeliehenesBuchFürMitglied(BuchAusgeliehen ausgeliehen) {
		String mitglied = ausgeliehen.getMitglied();
		ausleihenProMitglied.compute(mitglied, (member,anzahl) -> anzahl != null ? anzahl +1: 1);
	}
}
