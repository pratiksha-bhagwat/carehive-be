package com.carehive.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carehive.dtos.ApiResponse;
import com.carehive.entities.User;
import com.carehive.entities.UserType;
import com.carehive.entities.UserUpdateRequest;
import com.carehive.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserUpdateRequest>> getDetails(@PathVariable int id) {
        try {
            UserUpdateRequest userDetails = userService.getDetails(id);
            if (userDetails == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(userDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get user details: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUserDetails(
            @RequestBody UserUpdateRequest request,
            @PathVariable int id) {
        try {
            User user = new User();
            user.setUserType(request.getUserType());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setContact(request.getContact());
            user.setEmergencyContact(request.getEmergencyContact());
            user.setGender(request.getGender());
            user.setDate(request.getDate());
            
            List<Integer> serviceIds = request.getServiceIds();
            if (serviceIds == null) {
                serviceIds = new ArrayList<>();
            }

            User updatedUser = userService.updateUser(user, id, serviceIds);
            return ResponseEntity.ok(ApiResponse.success("User details updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user details: " + e.getMessage()));
        }
    }
    
    @GetMapping("/allCaretakers")
    public ResponseEntity<ApiResponse<List<User>>> getAllCaretakers() {
        try {
            List<User> caretakers = userService.getAllCaretakers();
            return ResponseEntity.ok(ApiResponse.success(caretakers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve caretakers: " + e.getMessage()));
        }
    }
    
    @GetMapping("/count/{userType}")
    public ResponseEntity<ApiResponse<Integer>> countUsersByUserType(@PathVariable UserType userType) {
        try {
            int count = userService.countByUserType(userType);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to count users by type: " + e.getMessage()));
        }
    }
}
