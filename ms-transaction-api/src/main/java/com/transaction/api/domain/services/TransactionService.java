package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService implements ITransactionPort {
    private final ITransactionDatabasePort transactionDatabasePort;

    public TransactionService(ITransactionDatabasePort transactionDatabasePort) {
        this.transactionDatabasePort = transactionDatabasePort;
    }

    public static final String TRANSACTION_NOT_FOUND = "Transacción no encontrada";

    @Override
    public TransactionDetail transactionId(String transactionId) {
        return transactionDatabasePort.findById(transactionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
    }

    @Override
    public TransactionPage searchTransactionByUser(SearchTransactionByUserQuery searchTransactionByUserQuery) {
        return transactionDatabasePort.searchTransactionByUser(searchTransactionByUserQuery);
    }

    @Override
    public TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery) {
        return transactionDatabasePort.listTransaction(listTransactionsQuery);
    }

    @Override
    public TransactionSummary getSummary(SummaryQuery summaryQuery) {
        return transactionDatabasePort.getSummary(summaryQuery);
    }

    @Override
    public TransactionPage transactionCbu(String cbu, SearchTransactionByCbuQuery searchTransactionByCbuQuery) {
        return transactionDatabasePort.transactionCbu(cbu, searchTransactionByCbuQuery)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
    }

    @Override
    public TransactionPage transactionCuit(String cuit,  SearchTransactionByCuitQuery searchTransactionByCuitQuery) {
        return transactionDatabasePort.transactionCuit(cuit, searchTransactionByCuitQuery)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TRANSACTION_NOT_FOUND));
    }
}
