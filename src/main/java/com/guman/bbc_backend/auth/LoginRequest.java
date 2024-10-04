// LoginRequest.java
package com.guman.bbc_backend.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String otp;
}