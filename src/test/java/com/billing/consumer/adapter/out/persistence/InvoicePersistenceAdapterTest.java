package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.mapper.CustomerEntityMapper;
import com.billing.consumer.adapter.out.persistence.mapper.InvoiceEntityMapper;
import com.billing.consumer.adapter.out.persistence.mapper.ProductEntityMapper;
import com.billing.consumer.application.port.out.CustomerRepositoryPort;
import com.billing.consumer.application.port.out.InvoiceRepositoryPort;
import com.billing.consumer.application.port.out.ProductRepositoryPort;
import com.billing.consumer.domain.Customer;
import com.billing.consumer.domain.Invoice;
import com.billing.consumer.domain.InvoiceLineItem;
import com.billing.consumer.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe um Postgres real via Testcontainers e roda contra {@code src/test/resources/schema.sql},
 * mantendo {@code ddl-auto=validate} (herdado de application.properties) para validar que os
 * mapeamentos JPA batem com o schema real, igual em producao.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({
		CustomerRepositoryAdapter.class, ProductRepositoryAdapter.class, InvoiceRepositoryAdapter.class,
		CustomerEntityMapper.class, ProductEntityMapper.class, InvoiceEntityMapper.class
})
class InvoicePersistenceAdapterTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void sqlInit(DynamicPropertyRegistry registry) {
		registry.add("spring.sql.init.mode", () -> "always");
	}

	@Autowired
	private CustomerRepositoryPort customerRepositoryPort;

	@Autowired
	private ProductRepositoryPort productRepositoryPort;

	@Autowired
	private InvoiceRepositoryPort invoiceRepositoryPort;

	@Test
	void savesAndFindsCustomerByEmail() {
		Customer saved = customerRepositoryPort.save(
				new Customer("Acme", "Jane Doe", "jane@acme.com", "Rua A", "Rua B"));

		assertThat(saved.getCustomerId()).isNotNull();
		assertThat(customerRepositoryPort.findByEmail("jane@acme.com"))
				.hasValueSatisfying(found -> assertThat(found.getCompanyName()).isEqualTo("Acme"));
	}

	@Test
	void savesAndFindsProductByName() {
		Product saved = productRepositoryPort.save(
				new Product("Consulting", "Hourly", new BigDecimal("150.00"), new BigDecimal("10.00")));

		assertThat(saved.getProductId()).isNotNull();
		assertThat(productRepositoryPort.findByName("Consulting"))
				.hasValueSatisfying(found -> assertThat(found.getUnitPrice()).isEqualByComparingTo("150.00"));
	}

	@Test
	void savesInvoiceWithLineItemsAndTracksInvoiceNumber() {
		Customer customer = customerRepositoryPort.save(
				new Customer("Acme", "Jane Doe", "jane2@acme.com", null, null));
		Product product = productRepositoryPort.save(
				new Product("Support", null, new BigDecimal("80.00"), new BigDecimal("5.00")));

		InvoiceLineItem lineItem = new InvoiceLineItem(product, BigDecimal.ONE, new BigDecimal("80.00"),
				null, new BigDecimal("4.00"), new BigDecimal("84.00"));
		Invoice invoice = new Invoice("INV-100", customer, LocalDate.now(), LocalDate.now().plusDays(15),
				"PENDING", null, List.of(lineItem));

		Invoice saved = invoiceRepositoryPort.save(invoice);

		assertThat(saved.getInvoiceId()).isNotNull();
		assertThat(saved.getLineItems()).hasSize(1);
		assertThat(saved.getLineItems().getFirst().getLineItemId()).isNotNull();
		assertThat(invoiceRepositoryPort.existsByInvoiceNumber("INV-100")).isTrue();
		assertThat(invoiceRepositoryPort.existsByInvoiceNumber("INV-999")).isFalse();
	}

}
