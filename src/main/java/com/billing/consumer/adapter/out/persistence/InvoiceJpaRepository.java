package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, Integer> {

	boolean existsByInvoiceNumber(String invoiceNumber);

}
