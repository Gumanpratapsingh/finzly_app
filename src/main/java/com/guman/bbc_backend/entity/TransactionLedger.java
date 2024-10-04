package com.guman.bbc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Data
@Table(name = "transactionledger")
public class TransactionLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "consumer_id")
    private Integer consumerId;

    @Column(name = "user_name")
    private String userName;

    private BigDecimal amount;

    @Column(name = "mode_of_payment")
    @Enumerated(EnumType.STRING)
    private ModeOfPayment modeOfPayment;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "connection_id", nullable = false, unique = true)
    private String connectionId;

//    // Enums
//    public enum ModeOfPayment {
//        ONLINE, CASH, CHEQUE
//    }

//    public enum TransactionStatus {
//        SUCCESS, FAILURE, PENDING
//    }
}