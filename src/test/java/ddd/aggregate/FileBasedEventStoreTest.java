package ddd.aggregate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileBasedEventStoreTest {

	private final EventStore store = new FileBasedEventStore();

	private AggregateRoot<Invoice, InvoiceEvent> aggregate;

	@Before
	public void setup() {
		aggregate = new AggregateRoot<>(Identifier.create(InvoiceEvent.class), Invoice.HANDLER);
		aggregate.perform(i -> Invoice.create());
		aggregate.tryCommit(store::commit);
	}

	@Test
	public void commitToEventStore() {
		aggregate.perform(i -> i.changeRecipient("xxx")).tryCommit(store::commit);
		aggregate.perform(i -> i.changeRecipient("yyy")).tryCommit(store::commit);
		aggregate.testVersion(new Version(3));
	}

	@Test
	public void lastCommittedVersion() {
		Version lastCommittedVersion = store.get(aggregate.getIdentifier()).lastCommittedVersion();
		Assert.assertEquals(new Version(1), lastCommittedVersion);

		aggregate.perform(i -> i.changeRecipient("xxx")).tryCommit(store::commit);

		lastCommittedVersion = store.get(aggregate.getIdentifier()).lastCommittedVersion();
		Assert.assertEquals(new Version(2), lastCommittedVersion);
	}

	@Test
	public void readFromStore() {
		aggregate.perform(i -> i.changeRecipient("111")).perform(i -> i.changeRecipient("222")).tryCommit(store::commit);
		aggregate.perform(i -> i.changeRecipient("333")).tryCommit(store::commit);

		AggregateRoot<Invoice, InvoiceEvent> root = new AggregateRoot<>(aggregate.getIdentifier(), Invoice.HANDLER);
		root.updateFromHistory(store::events);

		aggregate.testVersion(new Version(3));
	}
}
