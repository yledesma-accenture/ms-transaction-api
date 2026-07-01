package com.transaction.api.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        String externalRef,
        OffsetDateTime transactionAt,
        OffsetDateTime ingestedAt,
        String type,
        String status,
        int amount,
        String currency,
        Party benefactor,
        Party beneficiary,
        String description,
        UUID fileId,
        String createdBy,
        boolean flagged,
        String flagReason) {}
