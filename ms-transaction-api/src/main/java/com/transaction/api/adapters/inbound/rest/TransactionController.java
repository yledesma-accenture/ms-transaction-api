package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.adapters.inbound.dto.ListTransactionRequest;
import com.transaction.api.adapters.inbound.dto.SummaryRequest;
import com.transaction.api.adapters.inbound.dto.TransactionFilterRequest;
import com.transaction.api.adapters.model.*;
import com.transaction.api.domain.model.*;
import com.transaction.api.domain.port.application.ITransactionPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<TransactionPage> transactionCbu(@PathVariable  String cbu,
                                                          @ModelAttribute TransactionFilterRequest request) throws BadRequestException {
        log.info("/api/v1/transactions/search/cbu");
        SearchTransactionByCbuQuery searchTransactionByCbuQuery = mapper.toSearchTransactionByCbuQuery(cbu, request);

        TransactionPage transactionPage = transactionPort.transactionCbu(cbu,searchTransactionByCbuQuery);
        return ResponseEntity.ok(transactionPage);
    }

    @GetMapping("/search/cuit/{cuit}")
    public ResponseEntity<TransactionPage> transactionCuit(@PathVariable String cuit,
                                                           @ModelAttribute TransactionFilterRequest request) throws BadRequestException {
        log.info("/api/v1/transactions/search/cuit/");

        SearchTransactionByCuitQuery searchTransactionByCuitQuery = mapper.toSearchTransactionByCuitQuery(cuit, request);

        TransactionPage transactionPage = transactionPort.transactionCuit(cuit,searchTransactionByCuitQuery);
        return ResponseEntity.ok(transactionPage);
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
