package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Integer> {

	Optional<CustomerEntity> findByEmail(String email);

}
