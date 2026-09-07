package com.billing.consumer.domain;

/** Cliente. Objeto de dominio imutavel, sem dependencia de JPA/Spring. */
public class Customer {

	private final Integer customerId;
	private final String companyName;
	private final String contactName;

	/** Chave natural usada na resolucao da mensagem. Sem indice unico no banco. */
	private final String email;

	private final String billingAddress;
	private final String shippingAddress;

	/** Cliente ainda nao persistido. */
	public Customer(String companyName, String contactName, String email, String billingAddress,
			String shippingAddress) {
		this(null, companyName, contactName, email, billingAddress, shippingAddress);
	}

	/** Reidratacao a partir da persistencia, com id ja atribuido. */
	public Customer(Integer customerId, String companyName, String contactName, String email,
			String billingAddress, String shippingAddress) {
		this.customerId = customerId;
		this.companyName = companyName;
		this.contactName = contactName;
		this.email = email;
		this.billingAddress = billingAddress;
		this.shippingAddress = shippingAddress;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getContactName() {
		return contactName;
	}

	public String getEmail() {
		return email;
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

}
