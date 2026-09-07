package com.billing.consumer.adapter.in.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Produto/servico aninhado no item. Resolvido por {@code name} (chave natural). */
public record ProductPayload(

		@NotBlank String name,

		String description,

		@NotNull BigDecimal unitPrice,

		@NotNull BigDecimal taxRate

) {
}
