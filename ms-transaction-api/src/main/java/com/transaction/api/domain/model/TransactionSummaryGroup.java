package com.transaction.api.domain.model;

import java.math.BigDecimal;

public record TransactionSummaryGroup(String key,
                                      int count,
                                      BigDecimal totalAmount,
                                      BigDecimal averageAmount,
                                      BigDecimal minAmount,
                                      BigDecimal maxAmount,
                                      int flaggedCount) {}
