package com.transaction.api.domain.port.application;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.TransactionSummary;


import java.time.LocalDate;


public interface ITransactionPort {
    TransactionDetail transactionId(String transactionId);
    TransactionPage searchTransactionByUser(SearchTransactionByUserQuery searchTransactionByUserQuery);
    TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery);
    TransactionSummary getSummary(SummaryQuery summaryQuery);


    TransactionPage transactionCbu(String cbu, LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom,
                                       LocalDate ingestionDateTo, int page, int size, String sort);


    TransactionPage transactionCuit(String cuit, LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom,
                     LocalDate ingestionDateTo, int page, int size, String sort);

}
