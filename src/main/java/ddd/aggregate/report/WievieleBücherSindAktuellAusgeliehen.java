package ddd.aggregate.report;

import ddd.aggregate.EventStore;
import ddd.aggregate.FileBasedEventStore;
import ddd.aggregate.buch.BuchEvent;

public class WievieleBücherSindAktuellAusgeliehen {

	public static void main(String[] args) {
		EventStore eventStore = new FileBasedEventStore();
		AnzahlAusgeliehenerBücher report = new AnzahlAusgeliehenerBücher();
		eventStore.get(BuchEvent.class).events().forEach(report::verarbeiteEreignis);
		System.out.println(report.getAnzahl());
	}
}
