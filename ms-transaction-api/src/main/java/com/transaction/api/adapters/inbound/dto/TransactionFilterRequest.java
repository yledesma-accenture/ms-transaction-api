package com.transaction.api.adapters.inbound.dto;

import java.time.LocalDate;

public record TransactionFilterRequest(LocalDate txDateFrom,
                                       LocalDate txDateTo,
                                       LocalDate ingestionDateFrom,
                                       LocalDate ingestionDateTo,
                                       Integer page,
                                       Integer size,
                                       String sort) {
}
