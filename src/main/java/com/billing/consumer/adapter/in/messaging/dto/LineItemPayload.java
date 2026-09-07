package com.billing.consumer.adapter.in.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Item da fatura. {@code taxAmount} e {@code lineTotal} sao persistidos como vieram. */
public record LineItemPayload(

		@NotNull @Valid ProductPayload product,

		@NotNull BigDecimal quantity,

		@NotNull BigDecimal historicUnitPrice,

		BigDecimal discountAmount,

		BigDecimal taxAmount,

		@NotNull BigDecimal lineTotal

) {
}
