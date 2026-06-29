package com.transaction.api.adapters.inbound.rest;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTransaction() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/2323"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
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
        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200ForGetTransactionsSummaryEndpoint() throws Exception {
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
