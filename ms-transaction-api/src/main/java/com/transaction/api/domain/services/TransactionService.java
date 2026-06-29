package com.transaction.api.domain.services;

import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements ITransactionPort {

    private  TransactionDetail createTransactionDetailMock() {
        // Crear los datos de benefactor
        Party benefactor = new Party(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "ACC-00123",
                "0000003100012345678901",
                "20301234567",
                "Alice Martinez",
                "INDIVIDUAL",
                "310",
                "BR-042"
        );

        // Crear los datos de beneficiary (mismo en este caso)
        Party beneficiary = new Party(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "ACC-00123",
                "0000003100012345678901",
                "20301234567",
                "Alice Martinez",
                "INDIVIDUAL",
                "310",
                "BR-042"
        );

        // Crear la transacción
        Transaction transaction = new Transaction(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "TNX-2024-000001",
                OffsetDateTime.parse("2024-03-15T09:00:00Z"),
                OffsetDateTime.parse("2024-03-15T09:05:22Z"),
                TransactionType.DEBIT,
                "COMPLETED",
                250,
                "USD",
                benefactor,
                beneficiary,
                "ATM withdrawal",
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "operator-42",
                true,
                "Amount exceeds account 30-day average by 720%"
        );

        // Crear lista de validación warnings
        ValidationWarning warning = new ValidationWarning(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "MISSING_DESCRIPTION",
                "Optional field 'description' was empty",
                OffsetDateTime.parse("2026-06-29T19:35:04.387Z")
        );

        List<ValidationWarning> warnings = List.of(warning);

        // Crear y retornar TransactionDetail
        return new TransactionDetail(
                transaction,
                OffsetDateTime.parse("2026-06-29T19:35:04.387Z"),
                OffsetDateTime.parse("2026-06-29T19:35:04.387Z"),
                warnings
        );
    }


    @Override
    public TransactionDetail transactionId(String transactionId) {
        return createTransactionDetailMock();
    }
}
