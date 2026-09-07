package com.billing.consumer.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class CustomerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Integer customerId;

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "billing_address", columnDefinition = "text")
	private String billingAddress;

	@Column(name = "shipping_address", columnDefinition = "text")
	private String shippingAddress;

}
