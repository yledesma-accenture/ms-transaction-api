package com.transaction.api.domain.port.application;

import com.transaction.api.domain.model.TransactionDetail;

public interface ITransactionPort {
    TransactionDetail transactionId(String transactionId);
}
