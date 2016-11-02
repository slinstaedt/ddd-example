package ddd.aggregate.report;

import java.time.LocalDate;

import ddd.aggregate.EventHandler;
import ddd.aggregate.buch.BuchEvent;
import ddd.aggregate.buch.BuchEvent.*;

public class AnzahlAusgeliehenerBücher {

	public static final EventHandler<AnzahlAusgeliehenerBücher> HANDLER = EventHandler.<AnzahlAusgeliehenerBücher>ignoreUnhandled()
			.when(BuchAusgeliehen.class, AnzahlAusgeliehenerBücher::registriereAusgeliehenesBuch)
			.when(BuchZurückGegeben.class, AnzahlAusgeliehenerBücher::registriereZurückgegebenesBuch);
	
	private LocalDate stichTag;

	public AnzahlAusgeliehenerBücher() {
		this(LocalDate.now());
	}
	
	public AnzahlAusgeliehenerBücher(LocalDate stichTag) {
		this.stichTag = stichTag;
	}

	private int anzahl = 0;
	
	public void verarbeiteEreignis(BuchEvent event) {
		HANDLER.apply(this, event);
	}

	void registriereAusgeliehenesBuch(BuchAusgeliehen ausgeliehen) {
		if (!ausgeliehen.getAusleihDatum().isAfter(stichTag)) {
			anzahl++;
		}
	}

	void registriereZurückgegebenesBuch(BuchZurückGegeben zurückGegeben) {
		if (!zurückGegeben.getRückgabeDatum().isAfter(stichTag)) {
		    anzahl--;
		}
	}
	
	public int getAnzahl() {
		return anzahl;
	}
}
