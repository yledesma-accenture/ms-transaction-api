package com.transaction.api.domain.port.application;

import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.TransactionSummary;



public interface ITransactionPort {
    TransactionDetail transactionId(String transactionId);
    TransactionPage searchTransactionByUser(SearchTransactionByUserQuery searchTransactionByUserQuery);
    TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery);
    TransactionSummary getSummary(SummaryQuery summaryQuery);


    TransactionPage transactionCbu(String cbu, SearchTransactionByCbuQuery searchTransactionByCbuQuery);


    TransactionPage transactionCuit(String cuit, SearchTransactionByCuitQuery searchTransactionByCuitQuery);

}
