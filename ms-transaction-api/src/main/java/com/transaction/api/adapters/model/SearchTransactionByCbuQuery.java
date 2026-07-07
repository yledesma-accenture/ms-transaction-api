package com.transaction.api.adapters.model;

import lombok.Builder;

@Builder
public record SearchTransactionByCbuQuery(String cbu,
                                           FilterCommon filterCommon) {}
