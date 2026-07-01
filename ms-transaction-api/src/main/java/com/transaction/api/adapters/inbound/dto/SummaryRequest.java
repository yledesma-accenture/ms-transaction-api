package com.transaction.api.adapters.inbound.dto;

import java.time.LocalDate;

public record SummaryRequest(LocalDate txDateFrom,
                             LocalDate txDateTo,
                             LocalDate ingestionDateFrom,
                             LocalDate ingestionDateTo,
                             String groupBy) {
}
