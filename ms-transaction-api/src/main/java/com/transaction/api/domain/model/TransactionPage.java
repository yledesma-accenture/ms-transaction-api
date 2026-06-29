package com.transaction.api.domain.model;

import java.util.List;

public record TransactionPage(
        List<Transaction> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {}
