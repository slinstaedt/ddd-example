package ddd.aggregate.report;

import java.time.LocalDate;

import ddd.aggregate.EventStore;
import ddd.aggregate.FileBasedEventStore;
import ddd.aggregate.buch.BuchEvent;

public class WievieleBücherWarenVor6TagenAusgeliehen {

	public static void main(String[] args) {
		EventStore eventStore = new FileBasedEventStore();
		AnzahlAusgeliehenerBücher report = new AnzahlAusgeliehenerBücher(LocalDate.now().minusDays(6));
		eventStore.get(BuchEvent.class).events()
				.forEach(report::verarbeiteEreignis);
		System.out.println(report.getAnzahl());
	}
}
