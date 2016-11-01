package ddd.aggregate;

import java.io.Serializable;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InvoiceItem implements Serializable {

	private static final long serialVersionUID = 1L;

	int id;
	@NonNull
	String description;
	long amount;
}
