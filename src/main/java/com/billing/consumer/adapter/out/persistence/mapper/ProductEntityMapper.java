package com.billing.consumer.adapter.out.persistence.mapper;

import com.billing.consumer.adapter.out.persistence.entity.ProductEntity;
import com.billing.consumer.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

	public Product toDomain(ProductEntity entity) {
		return new Product(entity.getProductId(), entity.getName(), entity.getDescription(),
				entity.getUnitPrice(), entity.getTaxRate());
	}

	public ProductEntity toEntity(Product domain) {
		ProductEntity entity = new ProductEntity();
		entity.setName(domain.getName());
		entity.setDescription(domain.getDescription());
		entity.setUnitPrice(domain.getUnitPrice());
		entity.setTaxRate(domain.getTaxRate());
		return entity;
	}

}
