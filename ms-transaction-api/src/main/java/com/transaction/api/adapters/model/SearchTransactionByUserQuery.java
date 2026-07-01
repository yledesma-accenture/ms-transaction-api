package com.transaction.api.adapters.model;

import lombok.Builder;

@Builder
public record SearchTransactionByUserQuery(Long userId,
                                           FilterCommon filterCommon) {}
