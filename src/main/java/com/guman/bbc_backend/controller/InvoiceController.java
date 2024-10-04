package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.entity.Invoice;
import com.guman.bbc_backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/process-payment/{invoiceId}")
    public ResponseEntity<Invoice> processPayment(
            @PathVariable Integer invoiceId,
            @RequestParam boolean isOnlinePayment) {
        Invoice processedInvoice = invoiceService.processPayment(invoiceId, isOnlinePayment);
        return ResponseEntity.ok(processedInvoice);
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Integer invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/by-phone/{phoneNumber}")
    public ResponseEntity<List<Invoice>> getInvoicesByPhoneNumber(@PathVariable String phoneNumber) {
        List<Invoice> invoices = invoiceService.getInvoicesByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/unpaid/{connectionId}")
    public ResponseEntity<List<Invoice>> getUnpaidInvoicesByConnectionId(@PathVariable String connectionId) {
        List<Invoice> invoices = invoiceService.getUnpaidInvoicesByConnectionId(connectionId);
        return ResponseEntity.ok(invoices);
    }

    // @GetMapping("/all/{connectionId}")
    // public ResponseEntity<List<Invoice>> getAllInvoicesByConnectionId(@PathVariable String connectionId) {
    //     List<Invoice> invoices = invoiceService.getAllInvoicesByConnectionId(connectionId);
    //     return ResponseEntity.ok(invoices);
    // }
}