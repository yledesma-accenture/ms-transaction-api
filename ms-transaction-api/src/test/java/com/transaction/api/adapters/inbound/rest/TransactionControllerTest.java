package com.transaction.api.adapters.inbound.rest;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

}
