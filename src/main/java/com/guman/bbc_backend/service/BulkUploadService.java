package com.guman.bbc_backend.service;

import com.guman.bbc_backend.UploadSummary;
import com.guman.bbc_backend.dto.CustomerCSVDTO;
import com.guman.bbc_backend.dto.CustomerDTO;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkUploadService {

    @Autowired
    private CustomerService customerService;

//    @Autowired
//    ThreadPoolTaskExecutor processBatchExecutor;
    public UploadSummary processCsvFile(MultipartFile file, String token) {
        List<String> errors = new ArrayList<>();
        List<String> duplicateEntries = new ArrayList<>();
        int totalRecords = 0;
        int successfulUploads = 0;
        int failedUploads = 0;
        int batchSize = 1000; // Adjust this value based on your needs

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<CustomerCSVDTO> csvToBean = new CsvToBeanBuilder<CustomerCSVDTO>(reader)
                    .withType(CustomerCSVDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<CustomerCSVDTO> batch = new ArrayList<>(batchSize);
            for (CustomerCSVDTO csvCustomer : csvToBean) {
                totalRecords++;
                batch.add(csvCustomer);

                if (batch.size() >= batchSize) {
                    int batchSuccessCount = processBatch(batch, token, errors, duplicateEntries);
                    successfulUploads += batchSuccessCount;
                    failedUploads += batch.size() - batchSuccessCount;
                    batch.clear();
                }
            }

            // Process remaining customers
            if (!batch.isEmpty()) {
                int batchSuccessCount = processBatch(batch, token, errors, duplicateEntries);
                successfulUploads += batchSuccessCount;
                failedUploads += batch.size() - batchSuccessCount;
            }
        } catch (Exception e) {
            errors.add("Error processing CSV file: " + e.getMessage());
        }

        // Add duplicate entries to the errors list
        errors.addAll(duplicateEntries);

        return new UploadSummary(totalRecords, successfulUploads, failedUploads, errors);
    }

//    not working due to DB LOCK ERROR ad sequentially kar rha hu rather than multithreading use krke
//    public UploadSummary processCsvFile(MultipartFile file, String token) {
//        List<String> errors = new ArrayList<>();
//        List<String> duplicateEntries = new ArrayList<>();
//        int totalRecords = 0;
//        //int successfulUploads = 0;
//        List<List<CustomerCSVDTO>> batches = new ArrayList<>();
//        int batchSize = 1000; // Adjust this value
//
//        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
//            CsvToBean<CustomerCSVDTO> csvToBean = new CsvToBeanBuilder<CustomerCSVDTO>(reader)
//                    .withType(CustomerCSVDTO.class)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            List<CustomerCSVDTO> batch = new ArrayList<>();
//
//            for (CustomerCSVDTO csvCustomer : csvToBean) {
//                totalRecords++;
//                batch.add(csvCustomer);
//
//                if (batch.size() >= batchSize) {
//                    batches.add(new ArrayList<>(batch));
//
////                    successfulUploads += processBatch(batch, token, errors, duplicateEntries);
//                    batch.clear();
//                }
//            }
//
//            // Process remaining customers
//            if (!batch.isEmpty()) {
//                batches.add(new ArrayList<>(batch));
//
////                successfulUploads += processBatch(batch, token, errors, duplicateEntries);
//            }
//            processBatchesInParallel(batches, token, errors, duplicateEntries);
//
//        } catch (Exception e) {
//            errors.add("Error processing CSV file: " + e.getMessage());
//        }
//
//        int successfulUploads = totalRecords - errors.size() - duplicateEntries.size();
//        int failedUploads = errors.size();
//        // Add duplicate entries to the errors list
//        errors.addAll(duplicateEntries);
//
//        return new UploadSummary(totalRecords, successfulUploads, failedUploads, errors);
//    }

    private int processBatch(List<CustomerCSVDTO> batch, String token, List<String> errors, List<String> duplicateEntries) {
        List<CustomerDTO> customerDTOs = new ArrayList<>(batch.size());
        for (CustomerCSVDTO csvCustomer : batch) {
            try {
                validateCustomerData(csvCustomer);
                customerDTOs.add(convertToCustomerDTO(csvCustomer));
            } catch (Exception e) {
                errors.add("Error processing customer " + csvCustomer.getName() + ": " + e.getMessage());
            }
        }
        List<String> batchDuplicates = customerService.addCustomers(customerDTOs, token);
        duplicateEntries.addAll(batchDuplicates);
        return customerDTOs.size() - batchDuplicates.size();
    }

//    not using now ab upar krdiya h declare ek sath hi
//    private void processCustomer(CustomerCSVDTO csvCustomer, String token) throws Exception {
//        validateCustomerData(csvCustomer);
//        CustomerDTO customerDTO = convertToCustomerDTO(csvCustomer);
//        customerService.addCustomer(customerDTO, token);
//    }

//    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//    private void processBatchesInParallel(List<List<CustomerCSVDTO>> batches, String token, List<String> errors, List<String> duplicateEntries) {
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        int delayBetweenBatches = 5000;
//        for (List<CustomerCSVDTO> batch : batches) {
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                processBatch(batch, token, errors, duplicateEntries);
//                }, executorService);
//            futures.add(future);
//        }
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//    }
    private void validateCustomerData(CustomerCSVDTO customer) throws Exception {
        StringBuilder errors = new StringBuilder();

        if (customer.getName() == null || customer.getName().isEmpty()) {
            errors.append("Name is required. ");
        }
        // if (customer.getConsumerId() == null || customer.getConsumerId().isEmpty()) {
        //     errors.append("Customer ID is required. ");
        // }
        if (customer.getUnitConsumed() == null) {
            errors.append("Unit Consumed is required. ");
        }
        if (customer.getBillDueDate() == null) {
            errors.append("Bill Due Date is required. ");
        }
        if (customer.getBillingStartDate() == null) {
            errors.append("Billing Start Date is required. ");
        }
        if (customer.getBillingEndDate() == null) {
            errors.append("Billing End Date is required. ");
        }
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            errors.append("Email is required. ");
        }
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
            errors.append("Phone Number is required. ");
        } else {
            String formattedPhoneNumber = formatPhoneNumber(customer.getPhoneNumber());
            if (formattedPhoneNumber.length() != 10) {
                errors.append("Phone number must be 10 digits after formatting. ");
            }
        }

        if (customer.getConnectionId() == null || customer.getConnectionId().isEmpty()) {
            errors.append("Connection ID is required. ");
        }

        if (errors.length() > 0) {
            throw new Exception(errors.toString().trim());
        }
    }

    private CustomerDTO convertToCustomerDTO(CustomerCSVDTO csvCustomer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName(csvCustomer.getName());
        String connectionId = csvCustomer.getConnectionId();
        if (!connectionId.startsWith("CUST")) {
            connectionId = "CUST" + connectionId;
        }
        customerDTO.setConnectionId(connectionId);
        customerDTO.setUnitConsumption(csvCustomer.getUnitConsumed());
        customerDTO.setBillDueDate(csvCustomer.getBillDueDate());
        customerDTO.setBillingStartDate(csvCustomer.getBillingStartDate());
        customerDTO.setBillingEndDate(csvCustomer.getBillingEndDate());
        customerDTO.setEmail(csvCustomer.getEmail());
        customerDTO.setPhoneNumber(formatPhoneNumber(csvCustomer.getPhoneNumber()));
        customerDTO.setBillingAddress(csvCustomer.getBillingAddress());
        return customerDTO;
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "";
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        // If the number is longer than 10 digits, take the last 10
        if (digitsOnly.length() > 10) {
            digitsOnly = digitsOnly.substring(digitsOnly.length() - 10);
        }
        
        // If the number is shorter than 10 digits, pad with zeros
        while (digitsOnly.length() < 10) {
            digitsOnly = "0" + digitsOnly;
        }
        
        return digitsOnly;
    }
}