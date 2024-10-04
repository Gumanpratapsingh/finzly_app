package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.dto.PaymentRequest;
import com.guman.bbc_backend.entity.Invoice;
import com.guman.bbc_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            Invoice updatedInvoice = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok().body(Map.of(
                "message", "Payment processed successfully",
                "updatedInvoice", updatedInvoice
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}