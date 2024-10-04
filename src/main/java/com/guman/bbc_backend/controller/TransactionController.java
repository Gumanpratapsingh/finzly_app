package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.entity.TransactionLedger;
import com.guman.bbc_backend.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    public static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @GetMapping("/{consumerId}")
    public ResponseEntity<List<TransactionLedger>> getTransactions(@PathVariable Integer consumerId) {
        List<TransactionLedger> transactions = transactionService.getTransactionsByConsumerId(consumerId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody TransactionLedger transaction) {
        try {
            TransactionLedger processedTransaction = transactionService.processPayment(transaction);
            return ResponseEntity.ok(processedTransaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @GetMapping("/history/{phoneNumber}")
    public ResponseEntity<?> getTransactionsByPhoneNumber(@PathVariable String phoneNumber) {
        try {
            List<TransactionLedger> transactions = transactionService.getTransactionsByPhoneNumber(phoneNumber);
            if (transactions.isEmpty()) {
                logger.info("No transactions found for phone number: {}", phoneNumber);
                return ResponseEntity.ok().body(new ArrayList<>()); // Return an empty array instead of a string
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error fetching transactions for phone number {}: {}", phoneNumber, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ArrayList<>()); // Return an empty array for errors as well
        }
    }
}