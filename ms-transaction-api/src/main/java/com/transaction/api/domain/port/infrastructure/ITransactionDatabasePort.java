package com.transaction.api.domain.port.infrastructure;

import com.transaction.api.domain.model.TransactionDetail;

import java.util.Optional;

public interface ITransactionDatabasePort {

    public Optional<TransactionDetail> findById(String transactionId);
}
