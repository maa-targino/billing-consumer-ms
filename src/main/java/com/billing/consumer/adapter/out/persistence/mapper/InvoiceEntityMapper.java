package com.billing.consumer.adapter.out.persistence.mapper;

import com.billing.consumer.adapter.out.persistence.entity.CustomerEntity;
import com.billing.consumer.adapter.out.persistence.entity.InvoiceEntity;
import com.billing.consumer.adapter.out.persistence.entity.InvoiceLineItemEntity;
import com.billing.consumer.adapter.out.persistence.entity.ProductEntity;
import com.billing.consumer.domain.Invoice;
import com.billing.consumer.domain.InvoiceLineItem;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente e produtos ja foram resolvidos (e persistidos) pelos seus proprios adapters antes da
 * fatura chegar aqui, entao usamos {@link EntityManager#getReference} para montar a associacao
 * apenas pelo id, sem disparar um SELECT desnecessario.
 */
@Component
public class InvoiceEntityMapper {

	private final EntityManager entityManager;

	public InvoiceEntityMapper(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public InvoiceEntity toEntity(Invoice domain) {
		InvoiceEntity entity = new InvoiceEntity();
		entity.setInvoiceNumber(domain.getInvoiceNumber());
		entity.setIssueDate(domain.getIssueDate());
		entity.setDueDate(domain.getDueDate());
		entity.setStatus(domain.getStatus());
		entity.setNotes(domain.getNotes());
		entity.setCustomer(entityManager.getReference(CustomerEntity.class,
				domain.getCustomer().getCustomerId()));

		for (InvoiceLineItem lineItem : domain.getLineItems()) {
			InvoiceLineItemEntity lineEntity = new InvoiceLineItemEntity();
			lineEntity.setProduct(entityManager.getReference(ProductEntity.class,
					lineItem.getProduct().getProductId()));
			lineEntity.setQuantity(lineItem.getQuantity());
			lineEntity.setHistoricUnitPrice(lineItem.getHistoricUnitPrice());
			lineEntity.setDiscountAmount(lineItem.getDiscountAmount());
			lineEntity.setTaxAmount(lineItem.getTaxAmount());
			lineEntity.setLineTotal(lineItem.getLineTotal());
			entity.addLineItem(lineEntity);
		}

		return entity;
	}

	/**
	 * Reconstroi o dominio a partir da entidade salva, reaproveitando cliente e produtos que ja
	 * vieram resolvidos no {@code original} para evitar acessar proxies lazy.
	 */
	public Invoice toDomain(InvoiceEntity saved, Invoice original) {
		List<InvoiceLineItem> lineItems = new ArrayList<>();
		List<InvoiceLineItemEntity> savedLineItems = saved.getLineItems();
		List<InvoiceLineItem> originalLineItems = original.getLineItems();

		for (int i = 0; i < originalLineItems.size(); i++) {
			InvoiceLineItem originalLineItem = originalLineItems.get(i);
			Integer lineItemId = savedLineItems.get(i).getLineItemId();
			lineItems.add(new InvoiceLineItem(lineItemId, originalLineItem.getProduct(),
					originalLineItem.getQuantity(), originalLineItem.getHistoricUnitPrice(),
					originalLineItem.getDiscountAmount(), originalLineItem.getTaxAmount(),
					originalLineItem.getLineTotal()));
		}

		return new Invoice(saved.getInvoiceId(), original.getInvoiceNumber(), original.getCustomer(),
				original.getIssueDate(), original.getDueDate(), original.getStatus(),
				original.getNotes(), lineItems);
	}

}
