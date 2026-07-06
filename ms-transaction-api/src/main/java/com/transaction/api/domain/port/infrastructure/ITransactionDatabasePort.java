package com.transaction.api.domain.port.infrastructure;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.TransactionSummary;

import java.util.Optional;

public interface ITransactionDatabasePort {

    public Optional<TransactionDetail> findById(String transactionId);
    TransactionPage searchTransactionByUser(SearchTransactionByUserQuery  searchTransactionByUserQuery);
    TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery);
    TransactionSummary getSummary(SummaryQuery summaryQuery);
}
