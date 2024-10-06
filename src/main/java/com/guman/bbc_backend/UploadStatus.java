package com.guman.bbc_backend;

import lombok.Data;

@Data
public class UploadStatus {
    private boolean completed = false;
    private String error;
    private UploadSummary summary;
}
