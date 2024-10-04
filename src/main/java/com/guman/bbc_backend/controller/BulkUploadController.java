package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.UploadResponse;
import com.guman.bbc_backend.UploadSummary;
import com.guman.bbc_backend.service.BulkUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bulk-upload")
public class BulkUploadController {

    @Autowired
    private BulkUploadService bulkUploadService;

    @PostMapping("/customers")
    public ResponseEntity<?> uploadCustomers(@RequestParam("file") MultipartFile file,
                                             @RequestHeader("Authorization") String token) {
        try {
            UploadSummary summary = bulkUploadService.processCsvFile(file, token);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new UploadSummary(0, 0, 0, List.of("Error processing file: " + e.getMessage())));
        }
    }
}