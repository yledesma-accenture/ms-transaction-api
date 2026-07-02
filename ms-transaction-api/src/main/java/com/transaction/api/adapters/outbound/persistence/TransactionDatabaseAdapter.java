package com.transaction.api.adapters.outbound.persistence;

import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.port.infrastructure.ITransactionDatabasePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

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
}
