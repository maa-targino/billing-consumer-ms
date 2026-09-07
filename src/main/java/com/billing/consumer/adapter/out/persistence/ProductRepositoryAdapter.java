package com.billing.consumer.adapter.out.persistence;

import com.billing.consumer.adapter.out.persistence.entity.ProductEntity;
import com.billing.consumer.adapter.out.persistence.mapper.ProductEntityMapper;
import com.billing.consumer.application.port.out.ProductRepositoryPort;
import com.billing.consumer.domain.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ProductRepositoryAdapter implements ProductRepositoryPort {

	private final ProductJpaRepository jpaRepository;
	private final ProductEntityMapper mapper;

	ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductEntityMapper mapper) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public Optional<Product> findByName(String name) {
		return jpaRepository.findByName(name).map(mapper::toDomain);
	}

	@Override
	public Product save(Product product) {
		ProductEntity saved = jpaRepository.save(mapper.toEntity(product));
		return mapper.toDomain(saved);
	}

}
