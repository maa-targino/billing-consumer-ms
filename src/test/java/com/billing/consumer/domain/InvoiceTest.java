package com.billing.consumer.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

	private final Customer customer = new Customer("Acme", "Jane Doe", "jane@acme.com", null, null);
	private final Product product = new Product("Consulting", null, BigDecimal.TEN, BigDecimal.ONE);

	@Test
	void newInvoiceHasNoIdUntilPersisted() {
		Invoice invoice = new Invoice("INV-001", customer, LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, List.of(lineItem()));

		assertThat(invoice.getInvoiceId()).isNull();
		assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-001");
	}

	@Test
	void rehydratedInvoiceKeepsAssignedId() {
		Invoice invoice = new Invoice(42, "INV-001", customer, LocalDate.now(),
				LocalDate.now().plusDays(30), "PAID", null, List.of(lineItem()));

		assertThat(invoice.getInvoiceId()).isEqualTo(42);
	}

	@Test
	void lineItemsListIsImmutable() {
		Invoice invoice = new Invoice("INV-001", customer, LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, List.of(lineItem()));

		assertThatThrownBy(() -> invoice.getLineItems().add(lineItem()))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private InvoiceLineItem lineItem() {
		return new InvoiceLineItem(product, BigDecimal.ONE, BigDecimal.TEN, null, null, BigDecimal.TEN);
	}

}
