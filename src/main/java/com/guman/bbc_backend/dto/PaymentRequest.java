package com.guman.bbc_backend.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Integer invoiceId;
    private Double amount;
    private String paymentMethod;
}