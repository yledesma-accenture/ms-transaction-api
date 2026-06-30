package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private TransactionPage createDummyTransactionPage(int page, int size) {
        // Creamos un ejemplo simple con un único Transaction
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                "EXT-0001",
                java.time.OffsetDateTime.now(),
                java.time.OffsetDateTime.now(),
                TransactionType.DEBIT,
                "COMPLETED",
                1000,
                "ARS",
                new Party(UUID.randomUUID(), "20000000001", "0170099220000067797370", "20329851657", "Juan Perez", "PERSON", "001", "0001"),
                new Party(UUID.randomUUID(), "30000000002", "0170099220000067797371", "20329851658", "Maria Gomez", "PERSON", "001", "0002"),
                "Pago de ejemplo",
                UUID.randomUUID(),
                "system",
                false,
                null
        );

        List<Transaction> content = List.of(tx);
        long totalElements = content.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        boolean last = page >= Math.max(0, totalPages - 1);

        return new TransactionPage(content, page, size, totalElements, totalPages, last);
    }

    private TransactionSummary createDummyTransactionSummary(
            LocalDate txDateFrom,
            LocalDate txDateTo,
            LocalDate ingestionDateFrom,
            LocalDate ingestionDateTo,
            String groupBy) {

        TransactionSummaryGroup group1 = new TransactionSummaryGroup(
                "DEBIT",
                5,
                new BigDecimal("5000"),
                new BigDecimal("1000"),
                new BigDecimal("500"),
                new BigDecimal("1500"),
                0
        );

        TransactionSummaryGroup group2 = new TransactionSummaryGroup(
                "CREDIT",
                3,
                new BigDecimal("7500"),
                new BigDecimal("2500"),
                new BigDecimal("2000"),
                new BigDecimal("3500"),
                1
        );

        List<TransactionSummaryGroup> groups = List.of(group1, group2);

        long totalCount = groups.stream().mapToLong(TransactionSummaryGroup::count).sum();
        BigDecimal totalAmount = groups.stream()
                .map(TransactionSummaryGroup::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionSummary(
                txDateFrom,
                txDateTo,
                ingestionDateFrom,
                ingestionDateTo,
                totalCount,
                totalAmount,
                groupBy,
                groups
        );
    }


    @Override
    public TransactionDetail transactionId(String transactionId) {
        return createTransactionDetailMock();
    }

    @Override
    public TransactionPage searchTransactionByUser(SearchTransactionByUserQuery searchTransactionByUserQuery) {
        return createDummyTransactionPage(searchTransactionByUserQuery.filterCommon().page(), searchTransactionByUserQuery.filterCommon().size());
    }

    @Override
    public TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery) {
        return createDummyTransactionPage(listTransactionsQuery.filterCommon().page(), listTransactionsQuery.filterCommon().size());
    }

    @Override
    public TransactionSummary getSummary(SummaryQuery summaryQuery) {
        return createDummyTransactionSummary(
                summaryQuery.txDateFrom(),
                summaryQuery.txDateTo(),
                summaryQuery.ingestionDateFrom(),
                summaryQuery.ingestionDateTo(),
                summaryQuery.groupBy()
        );
    }
}
