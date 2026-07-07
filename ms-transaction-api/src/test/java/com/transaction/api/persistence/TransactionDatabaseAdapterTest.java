package com.transaction.api.persistence;


import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SearchTransactionByCbuQuery;
import com.transaction.api.adapters.model.SearchTransactionByCuitQuery;
import com.transaction.api.adapters.outbound.persistence.TransactionDatabaseAdapter;
import com.transaction.api.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionDatabaseAdapterTest {
    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private TransactionDatabaseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TransactionDatabaseAdapter(dataSource);
    }


    @Test
    void findByIdReturnsTransactionDetail() throws SQLException {
        String transactionId = UUID.randomUUID().toString();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockTransactionRow();

        Optional<TransactionDetail> result = adapter.findById(transactionId);

        assertTrue(result.isPresent());
        assertNotNull(result.get().transaction());
        verify(dataSource).getConnection();
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() throws SQLException {
        String transactionId = UUID.randomUUID().toString();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<TransactionDetail> result = adapter.findById(transactionId);

        assertFalse(result.isPresent());
        verify(dataSource).getConnection();
    }

    @Test
    void findByIdHandlesSQLException() throws SQLException {
        String transactionId = UUID.randomUUID().toString();

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        Optional<TransactionDetail> result = adapter.findById(transactionId);

        assertFalse(result.isPresent());
        verify(dataSource).getConnection();
    }

    @Test
    void findByIdReturnsTransactionDetailWithValidationWarnings() throws SQLException {
        String transactionId = UUID.randomUUID().toString();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockTransactionRow();

        Optional<TransactionDetail> result = adapter.findById(transactionId);

        assertTrue(result.isPresent());
        assertNotNull(result.get().validationWarnings());
    }



    // ==================== transactionCbu Tests ====================

    @Test
    void transactionCbuReturnsTransactionPage() throws SQLException {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong(1)).thenReturn(2L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCbu(cbu, query);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().page());
        assertEquals(10, result.get().size());
        verify(dataSource).getConnection();
    }

    @Test
    void transactionCbuReturnsEmptyPageWhenNoResults() throws SQLException {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<TransactionPage> result = adapter.transactionCbu(cbu, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().content().isEmpty());
        assertTrue(result.get().last());
        assertEquals(0, result.get().totalElements());
    }

    @Test
    void transactionCbuHandlesSQLException() throws SQLException {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        Optional<TransactionPage> result = adapter.transactionCbu(cbu, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().content().isEmpty());
        verify(dataSource).getConnection();
    }

    @Test
    void transactionCbuReturnsLastPageCorrectly() throws SQLException {
        String cbu = "0000003100012345678901";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 1, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        lenient().when(resultSet.getLong(1)).thenReturn(15L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCbu(cbu, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().last());
        assertEquals(2, result.get().totalPages());
    }

    @Test
    void transactionCbuWithFilters() throws SQLException {
        String cbu = "0000003100012345678901";
        LocalDate txDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        FilterCommon filterCommon = new FilterCommon(txDateFrom, txDateTo, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCbuQuery query = new SearchTransactionByCbuQuery(cbu, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        lenient().when(resultSet.getLong(1)).thenReturn(5L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCbu(cbu, query);

        assertTrue(result.isPresent());
        verify(preparedStatement, atLeastOnce()).setObject(anyInt(), any());
    }



    @Test
    void transactionCuitReturnsTransactionPage() throws SQLException {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        lenient().when(resultSet.getLong(1)).thenReturn(3L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCuit(cuit, query);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().page());
        assertEquals(10, result.get().size());
        verify(dataSource).getConnection();
    }

    @Test
    void transactionCuitReturnsEmptyPageWhenNoResults() throws SQLException {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<TransactionPage> result = adapter.transactionCuit(cuit, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().content().isEmpty());
        assertTrue(result.get().last());
    }

    @Test
    void transactionCuitHandlesSQLException() throws SQLException {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 0, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        Optional<TransactionPage> result = adapter.transactionCuit(cuit, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().content().isEmpty());
        verify(dataSource).getConnection();
    }

    @Test
    void transactionCuitWithAllFilters() throws SQLException {
        String cuit = "20301234567";
        LocalDate txDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate txDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        LocalDate ingestionDateFrom = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate ingestionDateTo = LocalDate.of(2024, Month.DECEMBER, 31);
        FilterCommon filterCommon = new FilterCommon(txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo, 0, 10, "transactionAt,asc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        lenient().when(resultSet.getLong(1)).thenReturn(5L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCuit(cuit, query);

        assertTrue(result.isPresent());
        verify(preparedStatement, atLeastOnce()).setObject(anyInt(), any());
    }

    @Test
    void transactionCuitReturnsLastPageCorrectly() throws SQLException {
        String cuit = "20301234567";
        FilterCommon filterCommon = new FilterCommon(null, null, null, null, 2, 10, "transactionAt,desc");
        SearchTransactionByCuitQuery query = new SearchTransactionByCuitQuery(cuit, filterCommon);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        lenient().when(resultSet.getLong(1)).thenReturn(25L);
        mockTransactionRow();

        Optional<TransactionPage> result = adapter.transactionCuit(cuit, query);

        assertTrue(result.isPresent());
        assertTrue(result.get().last());
        assertEquals(3, result.get().totalPages());
    }

    // ==================== Helper Methods ====================

    private void mockTransactionRow() throws SQLException {
        UUID id = UUID.randomUUID();
        UUID benPartyId = UUID.randomUUID();
        UUID benBeneficiaryId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-07T12:00:00Z");

        lenient().when(resultSet.getObject("id", UUID.class)).thenReturn(id);
        lenient().when(resultSet.getString("external_ref")).thenReturn("EXT-0001");
        lenient().when(resultSet.getObject("transaction_at", OffsetDateTime.class)).thenReturn(now);
        lenient().when(resultSet.getObject("ingested_at", OffsetDateTime.class)).thenReturn(now);
        lenient().when(resultSet.getString("type")).thenReturn("DEBIT");
        lenient().when(resultSet.getString("status")).thenReturn("COMPLETED");
        lenient().when(resultSet.getInt("amount")).thenReturn(1000);
        lenient().when(resultSet.getString("currency")).thenReturn("ARS");
        lenient().when(resultSet.getString("description")).thenReturn("Test transaction");
        lenient().when(resultSet.getObject("file_id", UUID.class)).thenReturn(fileId);
        lenient().when(resultSet.getString("created_by")).thenReturn("system");
        lenient().when(resultSet.getBoolean("flagged")).thenReturn(false);
        lenient().when(resultSet.getString("flag_reason")).thenReturn(null);
        lenient().when(resultSet.getObject(CREATED_AT, OffsetDateTime.class)).thenReturn(now);
        lenient().when(resultSet.getObject(UPDATED_AT, OffsetDateTime.class)).thenReturn(now);

        lenient().when(resultSet.getObject("benf_id", UUID.class)).thenReturn(benPartyId);
        lenient().when(resultSet.getString("benf_account")).thenReturn("ACC-123");
        lenient().when(resultSet.getString("benf_cbu")).thenReturn("0170099220000067797370");
        lenient().when(resultSet.getString("benf_cuit")).thenReturn("20329851657");
        lenient().when(resultSet.getString("benf_holder")).thenReturn("Juan Perez");
        lenient().when(resultSet.getString("benf_holder_type")).thenReturn("PERSON");
        lenient().when(resultSet.getString("benf_bank")).thenReturn("001");
        lenient().when(resultSet.getString("benf_branch")).thenReturn("0001");

        lenient().when(resultSet.getObject("ben_id", UUID.class)).thenReturn(benBeneficiaryId);
        lenient().when(resultSet.getString("ben_account")).thenReturn("ACC-456");
        lenient().when(resultSet.getString("ben_cbu")).thenReturn("0170099220000067797371");
        lenient().when(resultSet.getString("ben_cuit")).thenReturn("20329851658");
        lenient().when(resultSet.getString("ben_holder")).thenReturn("Maria Gomez");
        lenient().when(resultSet.getString("ben_holder_type")).thenReturn("PERSON");
        lenient().when(resultSet.getString("ben_bank")).thenReturn("002");
        lenient().when(resultSet.getString("ben_branch")).thenReturn("0002");
    }

    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";



}
