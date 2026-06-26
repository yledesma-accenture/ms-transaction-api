package com.transaction.api.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ValidationWarning(
        UUID id,
        String warningCode,
        String warningMessage,
        OffsetDateTime createdAt

) {
}
