package com.guman.bbc_backend.controller;

import com.guman.bbc_backend.UploadResponse;
import com.guman.bbc_backend.UploadStatus;
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
            String jobId = bulkUploadService.startProcessCsvFile(file, token);
            return ResponseEntity.ok(new UploadResponse(true, "Processing started", List.of("Job ID: " + jobId)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new UploadResponse(false, "Error initiating processing", List.of(e.getMessage())));
        }
    }

    @GetMapping("/customers/status/{jobId}")
    public ResponseEntity<?> getUploadStatus(@PathVariable String jobId) {
        UploadStatus status = bulkUploadService.getUploadStatus(jobId);
        return ResponseEntity.ok(status);
    }
}