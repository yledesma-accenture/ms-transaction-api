package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import org.junit.jupiter.api.BeforeEach;
import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.ValidationWarning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService service;

    @Mock
    private ITransactionDatabasePort transactionDatabasePort;

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
        SearchTransactionByUserQuery query = mock(SearchTransactionByUserQuery.class);

        TransactionPage expected = new TransactionPage(
                List.of(), 0, 10, 0, 0, true
        );

        when(transactionDatabasePort.searchTransactionByUser(query)).thenReturn(expected);

        TransactionPage result = service.searchTransactionByUser(query);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(transactionDatabasePort).searchTransactionByUser(query);
    }

    @Test
    void shouldReturnListTransaction() {
        ListTransactionsQuery query = mock(ListTransactionsQuery.class);

        TransactionPage expected = new TransactionPage(
                List.of(), 0, 10, 0, 0, true
        );

        when(transactionDatabasePort.listTransaction(query)).thenReturn(expected);

        TransactionPage result = service.listTransaction(query);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(transactionDatabasePort).listTransaction(query);
    }

    @Test
    void shouldReturnSummary() {
        SummaryQuery query = mock(SummaryQuery.class);

        TransactionSummary expected = new TransactionSummary(null, null, null, null,
                0, BigDecimal.ZERO, "type", List.of());

        when(transactionDatabasePort.getSummary(query)).thenReturn(expected);

        TransactionSummary result = service.getSummary(query);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(transactionDatabasePort).getSummary(query);
    }

    //@Test
    void transactionCuitReturnsPageWithContent() {
        TransactionPage page = service.transactionCuit(
                "20301234567",
                null,
                null,
                null,
                null,
                0,
                5,
                "transactionAt,desc"
        );


        assertThrows(NullPointerException.class, () -> service.getSummary(null));
    }


}
