package com.guman.bbc_backend.auth;

import com.guman.bbc_backend.entity.Employee;
import com.guman.bbc_backend.repository.EmployeeRepository;
import com.guman.bbc_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, EmployeeSession> sessionStore = new HashMap<>();


    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        
        Employee employee = employeeRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (employee != null) {
            // Existing employee, create session and return token
            EmployeeSession session = createSession(employee);
            response.setToken(session.getToken());
            response.setName(employee.getName() != null ? employee.getName() : "Employee");
            response.setMessage("Login successful");
            response.setExistingUser(true);
        } else {
            // New employee, generate and send OTP
            String otp = generateOtp();
            otpStore.put(loginRequest.getEmail(), otp);
            emailService.sendOtpEmail(loginRequest.getEmail(), otp);
            response.setMessage("OTP sent to your email");
            response.setExistingUser(false);
        }
        return response;
    }

    public LoginResponse verifyOtp(String email, String otp) {
        LoginResponse response = new LoginResponse();
        
        if (otp.equals(otpStore.get(email))) {
            String username = extractUsernameFromEmail(email);
            Employee newEmployee = new Employee();
            newEmployee.setEmail(email);
            newEmployee.setName(username);
            newEmployee.setCreatedAt(LocalDateTime.now());
            employeeRepository.save(newEmployee);

            EmployeeSession session = createSession(newEmployee);
            response.setToken(session.getToken());
            response.setMessage("OTP verified and account created");
            otpStore.remove(email);
        } else {
            response.setMessage("Invalid OTP");
        }
        return response;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public EmployeeSession createSession(Employee employee) {
        EmployeeSession session = new EmployeeSession();
        session.setEmployeeId(employee.getEmployeeId());
        session.setToken(generateSessionToken());
        //System.out.println(generateSessionToken());
        session.setExpirationTime(LocalDateTime.now().plusHours(1)); // Session expires in 1 hour
        sessionStore.put(session.getToken(), session);
        return session;
    }

    private String generateSessionToken() {
        //Generating a Random UUID without any BEARER PreFix
        return UUID.randomUUID().toString();
    }

    public boolean isValidSession(String token) {
        EmployeeSession session = sessionStore.get(token);
        if (session == null || !session.isValid()) {
            sessionStore.remove(token); // Remove if the specific session is expired or doesn't exist
            return false;
        }
        return true;
    }


    public void invalidateSession(String token) {
        sessionStore.remove(token);
    }
    @Scheduled(fixedRate = 3600000) // Runs every hour (3,600,000 milliseconds) to clear our sessionStore hashmap from any outdate entry
    public void cleanupExpiredSessions() {
        sessionStore.entrySet().removeIf(entry -> {
            boolean isExpired = !entry.getValue().isValid();
            if (isExpired) {
                System.out.println("Removing expired session token: " + entry.getKey());
            }
            return isExpired;
        });
    }


//    we are not using this as every single time when employee is login in we are creating a new session for them
//    public Employee getEmployeeByToken(String token) {
//        EmployeeSession session = sessionStore.get(token);
//        if (session != null && session.isValid()) {
////            return employeeRepository.findById(session.getEmployeeId()).orElse(null);
//        }
//        return null;
//    }

    private String extractUsernameFromEmail(String email) {
        return email.split("@")[0];
    }
}