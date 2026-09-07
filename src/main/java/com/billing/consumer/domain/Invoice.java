package com.billing.consumer.domain;

import java.time.LocalDate;
import java.util.List;

/** Fatura. Objeto de dominio imutavel, sem dependencia de JPA/Spring. */
public class Invoice {

	private final Integer invoiceId;
	private final String invoiceNumber;
	private final Customer customer;
	private final LocalDate issueDate;
	private final LocalDate dueDate;

	/**
	 * Mantido como String, nao enum: os valores vem de um agente de IA e um enum
	 * falharia diante de qualquer valor inesperado.
	 */
	private final String status;

	private final String notes;
	private final List<InvoiceLineItem> lineItems;

	/** Fatura ainda nao persistida. */
	public Invoice(String invoiceNumber, Customer customer, LocalDate issueDate, LocalDate dueDate,
			String status, String notes, List<InvoiceLineItem> lineItems) {
		this(null, invoiceNumber, customer, issueDate, dueDate, status, notes, lineItems);
	}

	/** Reidratacao a partir da persistencia, com id ja atribuido. */
	public Invoice(Integer invoiceId, String invoiceNumber, Customer customer, LocalDate issueDate,
			LocalDate dueDate, String status, String notes, List<InvoiceLineItem> lineItems) {
		this.invoiceId = invoiceId;
		this.invoiceNumber = invoiceNumber;
		this.customer = customer;
		this.issueDate = issueDate;
		this.dueDate = dueDate;
		this.status = status;
		this.notes = notes;
		this.lineItems = List.copyOf(lineItems);
	}

	public Integer getInvoiceId() {
		return invoiceId;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public Customer getCustomer() {
		return customer;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public String getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public List<InvoiceLineItem> getLineItems() {
		return lineItems;
	}

}
