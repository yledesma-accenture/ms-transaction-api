package com.transaction.api.adapters.inbound.rest.dto;

public record ErrorDetail(String field,
                          Object rejectedValue,
                          String message) {}
