package ddd.aggregate;

import org.junit.Assert;
import org.junit.Test;

import ddd.aggregate.InvoiceEvent.InvoiceCreated;
import ddd.aggregate.InvoiceEvent.InvoiceRecipientChanged;
import ddd.aggregate.InvoiceEvent.UnusedEvent;

public class InvoiceTest {

	@Test
	public void creation() {
		Invoice result = Invoice.HANDLER.apply(null, new InvoiceCreated());

		Assert.assertNotNull(result);
		Assert.assertFalse(result.readyToSend());
		Assert.assertEquals(0, result.totalAmount());
	}

	@Test(expected = IllegalArgumentException.class)
	public void creationFail() {
		Invoice.HANDLER.apply(new Invoice(new InvoiceCreated()), new InvoiceCreated());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unhandledEvent() {
		Invoice.HANDLER.apply(new Invoice(new InvoiceCreated()), new UnusedEvent());
	}

	@Test
	public void changeRecipient() {
		AggregateRoot<Invoice, InvoiceEvent> aggregate = new AggregateRoot<>(Identifier.create(InvoiceEvent.class), Invoice.HANDLER);
		aggregate.applyEvent(new InvoiceCreated());
		aggregate.perform(i -> i.changeRecipient("xxx")).perform(i -> i.changeRecipient("yyy"));

		aggregate.testState(i -> !i.readyToSend());
		aggregate.testState(i -> 0 == i.totalAmount());
		aggregate.testChangesNotContain(new InvoiceCreated());
		aggregate.testChangesContain(new InvoiceRecipientChanged("xxx", false)).testChangesContain(new InvoiceRecipientChanged("yyy", false));
	}
}
