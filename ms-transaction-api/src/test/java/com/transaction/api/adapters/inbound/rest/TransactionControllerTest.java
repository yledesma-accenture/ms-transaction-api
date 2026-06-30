package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.domain.model.Transaction;
import com.transaction.api.domain.model.TransactionDetail;
import com.transaction.api.domain.model.TransactionPage;
import com.transaction.api.domain.model.TransactionType;
import com.transaction.api.domain.port.application.ITransactionPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITransactionPort transactionPort;

    private TransactionDetail  getTransactionDetail(UUID transactionId) {
        return  new TransactionDetail(   new Transaction(
                transactionId,
                "TNX-2024-000001",
                OffsetDateTime.parse("2024-03-15T09:00:00Z"),
                OffsetDateTime.parse("2024-03-15T09:05:22Z"),
                TransactionType.DEBIT,
                "COMPLETED",
                250,
                "USD",
                null,
                null,
                "ATM withdrawal",
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "operator-42",
                true,
                "Amount exceeds account 30-day average by 720%"),
                null,
                null,
                null) ;
    }

    @Test
    void shouldReturnTransaction() throws Exception {
        UUID transactionId = UUID.fromString(
                "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        );
        TransactionDetail transactionDetail = getTransactionDetail(transactionId);

        Mockito.when(transactionPort.transactionId(transactionId.toString())).thenReturn(transactionDetail);;

        mockMvc.perform(get("/api/v1/transactions/3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value(transactionId.toString()));

    }

    @Test
    void shouldFailWhenTransactionIdIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/search/cbu/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCbu() throws Exception {
        UUID transactionId = UUID.fromString(
                "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        );
        TransactionDetail transactionDetail = getTransactionDetail(transactionId);
        TransactionPage transactionPage = new TransactionPage(List.of(transactionDetail.transaction()),1,10,100,10,false);

        Mockito.when(transactionPort.transactionCbu(
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        anyInt(),
                        anyString()))
                .thenReturn(transactionPage);

        mockMvc.perform(get("/api/v1/transactions/search/cbu/0170099220000067797370"))
                .andDo(print())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()));
    }

    @Test
    void shouldReturnCbuWithAllParameters() throws Exception {
        UUID transactionId = UUID.fromString(
                "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        );
        TransactionDetail transactionDetail = getTransactionDetail(transactionId);
        TransactionPage transactionPage = new TransactionPage(List.of(transactionDetail.transaction()),1,10,100,10,false);

        Mockito.when(transactionPort.transactionCbu(
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        anyInt(),
                        anyString()))
                .thenReturn(transactionPage);



        mockMvc.perform(get("/api/v1/transactions/search/cbu/0170099220000067797370")
                        .param("txDateFrom", "2026-01-01")
                        .param("txDateTo", "2026-06-30")
                        .param("ingestionDateFrom", "2026-01-01")
                        .param("ingestionDateTo", "2026-06-30")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()));
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
        UUID transactionId = UUID.fromString(
                "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        );
        TransactionDetail transactionDetail = getTransactionDetail(transactionId);
        TransactionPage transactionPage = new TransactionPage(List.of(transactionDetail.transaction()),1,10,100,10,false);

        Mockito.when(transactionPort.transactionCuit(
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        anyInt(),
                        anyString()))
                .thenReturn(transactionPage);
        mockMvc.perform(get("/api/v1/transactions/search/cuit/20329851657"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()));

    }

    @Test
    void shouldReturnCuitWithAllParameters() throws Exception {
        UUID transactionId = UUID.fromString(
                "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        );
        TransactionDetail transactionDetail = getTransactionDetail(transactionId);
        TransactionPage transactionPage = new TransactionPage(List.of(transactionDetail.transaction()),1,10,100,10,false);

        Mockito.when(transactionPort.transactionCuit(
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyInt(),
                        anyInt(),
                        anyString()))
                .thenReturn(transactionPage);

        mockMvc.perform(get("/api/v1/transactions/search/cuit/20329851657")
                        .param("txDateFrom", "2026-01-01")
                        .param("txDateTo", "2026-06-30")
                        .param("ingestionDateFrom", "2026-01-01")
                        .param("ingestionDateTo", "2026-06-30")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()));
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
