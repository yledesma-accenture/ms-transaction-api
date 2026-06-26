package com.transaction.api.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

public record TransactionDetail(
        Transaction transaction,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ValidationWarning> validationWarnings){}
