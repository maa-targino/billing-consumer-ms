package com.billing.consumer.domain;

import java.math.BigDecimal;

/** Item de fatura. Objeto de dominio imutavel, sem dependencia de JPA/Spring. */
public class InvoiceLineItem {

	private final Integer lineItemId;
	private final Product product;
	private final BigDecimal quantity;
	private final BigDecimal historicUnitPrice;
	private final BigDecimal discountAmount;
	private final BigDecimal taxAmount;
	private final BigDecimal lineTotal;

	/** Item ainda nao persistido. */
	public InvoiceLineItem(Product product, BigDecimal quantity, BigDecimal historicUnitPrice,
			BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal lineTotal) {
		this(null, product, quantity, historicUnitPrice, discountAmount, taxAmount, lineTotal);
	}

	/** Reidratacao a partir da persistencia, com id ja atribuido. */
	public InvoiceLineItem(Integer lineItemId, Product product, BigDecimal quantity,
			BigDecimal historicUnitPrice, BigDecimal discountAmount, BigDecimal taxAmount,
			BigDecimal lineTotal) {
		this.lineItemId = lineItemId;
		this.product = product;
		this.quantity = quantity;
		this.historicUnitPrice = historicUnitPrice;
		this.discountAmount = discountAmount;
		this.taxAmount = taxAmount;
		this.lineTotal = lineTotal;
	}

	public Integer getLineItemId() {
		return lineItemId;
	}

	public Product getProduct() {
		return product;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getHistoricUnitPrice() {
		return historicUnitPrice;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

}
