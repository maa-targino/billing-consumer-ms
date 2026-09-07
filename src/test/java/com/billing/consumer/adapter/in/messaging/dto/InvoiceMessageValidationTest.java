package com.billing.consumer.adapter.in.messaging.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O batch producer publica payloads gerados por uma IA, entao a validacao de contrato aqui e a
 * ultima barreira antes da mensagem virar excecao no listener e ir parar na DLQ.
 */
class InvoiceMessageValidationTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	@Test
	void validMessageHasNoViolations() {
		assertThat(validator.validate(validMessage())).isEmpty();
	}

	@Test
	void rejectsBlankInvoiceNumber() {
		InvoiceMessage message = new InvoiceMessage("", LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, validCustomer(), List.of(validLineItem()));

		assertThat(violationPaths(message)).contains("invoiceNumber");
	}

	@Test
	void rejectsInvalidCustomerEmail() {
		CustomerPayload badCustomer = new CustomerPayload("Acme", "Jane", "not-an-email", null, null);
		InvoiceMessage message = new InvoiceMessage("INV-001", LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, badCustomer, List.of(validLineItem()));

		assertThat(violationPaths(message)).contains("customer.email");
	}

	@Test
	void rejectsEmptyLineItems() {
		InvoiceMessage message = new InvoiceMessage("INV-001", LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, validCustomer(), List.of());

		assertThat(violationPaths(message)).contains("lineItems");
	}

	private Set<String> violationPaths(InvoiceMessage message) {
		Set<ConstraintViolation<InvoiceMessage>> violations = validator.validate(message);
		return violations.stream().map(v -> v.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
	}

	private InvoiceMessage validMessage() {
		return new InvoiceMessage("INV-001", LocalDate.now(), LocalDate.now().plusDays(30), "PENDING", null,
				validCustomer(), List.of(validLineItem()));
	}

	private CustomerPayload validCustomer() {
		return new CustomerPayload("Acme", "Jane", "jane@acme.com", null, null);
	}

	private LineItemPayload validLineItem() {
		ProductPayload product = new ProductPayload("Consulting", null, BigDecimal.TEN, BigDecimal.ONE);
		return new LineItemPayload(product, BigDecimal.ONE, BigDecimal.TEN, null, null, BigDecimal.TEN);
	}

}
