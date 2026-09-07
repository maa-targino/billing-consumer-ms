package com.billing.consumer.adapter.in.messaging;

import com.billing.consumer.adapter.in.messaging.dto.CustomerPayload;
import com.billing.consumer.adapter.in.messaging.dto.InvoiceMessage;
import com.billing.consumer.adapter.in.messaging.dto.LineItemPayload;
import com.billing.consumer.adapter.in.messaging.dto.ProductPayload;
import com.billing.consumer.application.port.in.IngestInvoiceCommand;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.CustomerData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.LineItemData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.ProductData;
import org.springframework.stereotype.Component;

/** Traduz o payload validado da fila para o comando do caso de uso. */
@Component
class InvoiceMessageMapper {

	IngestInvoiceCommand toCommand(InvoiceMessage message) {
		return new IngestInvoiceCommand(
				message.invoiceNumber(),
				message.issueDate(),
				message.dueDate(),
				message.status(),
				message.notes(),
				toCustomerData(message.customer()),
				message.lineItems().stream().map(this::toLineItemData).toList());
	}

	private CustomerData toCustomerData(CustomerPayload payload) {
		return new CustomerData(payload.companyName(), payload.contactName(), payload.email(),
				payload.billingAddress(), payload.shippingAddress());
	}

	private ProductData toProductData(ProductPayload payload) {
		return new ProductData(payload.name(), payload.description(), payload.unitPrice(),
				payload.taxRate());
	}

	private LineItemData toLineItemData(LineItemPayload payload) {
		return new LineItemData(toProductData(payload.product()), payload.quantity(),
				payload.historicUnitPrice(), payload.discountAmount(), payload.taxAmount(),
				payload.lineTotal());
	}

}
