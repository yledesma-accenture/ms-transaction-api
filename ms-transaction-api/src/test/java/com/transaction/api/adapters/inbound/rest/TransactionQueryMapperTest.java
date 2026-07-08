package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.inbound.dto.TransactionFilterRequest;
import com.transaction.api.adapters.inbound.rest.validator.RequestValidator;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.TransactionStatus;
import com.transaction.api.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionQueryMapperTest {
    @Mock
    private RequestValidator requestValidator;

    private TransactionQueryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionQueryMapper(requestValidator);
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

        verify(requestValidator).validate(null, null, null, null, request.type(), request.status());

        assertEquals(TransactionType.DEBIT, result.transactionType());
        assertEquals(TransactionStatus.PENDING, result.transactionStatus());
        assertEquals("USD", result.currency());
        assertEquals(BigDecimal.ONE, result.amountMin());
        assertEquals(BigDecimal.TEN, result.amountMax());
        assertEquals(0, result.filterCommon().page());
        assertEquals(20, result.filterCommon().size());
        assertEquals("transactionAt,desc", result.filterCommon().sort());
        assertEquals(true, result.flagged());
    }

    @Test
    void shouldUseDefaultValuesInSummaryQuery() {
        SummaryRequest request = new SummaryRequest(null, null, null, null, null);

        SummaryQuery result = mapper.toSummaryQuery(request);
        verify(requestValidator).validate(null, null, null, null, null, null);

        assertEquals("type", result.groupBy());
    }

    @Test
    void shouldMapSummaryQueryWithProvidedGroupBy() {
        SummaryRequest request = new SummaryRequest(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 28),
                "status"
        );

        SummaryQuery result = mapper.toSummaryQuery(request);

        verify(requestValidator).validate(
                request.txDateFrom(),
                request.txDateTo(),
                request.ingestionDateFrom(),
                request.ingestionDateTo(),
                null,
                null
        );

        assertEquals("status", result.groupBy());
        assertEquals(request.txDateFrom(), result.txDateFrom());
        assertEquals(request.txDateTo(), result.txDateTo());
        assertEquals(request.ingestionDateFrom(), result.ingestionDateFrom());
        assertEquals(request.ingestionDateTo(), result.ingestionDateTo());
    }

    @Test
    void toSearchTransactionByUserQueryWithValidUserId() {
        String userId = "12345";
        LocalDate txDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.DECEMBER, 31);

        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo,
                1, 15, "transactionAt,asc"
        );

        var result = mapper.toSearchTransactionByUserQuery(userId, request);

        verify(requestValidator).validate(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo, null, null
        );

        assertNotNull(result);
        assertEquals(String.valueOf(12345), result.userId());
        assertNotNull(result.filterCommon());
        assertEquals(1, result.filterCommon().page());
        assertEquals(15, result.filterCommon().size());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertEquals(txDateTo, result.filterCommon().txDateTo());
        assertEquals("transactionAt,asc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByUserQueryWithDefaultFilterValues() {
        String userId = "999";
        TransactionFilterRequest request = new TransactionFilterRequest(null, null, null, null, null, null, null);

        var result = mapper.toSearchTransactionByUserQuery(userId, request);

        verify(requestValidator).validate(null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(String.valueOf(999), result.userId());
        assertEquals(0, result.filterCommon().page());
        assertEquals(20, result.filterCommon().size());
        assertEquals("transaction_At,desc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByUserQueryWithPartialFilters() {
        String userId = "777";
        LocalDate txDateFrom = LocalDate.of(2024, Month.MARCH, 1);
        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, null, null, null,
                2, 25, "id,desc"
        );

        var result = mapper.toSearchTransactionByUserQuery(userId, request);

        verify(requestValidator).validate(txDateFrom, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(String.valueOf(777), result.userId());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertNull(result.filterCommon().txDateTo());
        assertEquals(2, result.filterCommon().page());
        assertEquals(25, result.filterCommon().size());
    }

    @Test
    void toSearchTransactionByCbuQueryWithValidCbu() {
        String cbu = "0000003100012345678901";
        LocalDate txDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.DECEMBER, 31);

        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo,
                0, 50, "amount,desc"
        );

        var result = mapper.toSearchTransactionByCbuQuery(cbu, request);

        verify(requestValidator).validate(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo, null, null
        );

        assertNotNull(result);
        assertEquals(cbu, result.cbu());
        assertNotNull(result.filterCommon());
        assertEquals(0, result.filterCommon().page());
        assertEquals(50, result.filterCommon().size());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertEquals(txDateTo, result.filterCommon().txDateTo());
        assertEquals(ingestionDateFrom, result.filterCommon().ingestionDateFrom());
        assertEquals(ingestionDateTo, result.filterCommon().ingestionDateTo());
        assertEquals("amount,desc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByCbuQueryWithDefaultValues() {
        String cbu = "0000001111111111111111";
        TransactionFilterRequest request = new TransactionFilterRequest(null, null, null, null, null, null, null);

        var result = mapper.toSearchTransactionByCbuQuery(cbu, request);

        verify(requestValidator).validate(null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(cbu, result.cbu());
        assertEquals(0, result.filterCommon().page());
        assertEquals(20, result.filterCommon().size());
        assertEquals("transaction_At,desc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByCbuQueryWithOnlyTxDates() {
        String cbu = "0000002222222222222222";
        LocalDate txDateFrom = LocalDate.of(2024, Month.FEBRUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.FEBRUARY, 28);
        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, txDateTo, null, null,
                1, 30, "status,asc"
        );

        var result = mapper.toSearchTransactionByCbuQuery(cbu, request);

        verify(requestValidator).validate(txDateFrom, txDateTo, null, null, null, null);

        assertNotNull(result);
        assertEquals(cbu, result.cbu());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertEquals(txDateTo, result.filterCommon().txDateTo());
        assertNull(result.filterCommon().ingestionDateFrom());
        assertNull(result.filterCommon().ingestionDateTo());
        assertEquals(1, result.filterCommon().page());
        assertEquals(30, result.filterCommon().size());
    }

    @Test
    void toSearchTransactionByCbuQueryWithOnlyIngestionDates() {
        String cbu = "0000003333333333333333";
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.MARCH, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.MARCH, 31);
        TransactionFilterRequest request = new TransactionFilterRequest(
                null, null, ingestionDateFrom, ingestionDateTo,
                0, 10, null
        );

        var result = mapper.toSearchTransactionByCbuQuery(cbu, request);

        assertNotNull(result);
        assertEquals(cbu, result.cbu());
        assertNull(result.filterCommon().txDateFrom());
        assertNull(result.filterCommon().txDateTo());
        assertEquals(ingestionDateFrom, result.filterCommon().ingestionDateFrom());
        assertEquals(ingestionDateTo, result.filterCommon().ingestionDateTo());
        assertEquals("transaction_At,desc", result.filterCommon().sort());
    }


    @Test
    void toSearchTransactionByCuitQueryWithValidCuit() {
        String cuit = "20301234567";
        LocalDate txDateFrom = LocalDate.of(2024, Month.APRIL, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.APRIL, 30);
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.APRIL, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.APRIL, 30);

        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo,
                2, 40, "type,desc"
        );

        var result = mapper.toSearchTransactionByCuitQuery(cuit, request);

        assertNotNull(result);
        assertEquals(cuit, result.cuit());
        assertNotNull(result.filterCommon());
        assertEquals(2, result.filterCommon().page());
        assertEquals(40, result.filterCommon().size());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertEquals(txDateTo, result.filterCommon().txDateTo());
        assertEquals(ingestionDateFrom, result.filterCommon().ingestionDateFrom());
        assertEquals(ingestionDateTo, result.filterCommon().ingestionDateTo());
        assertEquals("type,desc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByCuitQueryWithDefaultValues() {
        String cuit = "27654321098";
        TransactionFilterRequest request = new TransactionFilterRequest(null, null, null, null, null, null, null);

        var result = mapper.toSearchTransactionByCuitQuery(cuit, request);

        assertNotNull(result);
        assertEquals(cuit, result.cuit());
        assertEquals(0, result.filterCommon().page());
        assertEquals(20, result.filterCommon().size());
        assertEquals("transaction_At,desc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByCuitQueryWithMaxPagination() {
        String cuit = "20401234567";
        TransactionFilterRequest request = new TransactionFilterRequest(
                null, null, null, null,
                100, 500, "currency,asc"
        );

        var result = mapper.toSearchTransactionByCuitQuery(cuit, request);

        assertNotNull(result);
        assertEquals(cuit, result.cuit());
        assertEquals(100, result.filterCommon().page());
        assertEquals(500, result.filterCommon().size());
        assertEquals("currency,asc", result.filterCommon().sort());
    }

    @Test
    void toSearchTransactionByCuitQueryWithAllFiltersAndPagination() {
        String cuit = "20501234567";
        LocalDate txDateFrom = LocalDate.of(2024, Month.MAY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.MAY, 31);
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.MAY, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.MAY, 31);

        TransactionFilterRequest request = new TransactionFilterRequest(
                txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo,
                5, 35, "flagged,desc"
        );

        var result = mapper.toSearchTransactionByCuitQuery(cuit, request);

        assertNotNull(result);
        assertEquals(cuit, result.cuit());
        assertEquals(txDateFrom, result.filterCommon().txDateFrom());
        assertEquals(txDateTo, result.filterCommon().txDateTo());
        assertEquals(ingestionDateFrom, result.filterCommon().ingestionDateFrom());
        assertEquals(ingestionDateTo, result.filterCommon().ingestionDateTo());
        assertEquals(5, result.filterCommon().page());
        assertEquals(35, result.filterCommon().size());
        assertEquals("flagged,desc", result.filterCommon().sort());
    }
}
