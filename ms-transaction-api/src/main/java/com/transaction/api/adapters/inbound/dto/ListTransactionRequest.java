package com.transaction.api.adapters.inbound.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ListTransactionRequest(LocalDate txDateFrom,
                                     LocalDate txDateTo,
                                     LocalDate ingestionDateFrom,
                                     LocalDate ingestionDateTo,
                                     String type,
                                     String status,
                                     String currency,
                                     BigDecimal amountMin,
                                     BigDecimal amountMax,
                                     UUID fileId,
                                     Boolean flagged,
                                     Integer page,
                                     Integer size,
                                     String sort) {
}
