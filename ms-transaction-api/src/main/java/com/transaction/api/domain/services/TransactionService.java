package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements ITransactionPort {

    public static final String TRANSACTION_NOT_FOUND = "Transacción no encontrada";
    ITransactionDatabasePort databasePort;


    public TransactionService(ITransactionDatabasePort databasePort) {
        this.databasePort = databasePort;
    }


    private TransactionPage createDummyTransactionPage(int page, int size) {
        // Creamos un ejemplo simple con un único Transaction
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                "EXT-0001",
                java.time.OffsetDateTime.now(),
                java.time.OffsetDateTime.now(),
                "DEBIT",
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

    private TransactionSummary createDummyTransactionSummary(LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom,
                                                             LocalDate ingestionDateTo, String groupBy) {
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
        return databasePort.findById(transactionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
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

    @Override
    public TransactionPage transactionCbu(String cbu, SearchTransactionByCbuQuery searchTransactionByCbuQuery) {
        return databasePort.transactionCbu(cbu, searchTransactionByCbuQuery)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
    }

    @Override
    public TransactionPage transactionCuit(String cuit,  SearchTransactionByCuitQuery searchTransactionByCuitQuery) {
        return databasePort.transactionCuit(cuit, searchTransactionByCuitQuery)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
    }
}
