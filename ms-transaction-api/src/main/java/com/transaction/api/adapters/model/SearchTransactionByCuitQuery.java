package com.transaction.api.adapters.model;

import lombok.Builder;

@Builder
public record SearchTransactionByCuitQuery(String cuit,
                                          FilterCommon filterCommon) {}
