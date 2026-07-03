package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.domain.model.Party;
import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
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

                        Transaction transaction = new Transaction(
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

}
