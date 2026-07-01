package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.inbound.dto.TransactionFilterRequest;
import com.transaction.api.adapters.model.ListTransactionsQuery;
import com.transaction.api.adapters.model.SearchTransactionByUserQuery;
import com.transaction.api.adapters.model.SummaryQuery;
import com.transaction.api.adapters.model.FilterCommon;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final ITransactionPort transactionPort;
    private final TransactionQueryMapper mapper;

    public TransactionController(ITransactionPort transactionPort, TransactionQueryMapper mapper) {
        this.transactionPort = transactionPort;
        this.mapper = mapper;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDetail> transactionId(@PathVariable String transactionId) throws BadRequestException {
        log.info("/api/v1/transactions/transactionId {}", transactionId);
        TransactionDetail transactionDetail = transactionPort.transactionId(transactionId);
        return ResponseEntity.ok(transactionDetail);
    }

    @GetMapping("/search/cbu/{cbu}")
    public ResponseEntity<?> transactionCbu(@PathVariable  String cbu,
                                            @RequestParam(required=false) LocalDate txDateFrom,
                                            @RequestParam(required=false) LocalDate txDateTo,
                                            @RequestParam(required=false) LocalDate ingestionDateFrom,
                                            @RequestParam(required=false) LocalDate ingestionDateTo,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = "transactionAt,desc") String sort) throws BadRequestException {
        log.info("/api/v1/transactions/search/cbu");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/search/cuit/{cuit}")
    public ResponseEntity<?> transactionCuit(@PathVariable String cuit,
                                             @RequestParam(required=false) LocalDate txDateFrom,
                                             @RequestParam(required=false) LocalDate txDateTo,
                                             @RequestParam(required=false) LocalDate ingestionDateFrom,
                                             @RequestParam(required=false) LocalDate ingestionDateTo,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(defaultValue = "transactionAt,desc") String sort) throws BadRequestException {
        log.info("/api/v1/transactions/search/cuit/");
        return ResponseEntity.ok("OK");
    }



    @GetMapping("/search/user/{userId}")
    public ResponseEntity<TransactionPage>searchTransactionByUser(@PathVariable String userId,
                                                                  @ModelAttribute TransactionFilterRequest request)  {
        log.info("Searching transactions for userId: {}", userId);

        SearchTransactionByUserQuery searchTransactionByUserQuery = mapper.toSearchTransactionByUserQuery(userId, request);

        TransactionPage response = transactionPort.searchTransactionByUser(searchTransactionByUserQuery);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<TransactionPage> listTransactions(@ModelAttribute ListTransactionRequest request) {
        log.info("Listing transactions with page: {}, size: {}, sort: {}", request.page(), request.size(), request.sort());

        ListTransactionsQuery listTransactionQuery = mapper.toListTransactionQuery(request);

        TransactionPage response = transactionPort.listTransaction(listTransactionQuery);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> getSummary(@ModelAttribute SummaryRequest request) {
        log.info("Getting transaction summary");

        SummaryQuery summaryQuery = mapper.toSummaryQuery(request);

        TransactionSummary response = transactionPort.getSummary(summaryQuery);

        return ResponseEntity.ok(response);
    }
}
