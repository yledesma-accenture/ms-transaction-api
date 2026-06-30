package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    public ResponseEntity<TransactionPage> transactionCbu(@PathVariable  String cbu,
                                            @RequestParam(required=false) LocalDate txDateFrom,
                                            @RequestParam(required=false) LocalDate txDateTo,
                                            @RequestParam(required=false) LocalDate ingestionDateFrom,
                                            @RequestParam(required=false) LocalDate ingestionDateTo,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = "transactionAt,desc") String sort) throws BadRequestException {
        log.info("/api/v1/transactions/search/cbu");
        TransactionPage transactionPage = transactionPort.transactionCbu(cbu,txDateFrom,txDateTo,ingestionDateFrom,ingestionDateTo,page,size,sort);
        return ResponseEntity.ok(transactionPage);
    }

    @GetMapping("/search/cuit/{cuit}")
    public ResponseEntity<TransactionPage> transactionCuit(@PathVariable String cuit,
                                             @RequestParam(required=false) LocalDate txDateFrom,
                                             @RequestParam(required=false) LocalDate txDateTo,
                                             @RequestParam(required=false) LocalDate ingestionDateFrom,
                                             @RequestParam(required=false) LocalDate ingestionDateTo,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(defaultValue = "transactionAt,desc") String sort) throws BadRequestException {
        log.info("/api/v1/transactions/search/cuit/");
        TransactionPage transactionPage = transactionPort.transactionCuit(cuit,txDateFrom,txDateTo,ingestionDateFrom,ingestionDateTo,page,size,sort);
        return ResponseEntity.ok(transactionPage);
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
        return ResponseEntity.ok(new TransactionPage(List.of(), page, size, 0, 0, true));
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
        return ResponseEntity.ok(new TransactionPage(List.of(), page, size, 0, 0, true));
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> listTransactions(
            @RequestParam(required=false) LocalDate txDateFrom,
            @RequestParam(required=false) LocalDate txDateTo,
            @RequestParam(required=false) LocalDate ingestionDateFrom,
            @RequestParam(required=false) LocalDate ingestionDateTo,
            @RequestParam(defaultValue = "type") String groupBy) {
        log.info("Getting transaction summary");
        return ResponseEntity.ok(new TransactionSummary(txDateFrom, txDateTo, ingestionDateFrom, ingestionDateTo, 0, BigDecimal.ZERO, groupBy, List.of()));
    }



}
