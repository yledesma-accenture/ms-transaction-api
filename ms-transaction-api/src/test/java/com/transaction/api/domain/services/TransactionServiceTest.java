package com.transaction.api.domain.services;

import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import org.junit.jupiter.api.BeforeEach;
import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.ValidationWarning;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionService service;
    private ITransactionDatabasePort databasePort;
    @BeforeEach
    void setUp() {
        databasePort = mock(ITransactionDatabasePort.class);
        service = new TransactionService(databasePort);

        // Configurar el mock para transactionId
        UUID expectedId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-29T19:35:04.387Z");

        Transaction tx = new Transaction(
                expectedId, "TNX-2024-000001", createdAt, createdAt, "DEBIT", "COMPLETED",
                250, "USD",
                new Party(UUID.randomUUID(), "ACC-00123", "0170099220000067797370", "20329851657", "Alice Martinez", "PERSON", "001", "ACC-00123"),
                new Party(UUID.randomUUID(), "ACC-00123", "0170099220000067797371", "20329851658", "Bob Smith", "PERSON", "001", "0002"),
                "Test transaction", UUID.randomUUID(), "system", true, null
        );

        TransactionDetail detail = new TransactionDetail(
                tx, createdAt, createdAt, List.of(new ValidationWarning(expectedId,"MISSING_DESCRIPTION","MISSING_DESCRIPTION", createdAt))
        );

        when(databasePort.findById(expectedId.toString())).thenReturn(java.util.Optional.of(detail));
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
        assertNotNull(result.content());
        assertEquals(1, result.content().size(), "Service returns one dummy transaction");
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
    }

    @Test
    void shouldReturnSummary() {
        SummaryQuery query = mock(SummaryQuery.class);

        when(query.txDateFrom()).thenReturn(LocalDate.of(2024, Month.JANUARY, 1));
        when(query.txDateTo()).thenReturn(LocalDate.of(2024, Month.JANUARY, 31));
        when(query.ingestionDateFrom()).thenReturn(LocalDate.of(2024, Month.FEBRUARY, 1));
        when(query.ingestionDateTo()).thenReturn(LocalDate.of(2024, Month.FEBRUARY, 29));
        when(query.groupBy()).thenReturn("STATUS");

        TransactionSummary result = service.getSummary(query);

        assertNotNull(result);
    }




    @Test
    void transactionCbuReturnsTransactionPageWhenFound() {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        // Mock transaction data
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");
        Transaction tx = new Transaction(
                txId, "EXT-0001", now, now, "DEBIT", "COMPLETED",
                1000, "ARS",
                new Party(UUID.randomUUID(), "ACC-001", "0170099220000067797370", "20329851657", "Juan Perez", "PERSON", "001", "0001"),
                new Party(UUID.randomUUID(), "ACC-002", "0170099220000067797371", "20329851658", "Maria Gomez", "PERSON", "001", "0002"),
                "Transfer", UUID.randomUUID(), "system", false, null
        );

        TransactionPage expectedPage = new TransactionPage(
                List.of(tx),
                0,
                10,
                1L,
                1,
                true
        );

        when(databasePort.transactionCbu(cbu, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCbu(cbu, query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertTrue(result.last());
        assertEquals(txId, result.content().get(0).id());
        verify(databasePort).transactionCbu(cbu, query);
    }

    @Test
    void transactionCbuThrowsNotFoundWhenEmpty() {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(databasePort.transactionCbu(cbu, query)).thenReturn(java.util.Optional.empty());

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.transactionCbu(cbu, query)
        );

        verify(databasePort).transactionCbu(cbu, query);
    }

    @Test
    void transactionCbuReturnsMultipleTransactions() {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        OffsetDateTime now =  OffsetDateTime.parse("2026-07-07T12:00:00Z");
        List<Transaction> transactions = List.of(
                new Transaction(
                        UUID.randomUUID(), "EXT-0001", now, now, "DEBIT", "COMPLETED",
                        1000, "ARS",
                        new Party(UUID.randomUUID(), "ACC-001", "0170099220000067797370", "20329851657", "Juan", "PERSON", "001", "0001"),
                        new Party(UUID.randomUUID(), "ACC-002", "0170099220000067797371", "20329851658", "Maria", "PERSON", "001", "0002"),
                        "Transfer", UUID.randomUUID(), "system", false, null
                ),
                new Transaction(
                        UUID.randomUUID(), "EXT-0002", now, now, "CREDIT", "PENDING",
                        2000, "ARS",
                        new Party(UUID.randomUUID(), "ACC-003", "0170099220000067797372", "20329851659", "Luis", "PERSON", "001", "0003"),
                        new Party(UUID.randomUUID(), "ACC-004", "0170099220000067797373", "20329851660", "Ana", "PERSON", "001", "0004"),
                        "Payment", UUID.randomUUID(), "system", true, "FLAGGED"
                )
        );

        TransactionPage expectedPage = new TransactionPage(
                transactions,
                0,
                10,
                2L,
                1,
                true
        );

        when(databasePort.transactionCbu(cbu, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCbu(cbu, query);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
        assertEquals("EXT-0001", result.content().get(0).externalRef());
        assertEquals("EXT-0002", result.content().get(1).externalRef());
    }

    @Test
    void transactionCbuWithPaginationData() {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 1, 20, "transactionAt,asc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        OffsetDateTime now =  OffsetDateTime.parse("2026-07-07T12:00:00Z");
        Transaction tx = new Transaction(
                UUID.randomUUID(), "EXT-0003", now, now, "DEBIT", "COMPLETED",
                500, "USD",
                new Party(UUID.randomUUID(), "ACC-005", "0170099220000067797374", "20329851661", "Pedro", "PERSON", "001", "0005"),
                new Party(UUID.randomUUID(), "ACC-006", "0170099220000067797375", "20329851662", "Sofia", "PERSON", "001", "0006"),
                "Deposit", UUID.randomUUID(), "system", false, null
        );

        TransactionPage expectedPage = new TransactionPage(
                List.of(tx),
                1,
                20,
                25L,
                2,
                false
        );

        when(databasePort.transactionCbu(cbu, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCbu(cbu, query);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(2, result.totalPages());
        assertFalse(result.last());
    }

    // ==================== transactionCuit Tests ====================

    @Test
    void transactionCuitReturnsTransactionPageWhenFound() {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        UUID txId = UUID.randomUUID();
        OffsetDateTime now =  OffsetDateTime.parse("2026-07-07T12:00:00Z");
        Transaction tx = new Transaction(
                txId, "EXT-0004", now, now, "DEBIT", "COMPLETED",
                1500, "ARS",
                new Party(UUID.randomUUID(), "ACC-007", "0170099220000067797376", "20329851663", "Carlos", "PERSON", "001", "0007"),
                new Party(UUID.randomUUID(), "ACC-008", "0170099220000067797377", "20329851664", "Elena", "PERSON", "001", "0008"),
                "Purchase", UUID.randomUUID(), "system", false, null
        );

        TransactionPage expectedPage = new TransactionPage(
                List.of(tx),
                0,
                10,
                1L,
                1,
                true
        );

        when(databasePort.transactionCuit(cuit, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCuit(cuit, query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertTrue(result.last());
        assertEquals(txId, result.content().get(0).id());
        verify(databasePort).transactionCuit(cuit, query);
    }

    @Test
    void transactionCuitThrowsNotFoundWhenEmpty() {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(databasePort.transactionCuit(cuit, query)).thenReturn(java.util.Optional.empty());

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.transactionCuit(cuit, query)
        );

        verify(databasePort).transactionCuit(cuit, query);
    }

    @Test
    void transactionCuitReturnsMultipleTransactions() {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 15, "transactionAt,asc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        OffsetDateTime now =  OffsetDateTime.parse("2026-07-07T12:00:00Z");
        List<Transaction> transactions = List.of(
                new Transaction(
                        UUID.randomUUID(), "EXT-0005", now, now, "CREDIT", "COMPLETED",
                        3000, "ARS",
                        new Party(UUID.randomUUID(), "ACC-009", "0170099220000067797378", "20329851665", "Diego", "PERSON", "001", "0009"),
                        new Party(UUID.randomUUID(), "ACC-010", "0170099220000067797379", "20329851666", "Lucia", "PERSON", "001", "0010"),
                        "Refund", UUID.randomUUID(), "system", true, "HIGH_AMOUNT"
                ),
                new Transaction(
                        UUID.randomUUID(), "EXT-0006", now, now, "DEBIT", "COMPLETED",
                        500, "USD",
                        new Party(UUID.randomUUID(), "ACC-011", "0170099220000067797380", "20329851667", "Miguel", "PERSON", "001", "0011"),
                        new Party(UUID.randomUUID(), "ACC-012", "0170099220000067797381", "20329851668", "Rosa", "PERSON", "001", "0012"),
                        "Wire", UUID.randomUUID(), "system", false, null
                ),
                new Transaction(
                        UUID.randomUUID(), "EXT-0007", now, now, "DEBIT", "PENDING",
                        750, "ARS",
                        new Party(UUID.randomUUID(), "ACC-013", "0170099220000067797382", "20329851669", "Jose", "PERSON", "001", "0013"),
                        new Party(UUID.randomUUID(), "ACC-014", "0170099220000067797383", "20329851670", "Paula", "PERSON", "001", "0014"),
                        "Bill Payment", UUID.randomUUID(), "system", false, null
                )
        );

        TransactionPage expectedPage = new TransactionPage(
                transactions,
                0,
                15,
                3L,
                1,
                true
        );

        when(databasePort.transactionCuit(cuit, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCuit(cuit, query);

        assertNotNull(result);
        assertEquals(3, result.content().size());
        assertEquals(3L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
        assertEquals("EXT-0005", result.content().get(0).externalRef());
        assertEquals("EXT-0006", result.content().get(1).externalRef());
        assertEquals("EXT-0007", result.content().get(2).externalRef());
    }

    @Test
    void transactionCuitWithFiltersAndPagination() {
        String cuit = "20401234567";
        LocalDate txDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        FilterCommon filterCommon = new FilterCommon(txDateFrom, txDateTo, null, null, 2, 25, "amount,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        OffsetDateTime now =  OffsetDateTime.parse("2026-07-07T12:00:00Z");
        Transaction tx = new Transaction(
                UUID.randomUUID(), "EXT-0008", now, now, "CREDIT", "COMPLETED",
                5000, "ARS",
                new Party(UUID.randomUUID(), "ACC-015", "0170099220000067797384", "20329851671", "Roberto", "PERSON", "001", "0015"),
                new Party(UUID.randomUUID(), "ACC-016", "0170099220000067797385", "20329851672", "Laura", "PERSON", "001", "0016"),
                "Investment", UUID.randomUUID(), "system", true, "SUSPICIOUS"
        );

        TransactionPage expectedPage = new TransactionPage(
                List.of(tx),
                2,
                25,
                75L,
                3,
                false
        );

        when(databasePort.transactionCuit(cuit, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCuit(cuit, query);

        assertNotNull(result);
        assertEquals(2, result.page());
        assertEquals(25, result.size());
        assertEquals(75L, result.totalElements());
        assertEquals(3, result.totalPages());
        assertFalse(result.last());
        assertEquals("Investment", result.content().get(0).description());
    }

    @Test
    void transactionCuitReturnsEmptyTransactionList() {
        String cuit = "20501234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        TransactionPage expectedPage = new TransactionPage(
                List.of(),
                0,
                10,
                0L,
                0,
                true
        );

        when(databasePort.transactionCuit(cuit, query)).thenReturn(java.util.Optional.of(expectedPage));

        TransactionPage result = service.transactionCuit(cuit, query);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.totalPages());
        assertTrue(result.last());
    }

    @Test
    void transactionIdThrowsNotFoundExceptionWhenTransactionDoesNotExist() {
        String nonExistentId = UUID.randomUUID().toString();

        when(databasePort.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.transactionId(nonExistentId)
        );

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Transacción no encontrada", ex.getReason());
        verify(databasePort).findById(nonExistentId);
    }

    @Test
    void transactionIdWithValidationWarnings() {
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Transaction tx = new Transaction(
                txId, "TNX-2024-000002", now, now, "CREDIT", "COMPLETED",
                5000, "ARS",
                new Party(UUID.randomUUID(), "ACC-00456", "0170099220000067797370", "20329851657", "John Doe", "PERSON", "001", "ACC-00456"),
                new Party(UUID.randomUUID(), "ACC-00789", "0170099220000067797371", "20329851658", "Jane Smith", "PERSON", "001", "0002"),
                "Transfer", UUID.randomUUID(), "system", true, "FLAGGED"
        );

        List<ValidationWarning> warnings = List.of(
                new ValidationWarning(UUID.randomUUID(), "MISSING_DESCRIPTION", "Description is required", now),
                new ValidationWarning(UUID.randomUUID(), "HIGH_AMOUNT", "Amount exceeds limit", now),
                new ValidationWarning(UUID.randomUUID(), "SUSPICIOUS_PATTERN", "Unusual pattern detected", now)
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, warnings);

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertNotNull(result);
        assertNotNull(result.validationWarnings());
        assertEquals(3, result.validationWarnings().size());
        assertEquals("MISSING_DESCRIPTION", result.validationWarnings().get(0).warningCode());
        assertEquals("HIGH_AMOUNT", result.validationWarnings().get(1).warningCode());
        assertEquals("SUSPICIOUS_PATTERN", result.validationWarnings().get(2).warningCode());
        verify(databasePort).findById(txId.toString());
    }

    @Test
    void transactionIdWithNoValidationWarnings() {
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Transaction tx = new Transaction(
                txId, "TNX-2024-000003", now, now, "DEBIT", "COMPLETED",
                100, "USD",
                new Party(UUID.randomUUID(), "ACC-00111", "0170099220000067797370", "20329851657", "Alice", "PERSON", "001", "ACC-00111"),
                new Party(UUID.randomUUID(), "ACC-00222", "0170099220000067797371", "20329851658", "Bob", "PERSON", "001", "0002"),
                "Payment", UUID.randomUUID(), "system", false, null
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertNotNull(result);
        assertNotNull(result.validationWarnings());
        assertTrue(result.validationWarnings().isEmpty());
        verify(databasePort).findById(txId.toString());
    }

    @Test
    void transactionIdVerifiesBenefactorAndBeneficiaryData() {
        UUID txId = UUID.randomUUID();
        UUID benefactorId = UUID.randomUUID();
        UUID beneficiaryId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Party benefactor = new Party(
                benefactorId,
                "ACC-BENF-001",
                "0170099220000067797370",
                "20329851657",
                "Benefactor Name",
                "PERSON",
                "001",
                "0001"
        );

        Party beneficiary = new Party(
                beneficiaryId,
                "ACC-BENE-001",
                "0170099220000067797371",
                "20329851658",
                "Beneficiary Name",
                "PERSON",
                "002",
                "0002"
        );

        Transaction tx = new Transaction(
                txId, "TNX-2024-000004", now, now, "DEBIT", "COMPLETED",
                2500, "ARS",
                benefactor,
                beneficiary,
                "Business Payment", UUID.randomUUID(), "user123", false, null
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertNotNull(result.transaction().benefactor());
        assertEquals("Benefactor Name", result.transaction().benefactor().holderName());
        assertEquals("20329851657", result.transaction().benefactor().cuit());
        assertEquals("0170099220000067797370", result.transaction().benefactor().cbu());
        assertEquals("001", result.transaction().benefactor().bankCode());

        assertNotNull(result.transaction().beneficiary());
        assertEquals("Beneficiary Name", result.transaction().beneficiary().holderName());
        assertEquals("20329851658", result.transaction().beneficiary().cuit());
        assertEquals("0170099220000067797371", result.transaction().beneficiary().cbu());
        assertEquals("002", result.transaction().beneficiary().bankCode());
    }

    @Test
    void transactionIdVerifiesTransactionAmountAndCurrency() {
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Transaction tx = new Transaction(
                txId, "TNX-2024-000005", now, now, "CREDIT", "PENDING",
                99999, "EUR",
                new Party(UUID.randomUUID(), "ACC-001", "0170099220000067797370", "20329851657", "Sender", "PERSON", "001", "0001"),
                new Party(UUID.randomUUID(), "ACC-002", "0170099220000067797371", "20329851658", "Receiver", "PERSON", "001", "0002"),
                "International Transfer", UUID.randomUUID(), "system", true, "HIGH_AMOUNT"
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertEquals(99999, result.transaction().amount());
        assertEquals("EUR", result.transaction().currency());
        assertEquals("CREDIT", result.transaction().type());
        assertEquals("PENDING", result.transaction().status());
        assertTrue(result.transaction().flagged());
    }

    @Test
    void transactionIdVerifiesTransactionMetadata() {
        UUID txId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2024-06-15T10:30:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2024-06-15T14:45:00Z");
        OffsetDateTime txTime = OffsetDateTime.parse("2024-06-15T09:00:00Z");
        OffsetDateTime ingestionTime = OffsetDateTime.parse("2024-06-15T10:00:00Z");

        Transaction tx = new Transaction(
                txId, "TNX-2024-000006", txTime, ingestionTime, "DEBIT", "COMPLETED",
                1000, "ARS",
                new Party(UUID.randomUUID(), "ACC-001", "0170099220000067797370", "20329851657", "User1", "PERSON", "001", "0001"),
                new Party(UUID.randomUUID(), "ACC-002", "0170099220000067797371", "20329851658", "User2", "PERSON", "001", "0002"),
                "Test Transaction", fileId, "admin_user", false, null
        );

        TransactionDetail detail = new TransactionDetail(tx, createdAt, updatedAt, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
        assertEquals(txTime, result.transaction().transactionAt());
        assertEquals(ingestionTime, result.transaction().ingestedAt());
        assertEquals(fileId, result.transaction().fileId());
        assertEquals("admin_user", result.transaction().createdBy());
    }

    @Test
    void transactionIdWithNullBeneficiary() {
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Party benefactor = new Party(
                UUID.randomUUID(),
                "ACC-001",
                "0170099220000067797370",
                "20329851657",
                "Sender Only",
                "PERSON",
                "001",
                "0001"
        );

        Transaction tx = new Transaction(
                txId, "TNX-2024-000007", now, now, "DEBIT", "COMPLETED",
                500, "ARS",
                benefactor,
                null,
                "Withdrawal", UUID.randomUUID(), "system", false, null
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        TransactionDetail result = service.transactionId(txId.toString());

        assertNotNull(result.transaction().benefactor());
        assertNull(result.transaction().beneficiary());
    }

    @Test
    void transactionIdCallsDatabasePortFindByIdOnce() {
        UUID txId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        Transaction tx = new Transaction(
                txId, "TNX-2024-000008", now, now, "DEBIT", "COMPLETED",
                1000, "ARS",
                new Party(UUID.randomUUID(), "ACC-001", "0170099220000067797370", "20329851657", "User", "PERSON", "001", "0001"),
                new Party(UUID.randomUUID(), "ACC-002", "0170099220000067797371", "20329851658", "Recipient", "PERSON", "001", "0002"),
                "Payment", UUID.randomUUID(), "system", false, null
        );

        TransactionDetail detail = new TransactionDetail(tx, now, now, List.of());

        when(databasePort.findById(txId.toString())).thenReturn(java.util.Optional.of(detail));

        service.transactionId(txId.toString());

        verify(databasePort, times(1)).findById(txId.toString());
    }

}
