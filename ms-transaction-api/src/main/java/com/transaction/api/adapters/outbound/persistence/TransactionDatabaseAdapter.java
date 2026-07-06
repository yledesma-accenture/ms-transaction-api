package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
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

    public TransactionDatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<TransactionDetail> findById(String transactionId) {
        return Optional.empty();
    }

    @Override
    public TransactionPage searchTransactionByUser(SearchTransactionByUserQuery query) {
        List<Transaction> transactions = new ArrayList<>();
        int total = 0;

        String sql = """
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
        WHERE t.created_by = ?
        ORDER BY t.transaction_at DESC
        LIMIT ? OFFSET ?
    """;

        String countSql = """
        SELECT COUNT(*)
        FROM transactions t
        WHERE t.created_by = ?
    """;

        int page = query.filterCommon().page();
        int size = query.filterCommon().size();
        int offset = page * size;

        try (Connection conn = dataSource.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setString(1, String.valueOf(query.userId()));

                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(query.userId()));
                stmt.setInt(2, size);
                stmt.setInt(3, offset);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Transaction transaction = mapRow(rs);

                        transactions.add(transaction);
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

        StringBuilder sql = new StringBuilder("""
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

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(*)
        FROM transactions t
    """);

        boolean joinIngestedFiles =
                listTransactionsQuery.filterCommon().ingestionDateFrom() != null ||
                        listTransactionsQuery.filterCommon().ingestionDateTo() != null;

        if (joinIngestedFiles) {
            sql.append(" JOIN ingested_files f ON t.file_id = f.id ");
            countSql.append(" JOIN ingested_files f ON t.file_id = f.id ");
        }

        sql.append(" WHERE 1=1 ");
        countSql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();

        if (listTransactionsQuery.filterCommon().txDateFrom() != null) {
            sql.append(" AND t.transaction_at >= ?");
            countSql.append(" AND t.transaction_at >= ?");
            params.add(listTransactionsQuery.filterCommon().txDateFrom());
            countParams.add(listTransactionsQuery.filterCommon().txDateFrom());
        }

        if (listTransactionsQuery.filterCommon().txDateTo() != null) {
            sql.append(" AND t.transaction_at <= ?");
            countSql.append(" AND t.transaction_at <= ?");
            params.add(listTransactionsQuery.filterCommon().txDateTo());
            countParams.add(listTransactionsQuery.filterCommon().txDateTo());
        }

        if (listTransactionsQuery.filterCommon().ingestionDateFrom() != null) {
            sql.append(" AND f.processed_at >= ?");
            countSql.append(" AND f.processed_at >= ?");
            params.add(listTransactionsQuery.filterCommon().ingestionDateFrom());
            countParams.add(listTransactionsQuery.filterCommon().ingestionDateFrom());
        }

        if (listTransactionsQuery.filterCommon().ingestionDateTo() != null) {
            sql.append(" AND f.processed_at <= ?");
            countSql.append(" AND f.processed_at <= ?");
            params.add(listTransactionsQuery.filterCommon().ingestionDateTo());
            countParams.add(listTransactionsQuery.filterCommon().ingestionDateTo());
        }

        if (listTransactionsQuery.transactionType() != null) {
            sql.append(" AND t.type = ?");
            countSql.append(" AND t.type = ?");
            params.add(listTransactionsQuery.transactionType().name());
            countParams.add(listTransactionsQuery.transactionType().name());
        }

        if (listTransactionsQuery.transactionStatus() != null) {
            sql.append(" AND t.status = ?");
            countSql.append(" AND t.status = ?");
            params.add(listTransactionsQuery.transactionStatus().name());
            countParams.add(listTransactionsQuery.transactionStatus().name());
        }

        if (listTransactionsQuery.currency() != null) {
            sql.append(" AND t.currency = ?");
            countSql.append(" AND t.currency = ?");
            params.add(listTransactionsQuery.currency());
            countParams.add(listTransactionsQuery.currency());
        }

        if (listTransactionsQuery.amountMin() != null) {
            sql.append(" AND t.amount >= ?");
            countSql.append(" AND t.amount >= ?");
            params.add(listTransactionsQuery.amountMin());
            countParams.add(listTransactionsQuery.amountMin());
        }

        if (listTransactionsQuery.amountMax() != null) {
            sql.append(" AND t.amount <= ?");
            countSql.append(" AND t.amount <= ?");
            params.add(listTransactionsQuery.amountMax());
            countParams.add(listTransactionsQuery.amountMax());
        }

        sql.append(" ORDER BY t.transaction_at DESC LIMIT ? OFFSET ?");

        int page = listTransactionsQuery.filterCommon().page();
        int size = listTransactionsQuery.filterCommon().size();
        int offset = page * size;

        try (Connection conn = dataSource.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(countSql.toString())) {
                for (int i = 0; i < countParams.size(); i++) {
                    countStmt.setObject(i + 1, countParams.get(i));
                }

                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getLong(1);
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int index = 1;
                for (Object param : params) {
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

        String groupByColumn = resolveGroupByColumn(query.groupBy());

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

    private String resolveGroupByColumn(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return "t.status";
        }

        return switch (groupBy.toLowerCase()) {
            case "status" -> "t.status";
            case "type" -> "t.type";
            case "currency" -> "t.currency";
            case "created_by" -> "t.created_by";
            case "flagged" -> "t.flagged";
            default -> throw new IllegalArgumentException("Invalid groupBy: " + groupBy);
        };
    }

}
