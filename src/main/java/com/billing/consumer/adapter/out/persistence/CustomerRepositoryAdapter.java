package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.CustomerEntity;
import com.billing.consumer.adapter.out.persistence.mapper.CustomerEntityMapper;
import com.billing.consumer.application.port.out.CustomerRepositoryPort;
import com.billing.consumer.domain.Customer;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CustomerRepositoryAdapter implements CustomerRepositoryPort {

	private final CustomerJpaRepository jpaRepository;
	private final CustomerEntityMapper mapper;

	CustomerRepositoryAdapter(CustomerJpaRepository jpaRepository, CustomerEntityMapper mapper) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public Optional<Customer> findByEmail(String email) {
		return jpaRepository.findByEmail(email).map(mapper::toDomain);
	}

	@Override
	public Customer save(Customer customer) {
		CustomerEntity saved = jpaRepository.save(mapper.toEntity(customer));
		return mapper.toDomain(saved);
	}

}
