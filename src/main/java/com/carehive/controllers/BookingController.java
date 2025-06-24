package com.carehive.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carehive.dtos.ApiResponse;
import com.carehive.entities.BookingStatus;
import com.carehive.entities.Bookings;
import com.carehive.entities.UserType;
import com.carehive.services.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping("/")
    public ResponseEntity<ApiResponse<Bookings>> create(@RequestBody Bookings booking) {
        logger.info("Creating new booking for elder: {}, caretaker: {}, service: {}", 
            booking.getElderId(), booking.getCaretakerId(), booking.getServiceId());
            
        try {
            Bookings createdBooking = bookingService.create(booking);
            logger.info("Booking created successfully - ID: {}, Status: {}", 
                createdBooking.getBookingId(), createdBooking.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Booking created successfully", createdBooking));
        } catch (Exception e) {
            logger.error("Failed to create booking for elder: {}. Error: {}", 
                booking.getElderId(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create booking: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list/{id}/{userType}")
    public ResponseEntity<ApiResponse<List<Bookings>>> allBookings(
            @PathVariable int id, 
            @PathVariable UserType userType) {
        logger.info("Fetching all bookings for {} ID: {}", userType, id);
        
        try {
            List<Bookings> bookings = bookingService.allBookings(id, userType);
            logger.debug("Found {} bookings for {} ID: {}", bookings.size(), userType, id);
            return ResponseEntity.ok(ApiResponse.success(bookings));
        } catch (Exception e) {
            logger.error("Failed to fetch bookings for {} ID: {}. Error: {}", 
                userType, id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve bookings: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/{status}")
    @PreAuthorize("hasAuthority('CAREGIVER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Bookings>> updateBookingStatus(
            @PathVariable int id, 
            @PathVariable BookingStatus status) {
        logger.info("Updating booking ID: {} to status: {}", id, status);
        
        try {
            Bookings updatedBooking = bookingService.updateBookingStatus(id, status);
            if (updatedBooking == null) {
                logger.warn("Booking not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            logger.info("Booking ID: {} status updated to: {}", id, status);
            return ResponseEntity.ok(ApiResponse.success("Booking status updated", updatedBooking));
        } catch (Exception e) {
            logger.error("Failed to update booking ID: {}. Error: {}", 
                id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update booking status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Bookings>> getBookingDetails(@PathVariable int bookingId) {
        logger.debug("Fetching details for booking ID: {}", bookingId);
        
        try {
            Bookings booking = bookingService.getBookingDetails(bookingId);
            if (booking == null) {
                logger.warn("Booking not found with ID: {}", bookingId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(booking));
        } catch (Exception e) {
            logger.error("Failed to fetch booking details for ID: {}. Error: {}", 
                bookingId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve booking details: " + e.getMessage()));
        }
    }
    
    @GetMapping("/allBookings")
    public ResponseEntity<ApiResponse<List<Bookings>>> allBookings() {
        logger.info("Fetching all bookings");
        
        try {
            List<Bookings> bookings = bookingService.allBookings();
            logger.debug("Retrieved {} total bookings", bookings.size());
            return ResponseEntity.ok(ApiResponse.success(bookings));
        } catch (Exception e) {
            logger.error("Failed to fetch all bookings. Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve bookings: " + e.getMessage()));
        }
    }
}