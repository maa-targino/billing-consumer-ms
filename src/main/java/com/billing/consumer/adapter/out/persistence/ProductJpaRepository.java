package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ProductJpaRepository extends JpaRepository<ProductEntity, Integer> {

	Optional<ProductEntity> findByName(String name);

}
