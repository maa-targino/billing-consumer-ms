package com.billing.consumer.application.port.out;

import com.billing.consumer.domain.Invoice;

/** Porta de saida para persistencia de faturas. */
public interface InvoiceRepositoryPort {

	boolean existsByInvoiceNumber(String invoiceNumber);

	Invoice save(Invoice invoice);

}
