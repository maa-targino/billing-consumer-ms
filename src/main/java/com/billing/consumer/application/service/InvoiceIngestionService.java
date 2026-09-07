package com.billing.consumer.application.service;

import com.billing.consumer.application.port.in.IngestInvoiceCommand;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.CustomerData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.LineItemData;
import com.billing.consumer.application.port.in.IngestInvoiceCommand.ProductData;
import com.billing.consumer.application.port.in.IngestInvoiceUseCase;
import com.billing.consumer.application.port.out.CustomerRepositoryPort;
import com.billing.consumer.application.port.out.InvoiceRepositoryPort;
import com.billing.consumer.application.port.out.ProductRepositoryPort;
import com.billing.consumer.domain.Customer;
import com.billing.consumer.domain.Invoice;
import com.billing.consumer.domain.InvoiceLineItem;
import com.billing.consumer.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceIngestionService implements IngestInvoiceUseCase {

	private static final Logger log = LoggerFactory.getLogger(InvoiceIngestionService.class);

	private final InvoiceRepositoryPort invoiceRepository;
	private final CustomerRepositoryPort customerRepository;
	private final ProductRepositoryPort productRepository;

	public InvoiceIngestionService(InvoiceRepositoryPort invoiceRepository,
			CustomerRepositoryPort customerRepository,
			ProductRepositoryPort productRepository) {
		this.invoiceRepository = invoiceRepository;
		this.customerRepository = customerRepository;
		this.productRepository = productRepository;
	}

	@Override
	@Transactional
	public void ingest(IngestInvoiceCommand command) {
		if (invoiceRepository.existsByInvoiceNumber(command.invoiceNumber())) {
			log.info("Fatura {} já existe, ignorando reentrega", command.invoiceNumber());
			return;
		}

		Customer customer = resolveCustomer(command.customer());
		List<InvoiceLineItem> lineItems = command.lineItems().stream()
				.map(this::toLineItem)
				.toList();

		Invoice invoice = new Invoice(command.invoiceNumber(), customer, command.issueDate(),
				command.dueDate(), command.status(), command.notes(), lineItems);

		Invoice saved = invoiceRepository.save(invoice);
		log.info("Fatura {} persistida com {} item(ns)", saved.getInvoiceNumber(),
				saved.getLineItems().size());
	}

	private InvoiceLineItem toLineItem(LineItemData item) {
		Product product = resolveProduct(item.product());
		return new InvoiceLineItem(product, item.quantity(), item.historicUnitPrice(),
				item.discountAmount(), item.taxAmount(), item.lineTotal());
	}

	/** Resolve por email; se ja existir, reaproveita sem atualizar o cadastro. */
	private Customer resolveCustomer(CustomerData data) {
		return customerRepository.findByEmail(data.email())
				.orElseGet(() -> {
					log.info("Criando cliente {}", data.email());
					Customer customer = new Customer(data.companyName(), data.contactName(),
							data.email(), data.billingAddress(), data.shippingAddress());
					return customerRepository.save(customer);
				});
	}

	/** Resolve por nome; se ja existir, reaproveita sem atualizar o cadastro. */
	private Product resolveProduct(ProductData data) {
		return productRepository.findByName(data.name())
				.orElseGet(() -> {
					log.info("Criando produto {}", data.name());
					Product product = new Product(data.name(), data.description(), data.unitPrice(),
							data.taxRate());
					return productRepository.save(product);
				});
	}

}
