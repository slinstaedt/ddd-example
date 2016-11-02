package ddd.aggregate.buch;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Value;
@SuppressWarnings("serial")
public interface BuchEvent extends Serializable {

	@Value
	class BuchImBestandAufgenommen implements BuchEvent{
		String titel;
	}
	
	@Value
	class BuchAusgeliehen implements BuchEvent {
		String mitglied;
		LocalDate ausleihDatum;
	}

	@Value
	class BuchZurückGegeben implements BuchEvent {
		LocalDate rückgabeDatum; 
	}

}
