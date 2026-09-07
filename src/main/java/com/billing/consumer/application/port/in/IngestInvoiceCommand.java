package com.billing.consumer.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Comando de entrada do caso de uso, desacoplado do transporte (mensageria). O adapter de
 * entrada e responsavel por validar e montar este objeto a partir do payload recebido.
 */
public record IngestInvoiceCommand(

		String invoiceNumber,
		LocalDate issueDate,
		LocalDate dueDate,
		String status,
		String notes,
		CustomerData customer,
		List<LineItemData> lineItems

) {

	public record CustomerData(
			String companyName,
			String contactName,
			String email,
			String billingAddress,
			String shippingAddress) {
	}

	public record ProductData(
			String name,
			String description,
			BigDecimal unitPrice,
			BigDecimal taxRate) {
	}

	public record LineItemData(
			ProductData product,
			BigDecimal quantity,
			BigDecimal historicUnitPrice,
			BigDecimal discountAmount,
			BigDecimal taxAmount,
			BigDecimal lineTotal) {
	}

}
