package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.SearchTransactionByCbuQuery;
import com.transaction.api.adapters.model.SearchTransactionByCuitQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
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
import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class TransactionDatabaseAdapter implements ITransactionDatabasePort {

    private static class QueryParts {
        StringBuilder sql = new StringBuilder();
        StringBuilder countSql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();
    }

    private final DataSource dataSource;

    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";




    public TransactionDatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<TransactionDetail> findById(String transactionId) {
        UUID uuid = UUID.fromString(transactionId);
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
             stmt.setObject(1, uuid);

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

    @Override
    public TransactionPage searchTransactionByUser(SearchTransactionByUserQuery query) {
        List<Transaction> transactions = new ArrayList<>();
        int total = 0;

        QueryParts queryParts = baseQuery();

        queryParts.sql.append(" WHERE t.created_by = ?");
        queryParts.countSql.append(" WHERE t.created_by = ?");
        queryParts.params.add(query.userId());
        queryParts.countParams.add(query.userId());

        var filter = query.filterCommon();

        applyCommonDateFilters(queryParts, filter);


        queryParts.sql.append(buildOrderBy(filter.sort()));
        queryParts.sql.append(" LIMIT ? OFFSET ?");

        int page = filter.page();
        int size = filter.size();
        int offset = page * size;

        queryParts.params.add(size);
        queryParts.params.add(offset);

        try (Connection conn = dataSource.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(queryParts.countSql.toString())) {
                for (int i = 0; i < queryParts.countParams.size(); i++) {
                    countStmt.setObject(i + 1, queryParts.countParams.get(i));
                }

                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(queryParts.sql.toString())) {
                for (int i = 0; i < queryParts.params.size(); i++) {
                    stmt.setObject(i + 1, queryParts.params.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapRow(rs));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error querying transactions", e);
        }

        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        boolean last = page >= totalPages - 1;

        return new TransactionPage(transactions, page, size, total, totalPages, last);
    }

    @Override
    public TransactionPage listTransaction(ListTransactionsQuery listTransactionsQuery) {
        List<Transaction> transactions = new ArrayList<>();
        long total = 0;
        QueryParts queryParts = baseQuery();

        boolean joinIngestedFiles =
                listTransactionsQuery.filterCommon().ingestionDateFrom() != null ||
                        listTransactionsQuery.filterCommon().ingestionDateTo() != null;

        if (joinIngestedFiles) {
            queryParts.sql.append(" JOIN ingested_files f ON t.file_id = f.id ");
            queryParts.countSql.append(" JOIN ingested_files f ON t.file_id = f.id ");
        }

        queryParts.sql.append(" WHERE 1=1 ");
        queryParts.countSql.append(" WHERE 1=1 ");

        applyCommonDateFilters(queryParts, listTransactionsQuery.filterCommon());

        if (listTransactionsQuery.transactionType() != null) {
            addFilter(queryParts, " AND t.type = ?", listTransactionsQuery.transactionType().name());
        }

        if (listTransactionsQuery.transactionStatus() != null) {
            addFilter(queryParts, " AND t.status = ?", listTransactionsQuery.transactionStatus().name());
        }

        if (listTransactionsQuery.currency() != null) {
            addFilter(queryParts, " AND t.currency = ?", listTransactionsQuery.currency());
        }

        if (listTransactionsQuery.amountMin() != null) {
            addFilter(queryParts, " AND t.amount >= ?", listTransactionsQuery.amountMin());
        }

        if (listTransactionsQuery.amountMax() != null) {
            addFilter(queryParts, " AND t.amount <= ?", listTransactionsQuery.amountMax());
        }

        queryParts.sql.append(buildOrderBy(listTransactionsQuery.filterCommon().sort()));
        queryParts.sql.append(" LIMIT ? OFFSET ?");

        int page = listTransactionsQuery.filterCommon().page();
        int size = listTransactionsQuery.filterCommon().size();
        int offset = page * size;

        try (Connection conn = dataSource.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(queryParts.countSql.toString())) {
                for (int i = 0; i < queryParts.countParams.size(); i++) {
                    countStmt.setObject(i + 1, queryParts.countParams.get(i));
                }

                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getLong(1);
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(queryParts.sql.toString())) {
                int index = 1;
                for (Object param : queryParts.params) {
                    stmt.setObject(index++, param);
                }
                stmt.setInt(index++, size);
                stmt.setInt(index, offset);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapRow(rs));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listing transactions", e);
        }

        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        boolean last = page >= totalPages - 1;

        return new TransactionPage(transactions, page, size, total, totalPages, last);
    }

    @Override
    public TransactionSummary getSummary(SummaryQuery query) {
        List<TransactionSummaryGroup> groups = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        List<Object> globalParams = new ArrayList<>();

        String groupByColumn = resolveColumn(query.groupBy());

        boolean joinIngestedFiles =
                query.ingestionDateFrom() != null || query.ingestionDateTo() != null;

        StringBuilder fromClause = new StringBuilder(" FROM transactions t ");
        if (joinIngestedFiles) {
            fromClause.append(" JOIN ingested_files f ON t.file_id = f.id ");
        }

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        if (query.txDateFrom() != null) {
            whereClause.append(" AND t.transaction_at >= ? ");
            params.add(query.txDateFrom());
            globalParams.add(query.txDateFrom());
        }

        if (query.txDateTo() != null) {
            whereClause.append(" AND t.transaction_at <= ? ");
            params.add(query.txDateTo());
            globalParams.add(query.txDateTo());
        }

        if (query.ingestionDateFrom() != null) {
            whereClause.append(" AND f.processed_at >= ? ");
            params.add(query.ingestionDateFrom());
            globalParams.add(query.ingestionDateFrom());
        }

        if (query.ingestionDateTo() != null) {
            whereClause.append(" AND f.processed_at <= ? ");
            params.add(query.ingestionDateTo());
            globalParams.add(query.ingestionDateTo());
        }

        String globalSql = """
        SELECT
            COUNT(*) AS total_count,
            COALESCE(SUM(t.amount), 0) AS total_amount
    """ + fromClause + whereClause;

        String groupSql = """
        SELECT
            %s AS group_key,
            COUNT(*) AS count,
            COALESCE(SUM(t.amount), 0) AS total_amount,
            COALESCE(AVG(t.amount), 0) AS average_amount,
            COALESCE(MIN(t.amount), 0) AS min_amount,
            COALESCE(MAX(t.amount), 0) AS max_amount,
            SUM(CASE WHEN t.flagged = TRUE THEN 1 ELSE 0 END) AS flagged_count
    """.formatted(groupByColumn) + fromClause + whereClause +
                " GROUP BY " + groupByColumn +
                " ORDER BY " + groupByColumn;

        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        try (Connection conn = dataSource.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(globalSql)) {
                for (int i = 0; i < globalParams.size(); i++) {
                    stmt.setObject(i + 1, globalParams.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalCount = rs.getLong("total_count");
                        totalAmount = rs.getBigDecimal("total_amount");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(groupSql)) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        groups.add(new TransactionSummaryGroup(
                                rs.getString("group_key"),
                                rs.getInt("count"),
                                rs.getBigDecimal("total_amount"),
                                rs.getBigDecimal("average_amount"),
                                rs.getBigDecimal("min_amount"),
                                rs.getBigDecimal("max_amount"),
                                rs.getInt("flagged_count")
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error getting transaction summary", e);
        }

        return new TransactionSummary(
                query.txDateFrom(),
                query.txDateTo(),
                query.ingestionDateFrom(),
                query.ingestionDateTo(),
                totalCount,
                totalAmount,
                query.groupBy(),
                groups
        );
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Party benefactor = new Party(
                rs.getObject("benefactor_id", UUID.class),
                rs.getString("benefactor_account_number"),
                rs.getString("benefactor_cbu"),
                rs.getString("benefactor_cuit"),
                rs.getString("benefactor_holder_name"),
                rs.getString("benefactor_holder_type"),
                rs.getString("benefactor_bank_code"),
                rs.getString("benefactor_branch_code")
        );

        Party beneficiary = new Party(
                rs.getObject("beneficiary_id", UUID.class),
                rs.getString("beneficiary_account_number"),
                rs.getString("beneficiary_cbu"),
                rs.getString("beneficiary_cuit"),
                rs.getString("beneficiary_holder_name"),
                rs.getString("beneficiary_holder_type"),
                rs.getString("beneficiary_bank_code"),
                rs.getString("beneficiary_branch_code")
        );

        return new Transaction(
                rs.getObject("id", UUID.class),
                rs.getString("external_ref"),
                rs.getObject("transaction_at", OffsetDateTime.class),
                rs.getObject("ingested_at", OffsetDateTime.class),
                rs.getString("type"),
                rs.getString("status"),
                rs.getInt("amount"),
                rs.getString("currency"),
                benefactor,
                beneficiary,
                rs.getString("description"),
                rs.getObject("file_id", UUID.class),
                rs.getString("created_by"),
                rs.getBoolean("flagged"),
                rs.getString("flag_reason")
        );
    }

    private QueryParts baseQuery(){
        QueryParts queryParts = new QueryParts();

        queryParts.sql = new StringBuilder("""
         SELECT
            t.id,
            t.external_ref,
            t.transaction_at,
            t.ingested_at,
            t.type,
            t.status,
            t.amount,
            t.currency,
            t.description,
            t.file_id,
            t.created_by,
            t.flagged,
            t.flag_reason,

            b.id AS benefactor_id,
            b.account_number AS benefactor_account_number,
            b.cbu AS benefactor_cbu,
            b.cuit AS benefactor_cuit,
            b.holder_name AS benefactor_holder_name,
            b.holder_type AS benefactor_holder_type,
            b.bank_code AS benefactor_bank_code,
            b.branch_code AS benefactor_branch_code,

            y.id AS beneficiary_id,
            y.account_number AS beneficiary_account_number,
            y.cbu AS beneficiary_cbu,
            y.cuit AS beneficiary_cuit,
            y.holder_name AS beneficiary_holder_name,
            y.holder_type AS beneficiary_holder_type,
            y.bank_code AS beneficiary_bank_code,
            y.branch_code AS beneficiary_branch_code
         FROM transactions t
         JOIN accounts b ON t.benefactor_id = b.id
         JOIN accounts y ON t.beneficiary_id = y.id
         """);

        queryParts.countSql = new StringBuilder("""
         SELECT COUNT(*)
         FROM transactions t""");

        return queryParts;
    }

    private String buildOrderBy(String sort) {
        String defaultField = "transactionAt";
        String defaultDirection = "desc";

        if (sort == null || sort.isBlank()) {
            return " ORDER BY " + resolveColumn(defaultField) + " " + defaultDirection;
        }

        String[] parts = sort.split(",");
        String field = parts.length > 0 ? parts[0].trim() : defaultField;
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : defaultDirection;

        if (!direction.equals("asc") && !direction.equals("desc")) {
            direction = defaultDirection;
        }

        return " ORDER BY " + resolveColumn(field) + " " + direction;
    }

    private String resolveColumn(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return "t.status";
        }

        return switch (groupBy.toLowerCase()) {
            case "status" -> "t.status";
            case "type" -> "t.type";
            case "currency" -> "t.currency";
            case "created_by" -> "t.created_by";
            case "flagged" -> "t.flagged";
            case "transaction_at", "transactionat" -> "t.transaction_at";
            default -> throw new IllegalArgumentException("Invalid groupBy: " + groupBy);
        };
    }

    private void addFilter(QueryParts queryParts, String clause, Object value) {
        queryParts.sql.append(clause);
        queryParts.countSql.append(clause);
        queryParts.params.add(value);
        queryParts.countParams.add(value);
    }

    private void applyCommonDateFilters(QueryParts queryParts, FilterCommon filter) {
        if (filter.txDateFrom() != null) {
            addFilter(queryParts, " AND t.transaction_at >= ?", Date.valueOf(filter.txDateFrom()));
        }
        if (filter.txDateTo() != null) {
            addFilter(queryParts, " AND t.transaction_at <= ?", Date.valueOf(filter.txDateTo()));
        }
        if (filter.ingestionDateFrom() != null) {
            addFilter(queryParts, " AND t.ingested_at >= ?", Date.valueOf(filter.ingestionDateFrom()));
        }
        if (filter.ingestionDateTo() != null) {
            addFilter(queryParts, " AND t.ingested_at <= ?", Date.valueOf(filter.ingestionDateTo()));
        }
    }

}
