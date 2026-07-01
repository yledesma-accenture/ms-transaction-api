package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.inbound.dto.TransactionFilterRequest;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TransactionQueryMapper {
    public SearchTransactionByUserQuery toSearchTransactionByUserQuery(String userId, TransactionFilterRequest request) {
        return SearchTransactionByUserQuery.builder()
                .userId(Long.valueOf(userId))
                .filterCommon(toTransactionFilterCommon(request))
                .build();
    }

    public ListTransactionsQuery toListTransactionQuery(ListTransactionRequest request){
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
                .transactionType(mapTransactionType(request.type()))
                .transactionStatus(mapTransactionStatus(request.status()))
                .currency(request.currency())
                .amountMin(request.amountMin())
                .amountMax(request.amountMax())
                .fileId(request.fileId())
                .flagged(request.flagged())
                .build();
    }

    public SummaryQuery toSummaryQuery(SummaryRequest request){
        return SummaryQuery.builder()
                .txDateFrom(request.txDateFrom())
                .txDateTo(request.txDateTo())
                .ingestionDateFrom(request.ingestionDateFrom())
                .ingestionDateTo(request.ingestionDateTo())
                .groupBy(request.groupBy() != null ? request.groupBy() : "type")
                .build();
    }

    private FilterCommon toTransactionFilterCommon(TransactionFilterRequest request) {
        return FilterCommon.builder()
                .txDateFrom(request.txDateFrom())
                .txDateTo(request.txDateTo())
                .ingestionDateFrom(request.ingestionDateFrom())
                .ingestionDateTo(request.ingestionDateTo())
                .page(request.page() != null ? request.page() : 0)
                .size(request.size() != null ? request.size() : 20)
                .sort(request.sort() != null ? request.sort() : "transactionAt,desc")
                .build();
    }

    private TransactionStatus mapTransactionStatus(String status){
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TransactionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "transactionStatus inválido: " + status
            );
        }
    }

    private TransactionType mapTransactionType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "transactionType inválido: " + type
            );
        }
    }
}
