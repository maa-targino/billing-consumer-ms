package com.billing.consumer.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_items")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceLineItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "line_item_id")
	private Integer lineItemId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private InvoiceEntity invoice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductEntity product;

	@Column(name = "quantity", nullable = false, precision = 10, scale = 2)
	private BigDecimal quantity;

	@Column(name = "historic_unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal historicUnitPrice;

	@Column(name = "discount_amount", precision = 10, scale = 2)
	private BigDecimal discountAmount;

	@Column(name = "tax_amount", precision = 10, scale = 2)
	private BigDecimal taxAmount;

	@Column(name = "line_total", nullable = false, precision = 10, scale = 2)
	private BigDecimal lineTotal;

}
