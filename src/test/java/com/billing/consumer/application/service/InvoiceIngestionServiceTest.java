package com.billing.consumer.application.service;

import com.billing.consumer.application.port.in.IngestInvoiceCommand;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.CustomerData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.LineItemData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.ProductData;
import com.billing.consumer.application.port.out.CustomerRepositoryPort;
import com.billing.consumer.application.port.out.InvoiceRepositoryPort;
import com.billing.consumer.application.port.out.ProductRepositoryPort;
import com.billing.consumer.domain.Customer;
import com.billing.consumer.domain.Invoice;
import com.billing.consumer.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceIngestionServiceTest {

	@Mock
	private InvoiceRepositoryPort invoiceRepository;

	@Mock
	private CustomerRepositoryPort customerRepository;

	@Mock
	private ProductRepositoryPort productRepository;

	private InvoiceIngestionService service;

	@BeforeEach
	void setUp() {
		service = new InvoiceIngestionService(invoiceRepository, customerRepository, productRepository);
	}

	@Test
	void skipsIngestionWhenInvoiceAlreadyExists() {
		when(invoiceRepository.existsByInvoiceNumber("INV-001")).thenReturn(true);

		service.ingest(command("INV-001"));

		verify(invoiceRepository, never()).save(any());
		verify(customerRepository, never()).save(any());
	}

	@Test
	void reusesExistingCustomerByEmail() {
		Customer existing = new Customer(7, "Acme", "Jane", "jane@acme.com", null, null);
		when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
		when(customerRepository.findByEmail("jane@acme.com")).thenReturn(Optional.of(existing));
		when(productRepository.findByName("Consulting"))
				.thenReturn(Optional.of(new Product(9, "Consulting", null, BigDecimal.TEN, BigDecimal.ONE)));
		when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.ingest(command("INV-001"));

		verify(customerRepository, never()).save(any());
		ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
		verify(invoiceRepository).save(captor.capture());
		assertThat(captor.getValue().getCustomer().getCustomerId()).isEqualTo(7);
	}

	@Test
	void createsNewCustomerWhenNotFound() {
		when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
		when(customerRepository.findByEmail("jane@acme.com")).thenReturn(Optional.empty());
		when(customerRepository.save(any()))
				.thenAnswer(invocation -> {
					Customer c = invocation.getArgument(0);
					return new Customer(7, c.getCompanyName(), c.getContactName(), c.getEmail(),
							c.getBillingAddress(), c.getShippingAddress());
				});
		when(productRepository.findByName("Consulting"))
				.thenReturn(Optional.of(new Product(9, "Consulting", null, BigDecimal.TEN, BigDecimal.ONE)));
		when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.ingest(command("INV-001"));

		verify(customerRepository).save(any());
	}

	@Test
	void createsNewProductWhenNotFound() {
		when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
		when(customerRepository.findByEmail(any()))
				.thenReturn(Optional.of(new Customer(7, "Acme", "Jane", "jane@acme.com", null, null)));
		when(productRepository.findByName("Consulting")).thenReturn(Optional.empty());
		when(productRepository.save(any()))
				.thenAnswer(invocation -> {
					Product p = invocation.getArgument(0);
					return new Product(9, p.getName(), p.getDescription(), p.getUnitPrice(), p.getTaxRate());
				});
		when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.ingest(command("INV-001"));

		verify(productRepository).save(any());
	}

	@Test
	void mapsAllLineItemsOntoTheInvoice() {
		when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
		when(customerRepository.findByEmail(any()))
				.thenReturn(Optional.of(new Customer(7, "Acme", "Jane", "jane@acme.com", null, null)));
		when(productRepository.findByName(any()))
				.thenReturn(Optional.of(new Product(9, "Consulting", null, BigDecimal.TEN, BigDecimal.ONE)));
		when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		IngestInvoiceCommand twoItems = new IngestInvoiceCommand("INV-001", LocalDate.now(),
				LocalDate.now().plusDays(30), "PENDING", null,
				new CustomerData("Acme", "Jane", "jane@acme.com", null, null),
				List.of(lineItem(), lineItem()));

		service.ingest(twoItems);

		ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
		verify(invoiceRepository).save(captor.capture());
		assertThat(captor.getValue().getLineItems()).hasSize(2);
	}

	private IngestInvoiceCommand command(String invoiceNumber) {
		return new IngestInvoiceCommand(invoiceNumber, LocalDate.now(), LocalDate.now().plusDays(30),
				"PENDING", null, new CustomerData("Acme", "Jane", "jane@acme.com", null, null),
				List.of(lineItem()));
	}

	private LineItemData lineItem() {
		return new LineItemData(new ProductData("Consulting", null, BigDecimal.TEN, BigDecimal.ONE),
				BigDecimal.ONE, BigDecimal.TEN, null, null, BigDecimal.TEN);
	}

}
