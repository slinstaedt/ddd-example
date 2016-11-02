package ddd.aggregate.buch;

import java.time.LocalDate;

import ddd.aggregate.EventStore;
import ddd.aggregate.FileBasedEventStore;

public class Bibliothek {

	
	public static void main(String[] args) {
		Bibliothek bibliothek = new Bibliothek();
		bibliothek.anlegen();
	}

	private void anlegen() {
		String donald = "Donald Duck";
		String jim = "Jim Knopf";
		EventStore eventStore  = new FileBasedEventStore();
		
		Buch buch1 = Buch.nehmImBestandAuf("Henriette Bimmelbahn");
		buch1.leiheAus(donald, LocalDate.now().minusDays(5));
		buch1.gebeZurück(LocalDate.now().minusDays(4));
		buch1.leiheAus(jim, LocalDate.now().minusDays(2));
		buch1.tryCommit(eventStore::commit);
		
		Buch buch2 = Buch.nehmImBestandAuf("Event Sourcing + DDD");
		buch2.leiheAus(donald, LocalDate.now().minusDays(5));
		buch2.gebeZurück(LocalDate.now().minusDays(4));
		buch2.leiheAus(donald, LocalDate.now().minusDays(2));
		buch2.gebeZurück(LocalDate.now().minusDays(1));
		buch2.tryCommit(eventStore::commit);
		
		Buch buch3 = Buch.nehmImBestandAuf("Der Prozess");
		buch3.leiheAus(jim, LocalDate.now().minusDays(7));
		buch3.gebeZurück(LocalDate.now().minusDays(5));
		buch3.leiheAus(donald, LocalDate.now().minusDays(2));
		buch3.tryCommit(eventStore::commit);
		
		// am Ende: 2 Bücher ausgeliehen
		
		// vor 6 Tagen: 1 Buch ausgeliehen 
	}
	
	
}
