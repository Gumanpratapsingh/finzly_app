package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.dto.CustomerDTO;
import com.guman.bbc_backend.entity.Customer;
import com.guman.bbc_backend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@Valid @RequestBody CustomerDTO customerDTO, @RequestHeader("Authorization") String token, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            Customer createdCustomer = customerService.addCustomer(customerDTO, token);
            return ResponseEntity.ok().body(createdCustomer);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection ID is already in use")) {
                return ResponseEntity.badRequest().body("Error adding customer: " + e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error adding customer: " + e.getMessage());
        }
    }

    @GetMapping("/connection-ids/{phoneNumber}")
    public ResponseEntity<List<String>> getConnectionIds(@PathVariable String phoneNumber) {
        try {
            List<String> connectionIds = customerService.getConnectionIdsByPhoneNumber(phoneNumber);
            return ResponseEntity.ok(connectionIds);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // @GetMapping("/{phoneNumber}/connections")
    // public ResponseEntity<List<String>> getConnectionIds(@PathVariable String phoneNumber) {
    //     List<String> connectionIds = customerService.getConnectionIdsByPhoneNumber(phoneNumber);
    //     return ResponseEntity.ok(connectionIds);
    // }
}