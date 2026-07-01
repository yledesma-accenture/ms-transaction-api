package com.transaction.api.adapters.inbound.rest;


import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITransactionPort transactionPort;

    @MockitoBean
    private TransactionQueryMapper  mapper;

    @Test
    void shouldReturnTransaction() throws Exception {

        UUID transactionUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID fileUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

        Transaction transaction = new Transaction(
                transactionUuid,
                "ext-123",
                OffsetDateTime.parse("2026-01-10T10:15:30Z"),
                OffsetDateTime.parse("2026-01-10T11:15:30Z"),
                "DEBIT",
                "PENDING",
                1000,
                "USD",
                null,
                null,
                "test",
                fileUuid,
                "pablo.conde",
                false,
                null
        );

        TransactionDetail detail = new TransactionDetail(
                transaction,
                OffsetDateTime.parse("2026-01-10T12:00:00Z"),
                OffsetDateTime.parse("2026-01-10T12:30:00Z"),
                List.of()
        );

        when(transactionPort.transactionId("2323")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/transactions/2323"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.transaction.externalRef").value("ext-123"))
                .andExpect(jsonPath("$.transaction.type").value("DEBIT"))
                .andExpect(jsonPath("$.transaction.status").value("PENDING"))
                .andExpect(jsonPath("$.transaction.amount").value(1000))
                .andExpect(jsonPath("$.transaction.currency").value("USD"))
                .andExpect(jsonPath("$.transaction.description").value("test"))
                .andExpect(jsonPath("$.transaction.fileId").value("123e4567-e89b-12d3-a456-426614174001"))
                .andExpect(jsonPath("$.transaction.createdBy").value("pablo.conde"))
                .andExpect(jsonPath("$.transaction.flagged").value(false))
                .andExpect(jsonPath("$.validationWarnings").isArray())
                .andExpect(jsonPath("$.validationWarnings").isEmpty());
    }

    @Test
    void shouldFailWhenTransactionIdIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCbu() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/0170099220000067797370"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldReturnCbuWithAllParameters() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/0170099220000067797370")
                        .param("txDateFrom", "2026-01-01")
                        .param("txDateTo", "2026-06-30")
                        .param("ingestionDateFrom", "2026-01-01")
                        .param("ingestionDateTo", "2026-06-30")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldFailWhenCbuIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailWhenCuitTxDateFromIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cuit/20329851657")
                        .param("txDateFrom", "20260101"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnCuit() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cuit/20329851657"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldReturnCuitWithAllParameters() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cuit/20329851657")
                        .param("txDateFrom", "2026-01-01")
                        .param("txDateTo", "2026-06-30")
                        .param("ingestionDateFrom", "2026-01-01")
                        .param("ingestionDateTo", "2026-06-30")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldFailWhenTxDateFromIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/0170099220000067797370")
                        .param("txDateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenCuitIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cuit/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyTransactionPageWhenSearchByUser() throws Exception {
        TransactionPage page = new TransactionPage(List.of(), 0, 20, 0, 0, true);

        when(transactionPort.searchTransactionByUser(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions/search/user/123")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenTxDateFromHasInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/user/123")
                        .param("txDateFrom", "2024/01/01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEmptyTransactionPageWhenListTransaction() throws Exception {
        TransactionPage page = new TransactionPage(List.of(), 0, 20, 0, 0, true);

        when(transactionPort.listTransaction(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("type", "DEBIT")
                        .param("status", "PENDING")
                        .param("currency", "USD")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsInvalid() throws Exception {
        when(mapper.toListTransactionQuery(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "transactionStatus inválido: INVALID_STATUS"));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200ForGetTransactionsSummaryEndpoint() throws Exception {
        TransactionSummary summary = new TransactionSummary(null, null, null, null,
                0, BigDecimal.ZERO, "type", List.of());

        SummaryQuery summaryQuery = new SummaryQuery(null, null, null, null, "type");

        when(mapper.toSummaryQuery(any())).thenReturn(summaryQuery);

        when(transactionPort.getSummary(any(SummaryQuery.class))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/transactions/summary")
                        .param("groupBy", "type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.groupedBy").value("type"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups").isEmpty());
    }

    @Test
    void shouldReturn400ForGetTransactionsSummaryEndpointWhenTxDateFromIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/summary")
                        .param("txDateFrom", "2024/01/01"))
                .andExpect(status().isBadRequest());
    }

}
