package com.transaction.api.adapters.inbound.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> transactionId(@PathVariable String transactionId) throws BadRequestException {
        log.info("/api/v1/transactions/transactionId {}", transactionId);
        return ResponseEntity.ok("OK");
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




}
