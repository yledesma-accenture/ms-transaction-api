package com.transaction.api.adapters.inbound.rest;

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

    public TransactionController(ITransactionPort transactionPort) {
        this.transactionPort = transactionPort;
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
                                                                  @RequestParam(required=false) LocalDate txDateFrom,
                                                                  @RequestParam(required=false) LocalDate txDateTo,
                                                                  @RequestParam(required=false) LocalDate ingestionDateFrom,
                                                                  @RequestParam(required=false) LocalDate ingestionDateTo,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(defaultValue = "transactionAt,desc") String sort)  {
        log.info("Searching transactions for userId: {}", userId);


        FilterCommon filterCommon = FilterCommon.builder()
                .txDateFrom(txDateFrom)
                .txDateTo(txDateTo)
                .ingestionDateFrom(ingestionDateFrom)
                .ingestionDateTo(ingestionDateTo)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        SearchTransactionByUserQuery searchTransactionByUserQuery = SearchTransactionByUserQuery.builder()
                .userId(Long.valueOf(userId))
                .filterCommon(filterCommon)
                .build();

        TransactionPage response = transactionPort.searchTransactionByUser(searchTransactionByUserQuery);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<TransactionPage> listTransactions(
            @RequestParam(required=false) LocalDate txDateFrom,
            @RequestParam(required=false) LocalDate txDateTo,
            @RequestParam(required=false) LocalDate ingestionDateFrom,
            @RequestParam(required=false) LocalDate ingestionDateTo,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax,
            @RequestParam(required = false) UUID fileId,
            @RequestParam(required = false) Boolean flagged,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionAt,desc") String sort) {
        log.info("Listing transactions with page: {}, size: {}, sort: {}", page, size, sort);

        FilterCommon filterCommon = FilterCommon.builder()
                .txDateFrom(txDateFrom)
                .txDateTo(txDateTo)
                .ingestionDateFrom(ingestionDateFrom)
                .ingestionDateTo(ingestionDateTo)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        ListTransactionsQuery listTransactionsQuery = ListTransactionsQuery.builder()
                .filterCommon(filterCommon)
                .transactionType(type)
                .transactionStatus(status)
                .currency(currency)
                .amountMin(amountMin)
                .amountMax(amountMax)
                .fileId(fileId)
                .flagged(flagged)
                .build();

        TransactionPage response = transactionPort.listTransaction(listTransactionsQuery);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> getSummary(
            @RequestParam(required=false) LocalDate txDateFrom,
            @RequestParam(required=false) LocalDate txDateTo,
            @RequestParam(required=false) LocalDate ingestionDateFrom,
            @RequestParam(required=false) LocalDate ingestionDateTo,
            @RequestParam(defaultValue = "type") String groupBy) {
        log.info("Getting transaction summary");

        SummaryQuery summaryQuery = SummaryQuery.builder()
                .txDateFrom(txDateFrom)
                .txDateTo(txDateTo)
                .ingestionDateFrom(ingestionDateFrom)
                .ingestionDateTo(ingestionDateTo)
                .groupBy(groupBy)
                .build();

        TransactionSummary response = transactionPort.getSummary(summaryQuery);

        return ResponseEntity.ok(response);
    }
}
