package com.billing.consumer.adapter.out.persistence.mapper;

import com.billing.consumer.adapter.out.persistence.entity.CustomerEntity;
import com.billing.consumer.domain.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {

	public Customer toDomain(CustomerEntity entity) {
		return new Customer(entity.getCustomerId(), entity.getCompanyName(), entity.getContactName(),
				entity.getEmail(), entity.getBillingAddress(), entity.getShippingAddress());
	}

	public CustomerEntity toEntity(Customer domain) {
		CustomerEntity entity = new CustomerEntity();
		entity.setCompanyName(domain.getCompanyName());
		entity.setContactName(domain.getContactName());
		entity.setEmail(domain.getEmail());
		entity.setBillingAddress(domain.getBillingAddress());
		entity.setShippingAddress(domain.getShippingAddress());
		return entity;
	}

}
