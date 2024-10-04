package com.guman.bbc_backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private boolean success;
    private String message;
    private List<String> errors;

    // Constructor, getters, and setters
}