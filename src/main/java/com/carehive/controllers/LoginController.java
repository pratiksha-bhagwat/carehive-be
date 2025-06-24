package com.carehive.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carehive.dtos.ApiResponse;
import com.carehive.entities.User;
import com.carehive.repositories.UserRepository;
import com.carehive.security.AuthResponse;
import com.carehive.security.JwtUtil;
import com.carehive.security.LoginRequest;
import com.carehive.services.LoginService;
import com.carehive.services.UserService;

@RestController
@RequestMapping("/user")
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    LoginService loginService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        
        try {
            User user = userRepository.findByEmail(request.getEmail());
            if (user == null) {
                logger.warn("Login failed: User not found with email: {}", request.getEmail());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid email or password"));
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Login failed: Invalid password for email: {}", request.getEmail());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid email or password"));
            }

            String token = jwtUtil.generateToken(user.getEmail());
            AuthResponse authResponse = new AuthResponse(token, user);
            
            logger.info("Login successful for user: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
            
        } catch (Exception e) {
            logger.error("Error during login for email: " + request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login"));
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Password reset requested for email: {}", email);
        
        if (email == null || email.isBlank()) {
            logger.warn("Password reset failed: Email is required");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
        }
        
        try {
            String result = loginService.forgotPassword(request);
            logger.info("Password reset email sent successfully to: {}", email);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("Error sending password reset email to: " + email, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process password reset request"));
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token, 
            @RequestBody String newPassword) {
        
        logger.info("Password reset attempt with token");
        
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("Password reset failed: Password must be at least 8 characters long");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password must be at least 8 characters long"));
        }
        
        try {
            String result = loginService.resetPassword(token, newPassword);
            logger.info("Password reset successful for token");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("Error resetting password with token", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired token"));
        }
    }
}
