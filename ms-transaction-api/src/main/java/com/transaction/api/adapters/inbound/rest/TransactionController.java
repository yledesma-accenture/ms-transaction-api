package com.transaction.api.adapters.inbound.rest;

import com.transaction.api.domain.model.TransactionPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

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
}
