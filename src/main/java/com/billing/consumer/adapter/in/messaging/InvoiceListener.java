package com.billing.consumer.adapter.in.messaging;

import com.billing.consumer.adapter.in.messaging.dto.InvoiceMessage;
import com.billing.consumer.application.port.in.IngestInvoiceUseCase;
import com.billing.consumer.config.RabbitConfig;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceListener {

	private final IngestInvoiceUseCase ingestInvoiceUseCase;
	private final InvoiceMessageMapper mapper;

	public InvoiceListener(IngestInvoiceUseCase ingestInvoiceUseCase, InvoiceMessageMapper mapper) {
		this.ingestInvoiceUseCase = ingestInvoiceUseCase;
		this.mapper = mapper;
	}

	/**
	 * Sem try/catch: a excecao precisa propagar para o retry e a DLQ funcionarem.
	 */
	@RabbitListener(queues = RabbitConfig.QUEUE_IN)
	public void onInvoice(@Valid InvoiceMessage message) {
		ingestInvoiceUseCase.ingest(mapper.toCommand(message));
	}

}
