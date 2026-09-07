package com.billing.consumer.domain;

import java.math.BigDecimal;

/** Produto/servico. Objeto de dominio imutavel, sem dependencia de JPA/Spring. */
public class Product {

	private final Integer productId;

	/** Chave natural usada na resolucao da mensagem. */
	private final String name;

	private final String description;
	private final BigDecimal unitPrice;
	private final BigDecimal taxRate;

	/** Produto ainda nao persistido. */
	public Product(String name, String description, BigDecimal unitPrice, BigDecimal taxRate) {
		this(null, name, description, unitPrice, taxRate);
	}

	/** Reidratacao a partir da persistencia, com id ja atribuido. */
	public Product(Integer productId, String name, String description, BigDecimal unitPrice,
			BigDecimal taxRate) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.unitPrice = unitPrice;
		this.taxRate = taxRate;
	}

	public Integer getProductId() {
		return productId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getTaxRate() {
		return taxRate;
	}

}
