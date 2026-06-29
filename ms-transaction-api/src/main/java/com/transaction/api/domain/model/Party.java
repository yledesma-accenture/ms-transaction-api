package com.transaction.api.domain.model;

import java.util.UUID;

public record Party(
        UUID id,
        String accountNumber,
        String cbu,
        String cuit,
        String holderName,
        String holderType,
        String bankCode,
        String branchCode

) {
}
