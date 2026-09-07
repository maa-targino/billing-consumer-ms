package com.billing.consumer.application.port.out;

import com.billing.consumer.domain.Product;

import java.util.Optional;

/** Porta de saida para persistencia de produtos/servicos. */
public interface ProductRepositoryPort {

	Optional<Product> findByName(String name);

	Product save(Product product);

}
