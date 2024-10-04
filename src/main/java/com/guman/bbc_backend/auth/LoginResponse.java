package com.guman.bbc_backend.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginResponse {
    private String token;
    private String message;
    private boolean newUser;
    @Setter
    @Getter
    private boolean existingUser;
    @Setter
    @Getter
    private String name;

}