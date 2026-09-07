package com.billing.consumer.adapter.in.messaging;

import com.billing.consumer.adapter.in.messaging.dto.CustomerPayload;
import com.billing.consumer.adapter.in.messaging.dto.InvoiceMessage;
import com.billing.consumer.adapter.in.messaging.dto.LineItemPayload;
import com.billing.consumer.adapter.in.messaging.dto.ProductPayload;
import com.billing.consumer.application.port.in.IngestInvoiceCommand;
import com.billing.consumer.application.port.in.IngestInvoiceUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/** O listener e deliberadamente fino: so precisa delegar ao use case o comando mapeado. */
@ExtendWith(MockitoExtension.class)
class InvoiceListenerTest {

	@Mock
	private IngestInvoiceUseCase useCase;

	private final InvoiceMessageMapper mapper = new InvoiceMessageMapper();

	@Test
	void delegatesMappedCommandToUseCase() {
		InvoiceListener listener = new InvoiceListener(useCase, mapper);

		listener.onInvoice(message());

		ArgumentCaptor<IngestInvoiceCommand> captor = ArgumentCaptor.forClass(IngestInvoiceCommand.class);
		verify(useCase).ingest(captor.capture());
		assertThat(captor.getValue().invoiceNumber()).isEqualTo("INV-001");
		assertThat(captor.getValue().lineItems()).hasSize(1);
		assertThat(captor.getValue().customer().email()).isEqualTo("jane@acme.com");
	}

	private InvoiceMessage message() {
		ProductPayload product = new ProductPayload("Consulting", null, BigDecimal.TEN, BigDecimal.ONE);
		LineItemPayload lineItem = new LineItemPayload(product, BigDecimal.ONE, BigDecimal.TEN, null, null,
				BigDecimal.TEN);
		CustomerPayload customer = new CustomerPayload("Acme", "Jane", "jane@acme.com", null, null);
		return new InvoiceMessage("INV-001", LocalDate.now(), LocalDate.now().plusDays(30), "PENDING", null,
				customer, List.of(lineItem));
	}

}
