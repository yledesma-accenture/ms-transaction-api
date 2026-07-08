package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.inbound.dto.TransactionFilterRequest;
import com.transaction.api.adapters.inbound.rest.validator.RequestValidator;
import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class TransactionQueryMapper {
    private final RequestValidator requestValidator;

    public TransactionQueryMapper(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    public SearchTransactionByUserQuery toSearchTransactionByUserQuery(String userId, TransactionFilterRequest request) {
        return SearchTransactionByUserQuery.builder()
                .userId(userId)
                .filterCommon(toTransactionFilterCommon(request))
                .build();
    }

    public SearchTransactionByCbuQuery toSearchTransactionByCbuQuery(String cbu, TransactionFilterRequest request) {
        return SearchTransactionByCbuQuery.builder()
                .cbu(cbu)
                .filterCommon(toTransactionFilterCommon(request))
                .build();
    }

    public SearchTransactionByCuitQuery toSearchTransactionByCuitQuery(String cuit, TransactionFilterRequest request) {
        return SearchTransactionByCuitQuery.builder()
                .cuit(cuit)
                .filterCommon(toTransactionFilterCommon(request))
                .build();
    }

    public ListTransactionsQuery toListTransactionQuery(ListTransactionRequest request){
        requestValidator.validate(request.txDateFrom(), request.txDateTo(), request.ingestionDateFrom(), request.ingestionDateTo(), request.type(), request.status());
        TransactionFilterRequest filterCommon = new TransactionFilterRequest(
                request.txDateFrom(),
                request.txDateTo(),
                request.ingestionDateFrom(),
                request.ingestionDateTo(),
                request.page(),
                request.size(),
                request.sort()
        );
        return ListTransactionsQuery.builder()
                .filterCommon(toTransactionFilterCommon(filterCommon))
                .transactionType(request.type() != null ? TransactionType.valueOf(request.type()) : null)
                .transactionStatus(request.status() != null ? TransactionStatus.valueOf(request.status()) : null)
                .currency(request.currency())
                .amountMin(request.amountMin())
                .amountMax(request.amountMax())
                .fileId(request.fileId())
                .flagged(request.flagged())
                .build();
    }

    public SummaryQuery toSummaryQuery(SummaryRequest request){
        requestValidator.validate(request.txDateFrom(), request.txDateTo(), request.ingestionDateFrom(), request.ingestionDateTo(),null, null);
        return SummaryQuery.builder()
                .txDateFrom(request.txDateFrom())
                .txDateTo(request.txDateTo())
                .ingestionDateFrom(request.ingestionDateFrom())
                .ingestionDateTo(request.ingestionDateTo())
                .groupBy(request.groupBy() != null ? request.groupBy() : "type")
                .build();
    }

    private FilterCommon toTransactionFilterCommon(TransactionFilterRequest request) {
        requestValidator.validate(request.txDateFrom(), request.txDateTo(), request.ingestionDateFrom(), request.ingestionDateTo(),null, null);
        return FilterCommon.builder()
                .txDateFrom(request.txDateFrom())
                .txDateTo(request.txDateTo())
                .ingestionDateFrom(request.ingestionDateFrom())
                .ingestionDateTo(request.ingestionDateTo())
                .page(request.page() != null ? request.page() : 0)
                .size(request.size() != null ? request.size() : 20)
                .sort(request.sort() != null ? request.sort() : "transaction_At,desc")
                .build();
    }
}
