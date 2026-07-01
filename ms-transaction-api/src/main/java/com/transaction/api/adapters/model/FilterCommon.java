package com.transaction.api.adapters.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record FilterCommon(LocalDate txDateFrom,
                           LocalDate txDateTo,
                           LocalDate ingestionDateFrom,
                           LocalDate ingestionDateTo,
                           int page,
                           int size,
                           String sort) {
}
