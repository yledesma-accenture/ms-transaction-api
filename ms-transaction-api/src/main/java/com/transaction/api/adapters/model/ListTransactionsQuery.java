package com.transaction.api.adapters.model;

import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ListTransactionsQuery(FilterCommon filterCommon,
                                    TransactionType transactionType,
                                    TransactionStatus transactionStatus,
                                    String currency,
                                    BigDecimal amountMin,
                                    BigDecimal amountMax,
                                    UUID fileId,
                                    Boolean flagged) {
}
