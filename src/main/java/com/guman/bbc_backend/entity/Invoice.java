package com.guman.bbc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;

    @Column(name = "consumer_id", nullable = false)
    private Integer consumerId;

    @Column(name = "billing_start_date", nullable = false)
    private LocalDate billingStartDate;

    @Column(name = "billing_end_date", nullable = false)
    private LocalDate billingEndDate;

    @Column(name = "bill_due_date", nullable = false)
    private LocalDate billDueDate;

    @Column(name = "unit_consumed", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitConsumed;

    @Column(name = "amount_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "early_payment_discount", precision = 10, scale = 2)
    private BigDecimal earlyPaymentDiscount;

    @Column(name = "online_payment_discount", precision = 10, scale = 2)
    private BigDecimal onlinePaymentDiscount;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_date")
    private LocalDate paidDate;


    public enum InvoiceStatus {
        PENDING, PAID
    }
}