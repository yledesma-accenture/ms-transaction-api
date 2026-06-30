package com.transaction.api.domain.services;

import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.ValidationWarning;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {


    private final TransactionService service = new TransactionService();

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
    void transactionCbuReturnsPageWithContent() {
        TransactionPage page = service.transactionCbu(
                "0000003100012345678901",
                null,
                null,
                null,
                null,
                0,
                10,
                "transactionAt,desc"
        );

        assertNotNull(page);
        assertNotNull(page.content());
        assertEquals(2, page.content().size(), "Service mock adds two transactions");
        assertEquals(2L, page.totalElements());
        assertEquals(1, page.totalPages());
        assertFalse(page.last());
        assertEquals(0, page.page());
        assertEquals(10, page.size());
    }

    @Test
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

        assertNotNull(page);
        assertEquals(2, page.content().size());
        assertEquals(2L, page.totalElements());
    }
}
