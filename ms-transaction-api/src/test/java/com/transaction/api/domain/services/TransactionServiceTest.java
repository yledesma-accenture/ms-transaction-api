package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.TransactionSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService();
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
