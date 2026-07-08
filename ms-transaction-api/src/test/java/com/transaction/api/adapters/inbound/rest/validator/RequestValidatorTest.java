package com.transaction.api.adapters.inbound.rest.validator;

import com.transaction.api.domain.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidatorTest {

    private RequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RequestValidator();
    }

    @Test
    void shouldNotThrowWhenAllValuesAreValid() {
        assertDoesNotThrow(() -> validator.validate(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 28),
                "DEBIT",
                "PENDING"
        ));
    }

    @Test
    void shouldNotThrowWhenTypeAndStatusAreNull() {
        assertDoesNotThrow(() -> validator.validate(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void shouldNotThrowWhenTypeAndStatusAreBlank() {
        assertDoesNotThrow(() -> validator.validate(
                null,
                null,
                null,
                null,
                " ",
                " "
        ));
    }

    @Test
    void shouldThrowWhenTransactionStatusIsInvalid() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validate(
                        null,
                        null,
                        null,
                        null,
                        "DEBIT",
                        "INVALID_STATUS"
                )
        );

        assertEquals("transactionStatus inválido: INVALID_STATUS", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTransactionTypeIsInvalid() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validate(
                        null,
                        null,
                        null,
                        null,
                        "INVALID_TYPE",
                        "PENDING"
                )
        );

        assertEquals("transactionType inválido: INVALID_TYPE", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTxDateFromIsAfterTxDateTo() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validate(
                        LocalDate.of(2024, 2, 1),
                        LocalDate.of(2024, 1, 1),
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals("txDateFrom must not be after txDateTo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenIngestionDateFromIsAfterIngestionDateTo() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validate(
                        null,
                        null,
                        LocalDate.of(2024, 3, 10),
                        LocalDate.of(2024, 3, 1),
                        null,
                        null
                )
        );

        assertEquals("ingestionDateFrom must not be after ingestionDateTo", ex.getMessage());
    }

    @Test
    void shouldNotThrowWhenDatesAreEqual() {
        LocalDate sameDate = LocalDate.of(2024, 1, 1);

        assertDoesNotThrow(() -> validator.validate(
                sameDate,
                sameDate,
                sameDate,
                sameDate,
                "CREDIT",
                "COMPLETED"
        ));
    }
}
