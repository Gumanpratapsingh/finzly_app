package com.guman.bbc_backend.auth;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeSession {
    private Integer employeeId;
    private String token;
    private LocalDateTime expirationTime;

    public boolean isValid() {
        return LocalDateTime.now().isBefore(expirationTime);
    }
}