package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionPage;
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
                new SearchTransactionByUserQuery("system", new FilterCommon(LocalDate.now(), LocalDate.now(), LocalDate.now(), LocalDate.now(),0,10, "asc"));

        TransactionPage result = adapter.searchTransactionByUser(query);

        assertNotNull(result);
        assertEquals(5, result.totalElements());
        assertEquals(5, result.content().size());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());

        Transaction tx = result.content().get(0);
        assertEquals("EXT-0001", tx.externalRef());
        assertNotNull(tx.benefactor());
        assertNotNull(tx.beneficiary());
        assertEquals("Juan Perez", tx.benefactor().holderName());
        assertEquals("Maria Gomez", tx.beneficiary().holderName());
    }
}
