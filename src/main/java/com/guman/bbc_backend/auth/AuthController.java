package com.guman.bbc_backend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private LoginService loginService;



    //we need this validate token to validate all the future url routes to only let the employee go ahead only if she/he is verified
    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            //System.out.println(authHeader);
            // we are using this substring to prevent any error from the HTTP CLIENT or FRONTEND FRAMEWORKS DURING SENDING ANY HTTP REQUEST
            String token = authHeader.substring(7);
            //System.out.println(token);
            boolean isValid = loginService.isValidSession(token);
            return ResponseEntity.ok(isValid);
        }
        return ResponseEntity.ok(false);
    }


   

}