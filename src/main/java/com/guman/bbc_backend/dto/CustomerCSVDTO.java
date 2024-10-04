package com.guman.bbc_backend.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerCSVDTO {
    @CsvBindByName(column = "Name")
    private String name;

    @CsvDate(value = "yyyy-MM-dd")
    @CsvBindByName(column = "Bill due date")
    private LocalDate billDueDate;

    // @CsvBindByName(column = "Customer ID")
    // private String consumerId;
    @CsvBindByName(column = "Connection ID")
    private String connectionId;

    @CsvBindByName(column = "Unit Consumed")
    private Double unitConsumed;

    @CsvDate(value = "yyyy-MM-dd")
    @CsvBindByName(column = "Billing Start Date")
    private LocalDate billingStartDate;

    @CsvDate(value = "yyyy-MM-dd")
    @CsvBindByName(column = "Billing End Date")
    private LocalDate billingEndDate;

    @CsvBindByName(column = "Amount Due")
    private Double amountDue;

    @CsvBindByName(column = "Status")
    private String status;

    @CsvBindByName(column = "User ID")
    private String userId;

    @CsvBindByName(column = "Billing Address")
    private String billingAddress;

    @CsvBindByName(column = "Email")
    private String email;

    @CsvBindByName(column = "Telephone")
    private String phoneNumber;
}
