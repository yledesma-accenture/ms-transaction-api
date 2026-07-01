package com.transaction.api.domain.services;

import com.transaction.api.adapters.inbound.rest.TransactionQueryMapper;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService();
    }

    @Test
    void transactionIdReturnsDetailWithExpectedFields() {
        UUID expectedId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

        TransactionDetail detail = service.transactionId(expectedId.toString());
        assertNotNull(detail, "TransactionDetail should not be null");

        Transaction tx = detail.transaction();
        assertNotNull(tx, "Transaction should not be null");
        assertEquals(expectedId, tx.id());
        assertEquals("TNX-2024-000001", tx.externalRef());
        assertEquals(250, tx.amount());
        assertEquals("USD", tx.currency());
        assertEquals("COMPLETED", tx.status());
        assertTrue(tx.flagged());
        assertEquals("Alice Martinez", tx.benefactor().holderName());
        assertEquals("ACC-00123", tx.benefactor().accountNumber());

        // createdAt / updatedAt at TransactionDetail level
        OffsetDateTime expectedCreated = OffsetDateTime.parse("2026-06-29T19:35:04.387Z");
        assertEquals(expectedCreated, detail.createdAt());
        assertEquals(expectedCreated, detail.updatedAt());

        // validation warnings
        List<ValidationWarning> warnings = detail.validationWarnings();
        assertNotNull(warnings);
        assertEquals(1, warnings.size());
        assertEquals("MISSING_DESCRIPTION", warnings.get(0).warningCode());
    }

    @Test
    void shouldSearchTransactionByUser() {
        FilterCommon filterCommon = mock(FilterCommon.class);
        SearchTransactionByUserQuery query = mock(SearchTransactionByUserQuery.class);

        when(query.filterCommon()).thenReturn(filterCommon);
        when(filterCommon.page()).thenReturn(0);
        when(filterCommon.size()).thenReturn(10);

        TransactionPage result = service.searchTransactionByUser(query);

        assertNotNull(result);
    }

    @Test
    void shouldReturnListTransaction() {
        FilterCommon filterCommon = mock(FilterCommon.class);
        ListTransactionsQuery query = mock(ListTransactionsQuery.class);

        when(query.filterCommon()).thenReturn(filterCommon);
        when(filterCommon.page()).thenReturn(0);
        when(filterCommon.size()).thenReturn(10);

        TransactionPage result = service.listTransaction(query);

        assertNotNull(result);
    }

    @Test
    void shouldReturnSummary() {
        SummaryQuery query = mock(SummaryQuery.class);

        when(query.txDateFrom()).thenReturn(LocalDate.of(2024, 1, 1));
        when(query.txDateTo()).thenReturn(LocalDate.of(2024, 1, 31));
        when(query.ingestionDateFrom()).thenReturn(LocalDate.of(2024, 2, 1));
        when(query.ingestionDateTo()).thenReturn(LocalDate.of(2024, 2, 29));
        when(query.groupBy()).thenReturn("STATUS");

        TransactionSummary result = service.getSummary(query);

        assertNotNull(result);
    }

    @Test
    void shouldHandleNullQueryInGetSummary() {
        assertThrows(NullPointerException.class, () -> service.getSummary(null));
    }


}
