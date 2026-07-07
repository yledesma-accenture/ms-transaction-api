package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(TransactionDatabaseAdapter.class)
class TransactionDatabaseAdapterTest {
    @Autowired
    private TransactionDatabaseAdapter adapter;

    @Test
    void shouldSearchTransactionByUser() {
        SearchTransactionByUserQuery query =
                new SearchTransactionByUserQuery("system", new FilterCommon(null, null, null, null,0,10, null));

        TransactionPage result = adapter.searchTransactionByUser(query);

        assertNotNull(result);
        assertEquals(42, result.totalElements());
        assertEquals(10, result.content().size());
        assertEquals(5, result.totalPages());
        assertFalse(result.last());

        Transaction tx = result.content().get(0);
        assertEquals("EXT-0013", tx.externalRef());
        assertNotNull(tx.benefactor());
        assertNotNull(tx.beneficiary());
        assertEquals("Servicios Globales SRL", tx.benefactor().holderName());
        assertEquals("Tech Solutions SA", tx.beneficiary().holderName());
    }

    @Test
    void shouldSearchTransactionByUserAndTxDateFromFilter() {
        SearchTransactionByUserQuery query =
                new SearchTransactionByUserQuery("admin", new FilterCommon(null, LocalDate.of(2026, 04, 07), null, null,0,10, null));

        TransactionPage result = adapter.searchTransactionByUser(query);

        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals(2, result.content().size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());

        Transaction tx = result.content().get(0);
        assertEquals("EXT-0007", tx.externalRef());
        assertNotNull(tx.benefactor());
        assertNotNull(tx.beneficiary());
        assertEquals("Maria Gomez", tx.benefactor().holderName());
        assertEquals("Tech Solutions SA", tx.beneficiary().holderName());
    }

    @Test
    void shouldListTransactionsWithoutFilters() {
        ListTransactionsQuery query = new ListTransactionsQuery(
                new FilterCommon(null, null, null, null, 0, 20, null), // transactionType
                null, // status
                null, // currency
                null,// createdBy
                null,
                null,
                null,
                null
        );

        TransactionPage result = adapter.listTransaction(query);

        assertNotNull(result);
        assertEquals(45, result.totalElements());
        assertEquals(20, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(3, result.totalPages());
        assertFalse(result.last());
    }

    @Test
    void shouldListTransactionsWithStatusFilter() {
        ListTransactionsQuery query = new ListTransactionsQuery(
                new FilterCommon(null, null, null, null, 0, 20, null),
                null,// transactionType
                TransactionStatus.PENDING, // status
                null, // currency
                null,// createdBy
                null,
                null,
                null
        );

        TransactionPage result = adapter.listTransaction(query);

        assertNotNull(result);
        assertEquals(8, result.totalElements());
        assertEquals(8, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    @Test
    void shouldListTransactionsWithStatusAndTypeFilter() {
        ListTransactionsQuery query = new ListTransactionsQuery(
                new FilterCommon(null, null, null, null, 0, 20, null),
                TransactionType.PAYMENT,// transactionType
                TransactionStatus.COMPLETED, // status
                null, // currency
                null,// createdBy
                null,
                null,
                null
        );

        TransactionPage result = adapter.listTransaction(query);

        assertNotNull(result);
        assertEquals(7, result.totalElements());
        assertEquals(7, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    @Test
    void shouldListTransactionsWithCurrencyFilter() {
        ListTransactionsQuery query = new ListTransactionsQuery(
                new FilterCommon(null, null, null, null, 0, 20, null),
                null,// transactionType
                null, // status
                "USD", // currency
                null,// createdBy
                null,
                null,
                null
        );

        TransactionPage result = adapter.listTransaction(query);

        assertNotNull(result);
        assertEquals(4, result.totalElements());
        assertEquals(4, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    @Test
    void shouldListTransactionsWithDateFilter() {
        ListTransactionsQuery query = new ListTransactionsQuery(
                new FilterCommon(LocalDate.of(2026, 01, 04), null, null, LocalDate.of(2026, 01, 07), 0, 20, null),
                null,// transactionType
                null, // status
                null, // currency
                null,// createdBy
                null,
                null,
                null
        );

        TransactionPage result = adapter.listTransaction(query);

        assertNotNull(result);
        assertEquals(4, result.totalElements());
        assertEquals(4, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    @Test
    void shouldGetSummaryGroupedByStatusWithoutFilters() {
        SummaryQuery query = new SummaryQuery(
                null,
                null,
                null,
                null,
                "status"
        );

        TransactionSummary result = adapter.getSummary(query);

        assertNotNull(result);
        assertEquals("status", result.groupedBy());
        assertEquals(45, result.totalCount());
        assertNotNull(result.totalAmount());
        assertNotNull(result.groups());
        assertFalse(result.groups().isEmpty());

        TransactionSummaryGroup completed = result.groups().stream()
                .filter(group -> "COMPLETED".equals(group.key()))
                .findFirst()
                .orElseThrow();

        assertEquals(33, completed.count());
    }

    @Test
    void shouldGetSummaryGroupedByStatusWithDateTxFilter() {
        SummaryQuery query = new SummaryQuery(
                LocalDate.of(2026, 01, 04),
                LocalDate.of(2026, 01, 07),
                null,
                null,
                "status"
        );

        TransactionSummary result = adapter.getSummary(query);

        assertNotNull(result);
        assertEquals("status", result.groupedBy());
        assertEquals(4, result.totalCount());
        assertNotNull(result.totalAmount());
        assertNotNull(result.groups());
        assertFalse(result.groups().isEmpty());

        TransactionSummaryGroup completed = result.groups().stream()
                .filter(group -> "COMPLETED".equals(group.key()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, completed.count());
    }
}
