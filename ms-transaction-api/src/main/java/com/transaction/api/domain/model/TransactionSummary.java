package com.transaction.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionSummary(LocalDate txDateFrom,
                                 LocalDate txDateTo,
                                 LocalDate ingestionDateFrom,
                                 LocalDate ingestionDateTo,
                                 long totalCount,
                                 BigDecimal totalAmount,
                                 String groupedBy,
                                 List<TransactionSummaryGroup> groups) {
}
