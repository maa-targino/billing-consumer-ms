package com.billing.consumer.adapter.in.messaging.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Cliente aninhado na mensagem. Resolvido por {@code email} (chave natural). */
public record CustomerPayload(

		@NotBlank String companyName,

		String contactName,

		@NotBlank @Email String email,

		String billingAddress,

		String shippingAddress

) {
}
