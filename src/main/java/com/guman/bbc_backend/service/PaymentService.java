package com.guman.bbc_backend.service;

import com.guman.bbc_backend.dto.PaymentRequest;
import com.guman.bbc_backend.entity.*;
import com.guman.bbc_backend.repository.ConsumerIDRepository;
import com.guman.bbc_backend.repository.CustomerRepository;
import com.guman.bbc_backend.repository.InvoiceRepository;
import com.guman.bbc_backend.repository.TransactionLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TransactionLedgerRepository transactionLedgerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ConsumerIDRepository consumerIDRepository;

    @Transactional
    public Invoice processPayment(PaymentRequest paymentRequest) {
        Invoice invoice = invoiceRepository.findById(paymentRequest.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Refresh the invoice status from the database
        invoiceRepository.refresh(invoice);

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is already paid");
        }

        BigDecimal paymentAmount = BigDecimal.valueOf(paymentRequest.getAmount());
        if (paymentAmount.compareTo(invoice.getAmountDue()) > 0) {
            throw new RuntimeException("Payment amount exceeds the due amount");
        }

        // Apply discounts
        BigDecimal discountedAmount = applyDiscounts(invoice, paymentRequest.getPaymentMethod());

        // Calculate the final amount paid after discounts
        BigDecimal finalAmountPaid = paymentAmount.min(discountedAmount);

        // Update invoice
        invoice.setFinalAmount(finalAmountPaid);
        BigDecimal remainingAmount = discountedAmount.subtract(finalAmountPaid);
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDate.now());
            invoice.setAmountDue(BigDecimal.ZERO);
        } else {
            invoice.setAmountDue(remainingAmount);
        }
        
        // Save the updated invoice
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Create transaction record
        createTransactionRecord(updatedInvoice, finalAmountPaid, paymentRequest.getPaymentMethod());

        return updatedInvoice;
    }

    private BigDecimal applyDiscounts(Invoice invoice, String paymentMethod) {
        BigDecimal discountedAmount = invoice.getAmountDue();
        LocalDate currentDate = LocalDate.now();

        // Apply early payment discount
        if (currentDate.isBefore(invoice.getBillDueDate())) {
            BigDecimal earlyPaymentDiscount = calculateEarlyPaymentDiscount(invoice.getAmountDue());
            discountedAmount = discountedAmount.subtract(earlyPaymentDiscount);
            invoice.setEarlyPaymentDiscount(earlyPaymentDiscount);
        }

        // Apply online payment discount
        if (!paymentMethod.equalsIgnoreCase("CASH")) {
            BigDecimal onlinePaymentDiscount = calculateOnlinePaymentDiscount(invoice.getAmountDue());
            discountedAmount = discountedAmount.subtract(onlinePaymentDiscount);
            invoice.setOnlinePaymentDiscount(onlinePaymentDiscount);
        }

        return discountedAmount;
    }

    private BigDecimal calculateEarlyPaymentDiscount(BigDecimal amount) {
        // Implement your early payment discount calculation logic here
        // For example, 5% discount
        return amount.multiply(new BigDecimal("0.05"));
    }

    private BigDecimal calculateOnlinePaymentDiscount(BigDecimal amount) {
        // Implement your online payment discount calculation logic here
        // For example, 2% discount
        return amount.multiply(new BigDecimal("0.05"));
    }

    private void createTransactionRecord(Invoice invoice, BigDecimal paymentAmount, String paymentMethod) {
        ConsumerID consumerID = consumerIDRepository.findById(invoice.getConsumerId())
                .orElseThrow(() -> new RuntimeException("Consumer ID not found for this invoice"));
        Customer customer = customerRepository.findById(consumerID.getUserId())
                .orElseThrow(() -> new RuntimeException("Customer not found for this invoice"));

        TransactionLedger transaction = new TransactionLedger();
        transaction.setUserId(customer.getUserId());
        transaction.setConsumerId(invoice.getConsumerId());
        transaction.setUserName(customer.getName());
        transaction.setAmount(paymentAmount);
        transaction.setModeOfPayment(convertPaymentMethod(paymentMethod));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceNumber(generateReferenceNumber());
        //santosh need
        transaction.setConnectionId(consumerID.getConnectionId()); // Set the connectionId

        transactionLedgerRepository.save(transaction);
    }

    private String generateReferenceNumber() {
        return "REF-" + System.currentTimeMillis();
    }

    private ModeOfPayment convertPaymentMethod(String paymentMethod) {
        try {
            return ModeOfPayment.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment method: " + paymentMethod);
        }
    }

    private String generateConnectionId() {
        return "CONN-" + System.currentTimeMillis();
    }
}