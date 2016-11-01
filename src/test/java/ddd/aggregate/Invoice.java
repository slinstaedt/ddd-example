package ddd.aggregate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ddd.aggregate.InvoiceEvent.InvoiceCreated;
import ddd.aggregate.InvoiceEvent.InvoiceItemAdded;
import ddd.aggregate.InvoiceEvent.InvoiceRecipientChanged;
import ddd.aggregate.InvoiceEvent.InvoiceSent;

public class Invoice {

	public static final EventHandler<Invoice> HANDLER = EventHandler.<Invoice>unhandled()
			.when(InvoiceCreated.class, Invoice::new)
			.when(InvoiceRecipientChanged.class, Invoice::recipientChanged)
			.when(InvoiceItemAdded.class, Invoice::itemAdded)
			.when(InvoiceSent.class, Invoice::sent);

	public static InvoiceCreated create() {
		return new InvoiceCreated();
	}

	private int nextItemId;
	private String recipient;
	private final List<InvoiceItem> items;
	private LocalDate sentOn;
	private LocalDate paymentDueOn;

	public Invoice(InvoiceCreated event) {
		items = new ArrayList<>();
	}

	boolean readyToSend() {
		return recipient != null && !items.isEmpty();
	}

	long totalAmount() {
		return items.stream().collect(Collectors.summingLong(InvoiceItem::getAmount));
	}

	public InvoiceRecipientChanged changeRecipient(String recipient) {
		return new InvoiceRecipientChanged(recipient, readyToSend());
	}

	public InvoiceItemAdded addItem(String description, long amount) {
		return new InvoiceItemAdded(new InvoiceItem(nextItemId, description, amount), totalAmount() + amount, readyToSend());
	}

	public InvoiceSent send(LocalDate sentOn) {
		if (readyToSend()) {
			return new InvoiceSent(sentOn, sentOn.plusDays(14));
		} else {
			throw new IllegalStateException("not ready to send");
		}
	}

	void recipientChanged(InvoiceRecipientChanged event) {
		recipient = Objects.requireNonNull(event.getRecipient());
	}

	void itemAdded(InvoiceItemAdded event) {
		items.add(event.getItem());
	}

	void sent(InvoiceSent event) {
		sentOn = event.getSentOn();
		paymentDueOn = event.getPaymentDueOn();
	}
}