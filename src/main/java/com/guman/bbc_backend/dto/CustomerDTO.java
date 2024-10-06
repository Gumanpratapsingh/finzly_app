package com.guman.bbc_backend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class CustomerDTO {
    private String name;
    private String email;
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    private String billingAddress;
    private Double unitConsumption;
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private LocalDate billDueDate;
    private Double amountDue;
    private String status;
    @Getter
    @Setter
    @Pattern(regexp = "^CUST\\d+$", message = "Connection ID must start with CUST followed by numbers")
    private String connectionId;

}