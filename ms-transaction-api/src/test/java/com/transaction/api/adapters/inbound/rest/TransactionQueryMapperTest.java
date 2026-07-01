package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionQueryMapperTest {
    private TransactionQueryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionQueryMapper();
    }

    @Test
    void shouldMapListTransactionQuery() {
        ListTransactionRequest request = new ListTransactionRequest(
                null, null, null, null,
                "DEBIT", "PENDING", "USD",
                BigDecimal.ONE, BigDecimal.TEN, UUID.randomUUID(), true,
                0, 20, "transactionAt,desc"
        );

        ListTransactionsQuery result = mapper.toListTransactionQuery(request);

        assertEquals(TransactionType.DEBIT, result.transactionType());
        assertEquals(TransactionStatus.PENDING, result.transactionStatus());
        assertEquals("USD", result.currency());
        assertEquals(BigDecimal.ONE, result.amountMin());
        assertEquals(BigDecimal.TEN, result.amountMax());
        assertEquals(true, result.flagged());
    }

    @Test
    void shouldReturnBadRequestWhenTransactionStatusIsInvalid() {
        ListTransactionRequest request = new ListTransactionRequest(
                null, null, null, null,
                "DEBIT", "INVALID_STATUS", "USD",
                BigDecimal.ONE, BigDecimal.TEN, UUID.randomUUID(), true,
                0, 20, "transactionAt,desc"
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> mapper.toListTransactionQuery(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("transactionStatus inválido"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionTypeIsInvalid() {
        ListTransactionRequest request = new ListTransactionRequest(
                null, null, null, null,
                "INVALID_TYPE", "INVALID_STATUS", "USD",
                BigDecimal.ONE, BigDecimal.TEN, UUID.randomUUID(), true,
                0, 20, "transactionAt,desc"
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> mapper.toListTransactionQuery(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("transactionType inválido"));
    }

    @Test
    void shouldUseDefaultValuesInSummaryQuery() {
        SummaryRequest request = new SummaryRequest(null, null, null, null, null);

        SummaryQuery result = mapper.toSummaryQuery(request);

        assertEquals("type", result.groupBy());
    }
}
