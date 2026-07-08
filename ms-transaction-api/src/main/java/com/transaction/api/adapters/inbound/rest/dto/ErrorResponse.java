package com.transaction.api.adapters.inbound.rest.dto;

import java.util.List;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ErrorDetail> details
) {}