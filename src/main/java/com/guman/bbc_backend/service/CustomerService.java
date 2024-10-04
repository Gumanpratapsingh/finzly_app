package com.guman.bbc_backend.service;

import com.guman.bbc_backend.auth.LoginService;
import com.guman.bbc_backend.dto.CustomerDTO;
import com.guman.bbc_backend.entity.ConsumerID;
import com.guman.bbc_backend.entity.Customer;
import com.guman.bbc_backend.entity.DueAmount;
import com.guman.bbc_backend.entity.Invoice;
import com.guman.bbc_backend.repository.CustomerRepository;
import com.guman.bbc_backend.repository.DueAmountRepository;
import com.guman.bbc_backend.repository.InvoiceRepository;
import com.guman.bbc_backend.repository.ConsumerIDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;



    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LoginService loginService;

    @Autowired
    private DueAmountRepository dueAmountRepository;

    @Autowired
    private ConsumerIDRepository consumerIDRepository;

    @Transactional
    public Customer addCustomer(CustomerDTO customerDTO, String token) {
        System.out.println("Received customer data: " + customerDTO);
        if (!loginService.isValidSession(token.substring(7))) {
            throw new RuntimeException("Invalid or expired token");
        }

        try {
            // Check if the connection ID already exists
            if (consumerIDRepository.existsByConnectionId(customerDTO.getConnectionId())) {
                throw new RuntimeException("Connection ID is already in use. Please use another Connection ID.");
            }

            // Check if customer exists by phone number or email
            Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(customerDTO.getPhoneNumber());
            if (existingCustomer.isEmpty()) {
                existingCustomer = customerRepository.findByEmail(customerDTO.getEmail());
            }

            if (existingCustomer.isPresent()) {
                Customer customer = existingCustomer.get();
                // Update existing customer information
                customer.setName(customerDTO.getName());
                customer.setBillingAddress(customerDTO.getBillingAddress());
                customer = customerRepository.save(customer);

                // Handle ConsumerID and Invoice creation
                handleConsumerIDAndInvoice(customer, customerDTO);

                return customer;
            } else {
                // Create new Customer
                Customer newCustomer = createCustomer(customerDTO);

                // Handle ConsumerID and Invoice creation
                handleConsumerIDAndInvoice(newCustomer, customerDTO);

                return newCustomer;
            }
        } catch (Exception e) {
            System.err.println("Error in addCustomer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error adding customer: " + e.getMessage());
        }
    }

    private void handleConsumerIDAndInvoice(Customer customer, CustomerDTO customerDTO) {
        Optional<ConsumerID> existingConsumerOptional = consumerIDRepository.findByUserIdAndConnectionId(customer.getUserId(), customerDTO.getConnectionId());

        if (existingConsumerOptional.isPresent()) {
            ConsumerID existingConsumer = existingConsumerOptional.get();
            // Check for date overlap
            List<Invoice> existingInvoices = invoiceRepository.findByConsumerId(existingConsumer.getConsumerId());
            boolean hasOverlap = existingInvoices.stream()
                    .anyMatch(invoice -> isDateOverlap(invoice, customerDTO));

            if (hasOverlap) {
                throw new RuntimeException("Date overlap with existing invoice");
            }

            // Create new Invoice
            createInvoice(existingConsumer, customerDTO);
        } else {
            // Create new ConsumerID
            ConsumerID newConsumer = createConsumerID(customer, customerDTO);
            createInvoice(newConsumer, customerDTO);
        }
    }

    private boolean isDateOverlap(Invoice latestInvoice, CustomerDTO customerDTO) {
        return (customerDTO.getBillingStartDate().isBefore(latestInvoice.getBillingEndDate()) &&
                customerDTO.getBillingEndDate().isAfter(latestInvoice.getBillingStartDate()));
    }

    private Customer createCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setBillingAddress(customerDTO.getBillingAddress());
        customer.setCreatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    private ConsumerID createConsumerID(Customer customer, CustomerDTO customerDTO) {
        ConsumerID consumerID = new ConsumerID();
        consumerID.setUserId(customer.getUserId());
        consumerID.setConnectionId(customerDTO.getConnectionId());
        consumerID.setCreatedAt(LocalDateTime.now());
        return consumerIDRepository.save(consumerID);
    }

    private Invoice createInvoice(ConsumerID consumerID, CustomerDTO customerDTO) {
        // Check if there are any unpaid invoices for this consumer
        List<Invoice> unpaidInvoices = invoiceRepository.findByConsumerIdAndStatus(
                consumerID.getConsumerId(), Invoice.InvoiceStatus.PENDING);

        if (!unpaidInvoices.isEmpty()) {
            throw new RuntimeException("Cannot generate a new invoice. There are unpaid invoices for this consumer.");
        }

        Invoice invoice = new Invoice();
        invoice.setConsumerId(consumerID.getConsumerId());
        invoice.setBillingStartDate(customerDTO.getBillingStartDate());
        invoice.setBillingEndDate(customerDTO.getBillingEndDate());
        invoice.setBillDueDate(customerDTO.getBillDueDate());
        invoice.setUnitConsumed(BigDecimal.valueOf(customerDTO.getUnitConsumption()));

        BigDecimal amountDue = calculateAmountDue(customerDTO.getUnitConsumption());
        invoice.setAmountDue(amountDue);
        invoice.setFinalAmount(amountDue);
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());

        // Set default values for discount fields
        invoice.setEarlyPaymentDiscount(BigDecimal.ZERO);
        invoice.setOnlinePaymentDiscount(BigDecimal.ZERO);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create DueAmount entry
        DueAmount dueAmount = new DueAmount();
        dueAmount.setConsumerId(consumerID.getConsumerId());
        //added new line not useful to me for santosh
        dueAmount.setBillDueDate(customerDTO.getBillDueDate());
        dueAmount.setConnectionId(consumerID.getConnectionId());
        dueAmount.setUserId(consumerID.getUserId());
        dueAmount.setAmount(amountDue);
        dueAmount.setCreatedAt(LocalDateTime.now());
        dueAmountRepository.save(dueAmount);

        return savedInvoice;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    private Integer generateCustomerId() {
        Random random = new Random();
        return 100000 + random.nextInt(900000);
    }
    private Integer generateUserId() {
        Random random = new Random();
        return 100000 + random.nextInt(900000);
    }

    private BigDecimal calculateAmountDue(Double unitConsumption) {
        BigDecimal ratePerUnit = BigDecimal.valueOf(41.50);
        return BigDecimal.valueOf(unitConsumption).multiply(ratePerUnit);
    }

    // @Transactional
    // public void addCustomers(List<CustomerDTO> customerDTOs, String token) {
    //     if (!loginService.isValidSession(token.substring(7))) {
    //         throw new RuntimeException("Invalid or expired token");
    //     }

    //     for (CustomerDTO customerDTO : customerDTOs) {
    //         try {
    //             addCustomer(customerDTO, token);
    //         } catch (Exception e) {
    //             // Log the error and continue processing other customers
    //             System.err.println("Error processing customer: " + e.getMessage());
    //         }
    //     }
    // }

    @Transactional
    public List<String> addCustomers(List<CustomerDTO> customerDTOs, String token) {
        if (!loginService.isValidSession(token.substring(7))) {
            throw new RuntimeException("Invalid or expired token");
        }

        List<String> duplicateEntries = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();
        List<ConsumerID> consumerIDs = new ArrayList<>();
        List<Invoice> invoices = new ArrayList<>();

        for (CustomerDTO customerDTO : customerDTOs) {
            try {
                // Check for existing customer
                Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(customerDTO.getPhoneNumber());
                if (existingCustomer.isEmpty()) {
                    existingCustomer = customerRepository.findByEmail(customerDTO.getEmail());
                }

                if (existingCustomer.isPresent()) {
                    // Add to duplicate entries list
                    duplicateEntries.add("Duplicate entry found for phone: " + customerDTO.getPhoneNumber() + " or email: " + customerDTO.getEmail());
                    continue; // Skip processing this entry
                }

                // Check if the connection ID already exists
                if (consumerIDRepository.existsByConnectionId(customerDTO.getConnectionId())) {
                    duplicateEntries.add("Duplicate Connection ID found: " + customerDTO.getConnectionId());
                    continue; // Skip processing this entry
                }

                Customer customer = createCustomer(customerDTO);
                customers.add(customer);

                ConsumerID consumerID = createConsumerID(customer, customerDTO);
                consumerIDs.add(consumerID);

                Invoice invoice = createInvoice(consumerID, customerDTO);
                invoices.add(invoice);
            } catch (Exception e) {
                // Log the error and continue processing other customers
                System.err.println("Error processing customer: " + e.getMessage());
                duplicateEntries.add("Error processing customer " + customerDTO.getName() + ": " + e.getMessage());
            }
        }

        customerRepository.saveAll(customers);
        consumerIDRepository.saveAll(consumerIDs);
        invoiceRepository.saveAll(invoices);

        return duplicateEntries;
    }

    public List<String> getConnectionIdsByPhoneNumber(String phoneNumber) {
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found with phone number: " + phoneNumber));
        return consumerIDRepository.findByUserId(customer.getUserId()).stream()
                .map(ConsumerID::getConnectionId)
                .collect(Collectors.toList());
    }
}