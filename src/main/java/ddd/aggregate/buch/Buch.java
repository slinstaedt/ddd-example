package ddd.aggregate.buch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ddd.aggregate.AggregateRoot;
import ddd.aggregate.EventHandler;
import ddd.aggregate.Identifier;
import ddd.aggregate.buch.BuchEvent.BuchAusgeliehen;
import ddd.aggregate.buch.BuchEvent.BuchImBestandAufgenommen;
import ddd.aggregate.buch.BuchEvent.BuchZurückGegeben;

public class Buch extends AggregateRoot<Buch, BuchEvent> {

	public static Buch nehmImBestandAuf(String titel) {
		Buch buch = new Buch();
		buch.record(new BuchImBestandAufgenommen(titel));
		return buch;
	}

	private List<BuchEvent> eventHistory = new ArrayList<>();
	private boolean kannAusgeliehenWerden = true;
	private String titel;

	public Buch() {
		super(Identifier.create(BuchEvent.class),
				EventHandler.<Buch> unhandled().when(BuchImBestandAufgenommen.class, Buch::wurdeInBestandAufgenommen)
						.when(BuchAusgeliehen.class, Buch::wurdeAusgeliehen)
						.when(BuchZurückGegeben.class, Buch::wurdeZurückgegeben));
		setState(this);
	}
	
	void wurdeInBestandAufgenommen(BuchImBestandAufgenommen event) {
		eventHistory.add(event);
		titel = event.getTitel();
		kannAusgeliehenWerden = true;
	}

	public void leiheAus(String mitglied, LocalDate localDate) {
		if (kannAusgeliehenWerden()) {
			record(new BuchAusgeliehen(mitglied, localDate));
		} else {
			throw new IllegalStateException("Buch wurde bereits ausgeliehen");
		}
	}

	public void wurdeAusgeliehen(BuchAusgeliehen event) {
		eventHistory.add(event);
		kannAusgeliehenWerden = false;
	}

	public void gebeZurück(LocalDate rückgabeDatum) {
		if (!kannAusgeliehenWerden) {
			record(new BuchZurückGegeben(rückgabeDatum));
		} else {
			throw new IllegalStateException("Buch ist nicht ausgeliehen.");
		}
	}

	public void wurdeZurückgegeben(BuchZurückGegeben event) {
		eventHistory.add(event);
		kannAusgeliehenWerden = true;
	}

	private boolean kannAusgeliehenWerden() {
		return kannAusgeliehenWerden;
	}

}
