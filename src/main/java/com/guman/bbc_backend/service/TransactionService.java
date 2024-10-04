package com.guman.bbc_backend.service;

import com.guman.bbc_backend.entity.*;
import com.guman.bbc_backend.repository.ConsumerIDRepository;
import com.guman.bbc_backend.repository.CustomerRepository;
import com.guman.bbc_backend.repository.InvoiceRepository;
import com.guman.bbc_backend.repository.TransactionLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.guman.bbc_backend.controller.TransactionController.logger;

@Service
public class TransactionService {

    @Autowired
    private TransactionLedgerRepository transactionLedgerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ConsumerIDRepository consumerIDRepository;

    public List<TransactionLedger> getTransactionsByConnectionId(String connectionId) {
        ConsumerID consumerID = consumerIDRepository.findByConnectionId(connectionId)
            .orElseThrow(() -> new RuntimeException("Consumer ID not found"));
        return transactionLedgerRepository.findByConsumerId(consumerID.getConsumerId());
    }

    @Transactional
    public TransactionLedger processPayment(TransactionLedger transaction) {
        try {
            // Validate the transaction
            if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount must be positive");
            }

            // Find the corresponding invoice
            Optional<Invoice> optionalInvoice = invoiceRepository.findTopByConsumerIdAndStatusOrderByBillingEndDateDesc(
                transaction.getConsumerId(), Invoice.InvoiceStatus.PENDING);

            if (optionalInvoice.isEmpty()) {
                throw new RuntimeException("No pending invoice found for this connection");
            }

            Invoice invoice = optionalInvoice.get();

            // Update the invoice
            BigDecimal remainingAmount = invoice.getAmountDue().subtract(transaction.getAmount());
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setStatus(Invoice.InvoiceStatus.PAID);
                invoice.setAmountDue(BigDecimal.ZERO);
            } else {
                invoice.setAmountDue(remainingAmount);
            }
            invoiceRepository.save(invoice);

            // Set transaction details
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setReferenceNumber(generateReferenceNumber());

            // Save and return the transaction
            return transactionLedgerRepository.save(transaction);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process payment", e);
        }
    }

    public List<TransactionLedger> getTransactionsByPhoneNumber(String phoneNumber) {
        logger.info("Fetching transactions for phone number: {}", phoneNumber);
        
        Customer customer;
        try {
            customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found with phone number: " + phoneNumber));
            logger.info("Found customer: {} with userId: {}", customer.getName(), customer.getUserId());
        } catch (RuntimeException e) {
            logger.error("Customer not found with phone number: {}", phoneNumber);
            return new ArrayList<>();
        }

        List<ConsumerID> consumerIDs = consumerIDRepository.findByUserId(customer.getUserId());
        if (consumerIDs.isEmpty()) {
            logger.warn("No ConsumerIDs found for userId: {}", customer.getUserId());
            return new ArrayList<>();
        }
        
        List<Integer> consumerIdList = consumerIDs.stream()
            .map(ConsumerID::getConsumerId)
            .collect(Collectors.toList());

        logger.info("Found {} consumer IDs for phone number {}: {}", consumerIdList.size(), phoneNumber, consumerIdList);

        List<TransactionLedger> transactions = new ArrayList<>();

        try {
            transactions = transactionLedgerRepository.findByConsumerIdInOrderByTransactionDateDesc(consumerIdList);
            logger.info("Found {} transactions for consumer IDs", transactions.size());
            if (transactions.isEmpty()) {
                logger.warn("No transactions found for consumer IDs: {}", consumerIdList);
            }
        } catch (Exception e) {
            logger.error("Error fetching transactions for consumer IDs: {}", e.getMessage(), e);
        }

        return transactions;
    }

    public List<TransactionLedger> getTransactionsByConsumerId(Integer consumerId) {
        return transactionLedgerRepository.findByConsumerId(consumerId);
    }

    private String generateReferenceNumber() {
        // Implement a method to generate a unique reference number
        return "REF-" + System.currentTimeMillis();
    }
}