package com.billing.consumer.application.port.in;

/** Porta de entrada: ingestao de uma fatura recebida do batch producer. */
public interface IngestInvoiceUseCase {

	void ingest(IngestInvoiceCommand command);

}
