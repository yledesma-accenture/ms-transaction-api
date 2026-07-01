package com.transaction.api.adapters.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SummaryQuery(LocalDate txDateFrom,
                           LocalDate txDateTo,
                           LocalDate ingestionDateFrom,
                           LocalDate ingestionDateTo,
                           String groupBy) {
}
