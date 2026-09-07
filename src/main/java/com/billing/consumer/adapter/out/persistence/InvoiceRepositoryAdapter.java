package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.InvoiceEntity;
import com.billing.consumer.adapter.out.persistence.mapper.InvoiceEntityMapper;
import com.billing.consumer.application.port.out.InvoiceRepositoryPort;
import com.billing.consumer.domain.Invoice;
import org.springframework.stereotype.Component;

@Component
class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

	private final InvoiceJpaRepository jpaRepository;
	private final InvoiceEntityMapper mapper;

	InvoiceRepositoryAdapter(InvoiceJpaRepository jpaRepository, InvoiceEntityMapper mapper) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public boolean existsByInvoiceNumber(String invoiceNumber) {
		return jpaRepository.existsByInvoiceNumber(invoiceNumber);
	}

	@Override
	public Invoice save(Invoice invoice) {
		InvoiceEntity saved = jpaRepository.save(mapper.toEntity(invoice));
		return mapper.toDomain(saved, invoice);
	}

}
