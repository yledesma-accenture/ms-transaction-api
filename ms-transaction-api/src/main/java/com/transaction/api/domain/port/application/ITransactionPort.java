package com.transaction.api.domain.port.application;

import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;


import java.time.LocalDate;


public interface ITransactionPort {
    TransactionDetail transactionId(String transactionId);


    TransactionPage transactionCbu(String cbu, LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom,
                                       LocalDate ingestionDateTo, int page, int size, String sort);


    TransactionPage transactionCuit(String cuit, LocalDate txDateFrom, LocalDate txDateTo, LocalDate ingestionDateFrom,
                     LocalDate ingestionDateTo, int page, int size, String sort);

}
