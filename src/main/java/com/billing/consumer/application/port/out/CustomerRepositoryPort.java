package com.billing.consumer.application.port.out;

import com.billing.consumer.domain.Customer;

import java.util.Optional;

/** Porta de saida para persistencia de clientes. */
public interface CustomerRepositoryPort {

	Optional<Customer> findByEmail(String email);

	Customer save(Customer customer);

}
