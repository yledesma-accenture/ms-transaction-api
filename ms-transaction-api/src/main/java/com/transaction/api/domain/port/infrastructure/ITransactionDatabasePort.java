package com.transaction.api.domain.port.infrastructure;

import com.transaction.api.adapters.model.SearchTransactionByCbuQuery;
import com.transaction.api.adapters.model.SearchTransactionByCuitQuery;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;

import java.util.Optional;

public interface ITransactionDatabasePort {

    Optional<TransactionDetail> findById(String transactionId);

    Optional<TransactionPage> transactionCbu(String cbu, SearchTransactionByCbuQuery searchTransactionByCbuQuery);

    Optional<TransactionPage> transactionCuit(String cuit,  SearchTransactionByCuitQuery searchTransactionByCuitQuery);

    }
