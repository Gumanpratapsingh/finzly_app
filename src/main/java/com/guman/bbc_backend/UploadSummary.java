package com.guman.bbc_backend;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadSummary {
    private int totalRecords;
    private int successfulUploads;
    private int failedUploads;
    private List<String> errors;
}