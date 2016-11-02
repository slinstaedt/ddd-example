package ddd.aggregate.report;

import java.util.stream.Stream;

import ddd.aggregate.EventStore;
import ddd.aggregate.EventStore.AggregateLog;
import ddd.aggregate.FileBasedEventStore;
import ddd.aggregate.buch.BuchEvent;

public class BuchÄnderungsHistorie {

	public static void main(String[] args) {
		EventStore eventStore = new FileBasedEventStore();
		Stream<AggregateLog<BuchEvent>> logs = eventStore.get(BuchEvent.class).logs();
		logs.flatMap(l->l.events()).forEach(System.out::println);
	}
}
