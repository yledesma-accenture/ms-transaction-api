package com.transaction.api.adapters.inbound.rest.validator;

import com.transaction.api.domain.exception.BadRequestException;
import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RequestValidator {
    public void validate(LocalDate txDateFrom,
                         LocalDate txDateTo,
                         LocalDate ingestionDateFrom,
                         LocalDate ingestionDateTo,
                         String type,
                         String status) {
        validateDates(txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo);
        validateStatus(status);
        validateType(type);
    }

    private TransactionStatus validateStatus(String status){
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TransactionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("transactionStatus inválido: " + status);
        }
    }

    private TransactionType validateType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("transactionType inválido: " + type);
        }
    }

    private void validateDates(LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom, LocalDate ingestionDateTo) {
        if (txDateFrom != null && txDateTo != null &&
                txDateFrom.isAfter(txDateTo)) {
            throw new BadRequestException("txDateFrom must not be after txDateTo");
        }

        if (ingestionDateFrom != null && ingestionDateTo != null &&
                ingestionDateFrom.isAfter(ingestionDateTo)) {
            throw new BadRequestException("ingestionDateFrom must not be after ingestionDateTo");
        }
    }
}
