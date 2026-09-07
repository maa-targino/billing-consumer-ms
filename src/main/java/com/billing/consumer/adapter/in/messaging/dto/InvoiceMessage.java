package com.billing.consumer.adapter.in.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload autocontido publicado em {@code qq.billing.in}: traz cliente e produtos aninhados,
 * uma fatura por mensagem. Este record e o contrato que o batch producer devera cumprir.
 */
public record InvoiceMessage(

		@NotBlank String invoiceNumber,

		@NotNull LocalDate issueDate,

		@NotNull LocalDate dueDate,

		@NotBlank String status,

		String notes,

		@NotNull @Valid CustomerPayload customer,

		@NotEmpty @Valid List<LineItemPayload> lineItems

) {
}
