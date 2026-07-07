package com.transaction.api.adapters.model;

import lombok.Builder;

@Builder
public record SearchTransactionByUserQuery(String userId,
                                           FilterCommon filterCommon) {}


