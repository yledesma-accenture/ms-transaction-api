package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SearchTransactionByCbuQuery;
import com.transaction.api.adapters.model.SearchTransactionByCuitQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class TransactionDatabaseAdapter implements ITransactionDatabasePort {

    private final DataSource dataSource;

    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";




    public TransactionDatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<TransactionDetail> findById(String transactionId) {
        String query = """
            SELECT t.id, t.external_ref, t.transaction_at, t.ingested_at, t.type, t.status,
                   t.amount, t.currency, t.description, t.file_id, t.created_by, t.flagged,
                   t.flag_reason, t.created_at, t.updated_at,
                   benf.id as benf_id, benf.account_number as benf_account, benf.cbu as benf_cbu,
                   benf.cuit as benf_cuit, benf.holder_name as benf_holder, benf.holder_type as benf_holder_type,
                   benf.bank_code as benf_bank, benf.branch_code as benf_branch,
                   ben.id as ben_id, ben.account_number as ben_account, ben.cbu as ben_cbu,
                   ben.cuit as ben_cuit, ben.holder_name as ben_holder, ben.holder_type as ben_holder_type,
                   ben.bank_code as ben_bank, ben.branch_code as ben_branch
            FROM transactions t
            LEFT JOIN accounts benf ON t.benefactor_id = benf.id
            LEFT JOIN accounts ben ON t.beneficiary_id = ben.id
            WHERE t.id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             stmt.setString(1, transactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = mapRowToTransaction(rs);
                    List<ValidationWarning> warnings = getValidationWarnings(transactionId, conn);
                    OffsetDateTime createdAt = rs.getObject(CREATED_AT, OffsetDateTime.class);
                    OffsetDateTime updatedAt = rs.getObject(UPDATED_AT, OffsetDateTime.class);

                    TransactionDetail detail = new TransactionDetail(
                            transaction,
                            createdAt,
                            updatedAt,
                            warnings
                    );
                    return Optional.of(detail);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding transaction by id: {}", transactionId, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TransactionPage> transactionCbu(String cbu, SearchTransactionByCbuQuery searchTransactionByCbuQuery) {
        log.info("Searching transactions by CBU: {}", cbu);
        FilterCommon filter = searchTransactionByCbuQuery.filterCommon();

        String countQuery = buildCbuCountQuery(filter);
        String selectQuery = buildCbuSelectQuery(filter);

        try (Connection conn = dataSource.getConnection()) {
            // Obtener total de registros
            long totalElements = getTotalCount(conn, countQuery, cbu, filter);

            if (totalElements == 0) {
                return Optional.of(new TransactionPage(List.of(), filter.page(), filter.size(), 0, 0, true));
            }

            // Obtener transacciones paginadas
            List<Transaction> content = getTransactionsByCbu(conn, selectQuery, cbu, filter);

            int totalPages = Math.toIntExact((totalElements + filter.size() - 1) / filter.size());
            boolean isLast = filter.page() >= totalPages - 1;

            return Optional.of(new TransactionPage(
                    content,
                    filter.page(),
                    filter.size(),
                    totalElements,
                    totalPages,
                    isLast
            ));
        } catch (SQLException e) {
            log.error("Error searching transactions by CBU: {}", cbu, e);
            return Optional.of(new TransactionPage(List.of(), filter.page(), filter.size(), 0, 0, true));
        }
    }

    @Override
    public Optional<TransactionPage> transactionCuit(String cuit, SearchTransactionByCuitQuery searchTransactionByCuitQuery) {
        log.info("Searching transactions by CUIT: {}", cuit);
        FilterCommon filter = searchTransactionByCuitQuery.filterCommon();

        String countQuery = buildCuitCountQuery(filter);
        String selectQuery = buildCuitSelectQuery(filter);

        try (Connection conn = dataSource.getConnection()) {
            long totalElements = getTotalCount(conn, countQuery, cuit, filter);

            if (totalElements == 0) {
                return Optional.of(new TransactionPage(List.of(), filter.page(), filter.size(), 0, 0, true));
            }

            List<Transaction> content = getTransactionsByCuit(conn, selectQuery, cuit, filter);

            int totalPages = Math.toIntExact((totalElements + filter.size() - 1) / filter.size());
            boolean isLast = filter.page() >= totalPages - 1;

            return Optional.of(new TransactionPage(
                    content,
                    filter.page(),
                    filter.size(),
                    totalElements,
                    totalPages,
                    isLast
            ));
        } catch (SQLException e) {
            log.error("Error searching transactions by CUIT: {}", cuit, e);
            return Optional.of(new TransactionPage(List.of(), filter.page(), filter.size(), 0, 0, true));
        }
    }

    private String buildCuitSelectQuery(FilterCommon filter) {
        StringBuilder query = new StringBuilder("""
        SELECT t.id, t.external_ref, t.transaction_at, t.ingested_at, t.type, t.status,
               t.amount, t.currency, t.description, t.file_id, t.created_by, t.flagged,
               t.flag_reason, t.created_at, t.updated_at,
               benf.id as benf_id, benf.account_number as benf_account, benf.cbu as benf_cbu,
               benf.cuit as benf_cuit, benf.holder_name as benf_holder, benf.holder_type as benf_holder_type,
               benf.bank_code as benf_bank, benf.branch_code as benf_branch,
               ben.id as ben_id, ben.account_number as ben_account, ben.cbu as ben_cbu,
               ben.cuit as ben_cuit, ben.holder_name as ben_holder, ben.holder_type as ben_holder_type,
               ben.bank_code as ben_bank, ben.branch_code as ben_branch
        FROM transactions t
        LEFT JOIN accounts benf ON t.benefactor_id = benf.id
        LEFT JOIN accounts ben ON t.beneficiary_id = ben.id
        WHERE (benf.cuit = ? OR ben.cuit = ?)
        """);

        appendFilters(query, filter);
        appendSort(query, filter.sort());
        appendPagination(query);

        return query.toString();
    }

    private String buildCuitCountQuery(FilterCommon filter) {
        StringBuilder query = new StringBuilder("""
        SELECT COUNT(DISTINCT t.id) FROM transactions t
        LEFT JOIN accounts benf ON t.benefactor_id = benf.id
        LEFT JOIN accounts ben ON t.beneficiary_id = ben.id
        WHERE (benf.cuit = ? OR ben.cuit = ?)
        """);

        appendFilters(query, filter);
        return query.toString();
    }


    private List<Transaction> getTransactionsByCuit(Connection conn, String query, String cuit, FilterCommon filter) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, cuit);
            stmt.setString(paramIndex++, cuit);
            paramIndex = setFilterParams(stmt, paramIndex, filter);
            stmt.setInt(paramIndex++, filter.size());
            stmt.setInt(paramIndex, filter.page() * filter.size());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapRowToTransaction(rs);
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }


    private List<Transaction> getTransactionsByCbu(Connection conn, String query, String cbu, FilterCommon filter) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, cbu);
            stmt.setString(paramIndex++, cbu);
            paramIndex = setFilterParams(stmt, paramIndex, filter);
            stmt.setInt(paramIndex++, filter.size());
            stmt.setInt(paramIndex, filter.page() * filter.size());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapRowToTransaction(rs);
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    private String buildCbuCountQuery(FilterCommon filter) {
        StringBuilder query = new StringBuilder("""
        SELECT COUNT(DISTINCT t.id) FROM transactions t
        LEFT JOIN accounts benf ON t.benefactor_id = benf.id
        LEFT JOIN accounts ben ON t.beneficiary_id = ben.id
        WHERE (benf.cbu = ? OR ben.cbu = ?)
        """);

        appendFilters(query, filter);
        return query.toString();
    }

    private String buildCbuSelectQuery(FilterCommon filter) {
        StringBuilder query = new StringBuilder("""
        SELECT t.id, t.external_ref, t.transaction_at, t.ingested_at, t.type, t.status,
               t.amount, t.currency, t.description, t.file_id, t.created_by, t.flagged,
               t.flag_reason, t.created_at, t.updated_at,
               benf.id as benf_id, benf.account_number as benf_account, benf.cbu as benf_cbu,
               benf.cuit as benf_cuit, benf.holder_name as benf_holder, benf.holder_type as benf_holder_type,
               benf.bank_code as benf_bank, benf.branch_code as benf_branch,
               ben.id as ben_id, ben.account_number as ben_account, ben.cbu as ben_cbu,
               ben.cuit as ben_cuit, ben.holder_name as ben_holder, ben.holder_type as ben_holder_type,
               ben.bank_code as ben_bank, ben.branch_code as ben_branch
        FROM transactions t
        LEFT JOIN accounts benf ON t.benefactor_id = benf.id
        LEFT JOIN accounts ben ON t.beneficiary_id = ben.id
        WHERE (benf.cbu = ? OR ben.cbu = ?)
        """);

        appendFilters(query, filter);
        appendSort(query, filter.sort());
        appendPagination(query);

        return query.toString();
    }

    private void appendFilters(StringBuilder query, FilterCommon filter) {
        if (filter.txDateFrom() != null) {
            query.append("AND CAST(t.transaction_at AS DATE) >= ? ");
        }
        if (filter.txDateTo() != null) {
            query.append("AND CAST(t.transaction_at AS DATE) <= ? ");
        }
        if (filter.ingestionDateFrom() != null) {
            query.append("AND CAST(t.ingested_at AS DATE ) >= ? ");
        }
        if (filter.ingestionDateTo() != null) {
            query.append("AND CAST(t.ingested_at AS DATE) <= ? ");
        }
    }

    private void appendSort(StringBuilder query, String sort) {
        log.info("Appending sort to query: {}", sort);
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                query.append("ORDER BY t.").append(parts[0]).append(" ").append(parts[1].toUpperCase()).append(" ");
            }
        } else {
            query.append("ORDER BY t.transaction_at DESC ");
        }
    }

    private void appendPagination(StringBuilder query) {
        query.append("LIMIT ? OFFSET ?");
    }

    private long getTotalCount(Connection conn, String query, String cbu, FilterCommon filter) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, cbu);
            stmt.setString(paramIndex++, cbu);
            paramIndex = setFilterParams(stmt, paramIndex, filter);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }



    private int setFilterParams(PreparedStatement stmt, int paramIndex, FilterCommon filter) throws SQLException {
        if (filter.txDateFrom() != null) {
            stmt.setObject(paramIndex++, filter.txDateFrom());
        }
        if (filter.txDateTo() != null) {
            stmt.setObject(paramIndex++, filter.txDateTo());
        }
        if (filter.ingestionDateFrom() != null) {
            stmt.setObject(paramIndex++, filter.ingestionDateFrom());
        }
        if (filter.ingestionDateTo() != null) {
            stmt.setObject(paramIndex++, filter.ingestionDateTo());
        }
        return paramIndex;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getObject("id", UUID.class),
                rs.getString("external_ref"),
                rs.getObject("transaction_at", OffsetDateTime.class),
                rs.getObject("ingested_at", OffsetDateTime.class),
                rs.getString("type"),
                rs.getString("status"),
                rs.getInt("amount"),
                rs.getString("currency"),
                mapParty(rs, "benf"),
                mapParty(rs, "ben"),
                rs.getString("description"),
                rs.getObject("file_id", UUID.class),
                rs.getString("created_by"),
                rs.getBoolean("flagged"),
                rs.getString("flag_reason")
        );
    }

    private Party mapParty(ResultSet rs, String prefix) throws SQLException {
        UUID id = rs.getObject(prefix + "_id", UUID.class);
        if (id == null) return null;

        return new Party(
                id,
                rs.getString(prefix + "_account"),
                rs.getString(prefix + "_cbu"),
                rs.getString(prefix + "_cuit"),
                rs.getString(prefix + "_holder"),
                rs.getString(prefix + "_holder_type"),
                rs.getString(prefix + "_bank"),
                rs.getString(prefix + "_branch")
        );
    }

    private List<ValidationWarning> getValidationWarnings(String transactionId, Connection conn) throws SQLException {
        String query = "SELECT id, warning_code, warning_message, created_at FROM transaction_validation_warnings WHERE transaction_id = ?";
        List<ValidationWarning> warnings = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    warnings.add(new ValidationWarning(
                            rs.getObject("id", UUID.class),
                            rs.getString("warning_code"),
                            rs.getString("warning_message"),
                            rs.getObject(CREATED_AT, OffsetDateTime.class)
                    ));
                }
            }
        }

        return warnings;
    }
}
