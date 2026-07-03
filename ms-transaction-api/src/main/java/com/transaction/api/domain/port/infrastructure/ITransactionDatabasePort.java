package com.transaction.api.domain.port.infrastructure;

import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;

import java.util.Optional;

public interface ITransactionDatabasePort {

    public Optional<TransactionDetail> findById(String transactionId);
    TransactionPage searchTransactionByUser(SearchTransactionByUserQuery  searchTransactionByUserQuery);
}
