package com.carehive.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carehive.dtos.ApiResponse;
import com.carehive.entities.Services;
import com.carehive.services.ServicesService;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServicesService servicesService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Services>> addService(@RequestBody Services service) {
        logger.info("Creating new service: {}", service.getServiceTitle());
        try {
            Services createdService = servicesService.createService(service);
            logger.info("Service created successfully with ID: {}", createdService.getServiceId());
            return ResponseEntity.ok(ApiResponse.success("Service created successfully", createdService));
        } catch (Exception e) {
            logger.error("Failed to create service: " + service.getServiceTitle(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create service: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Services>> updateService(
            @PathVariable int id, 
            @RequestBody Services service) {
        logger.info("Updating service ID: {}", id);
        try {
            Services updatedService = servicesService.updateService(id, service);
            if (updatedService == null) {
                logger.warn("Service not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            logger.info("Service ID: {} updated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Service updated successfully", updatedService));
        } catch (Exception e) {
            logger.error("Failed to update service ID: " + id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update service: " + e.getMessage()));
        }
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<Services>>> services() {
        logger.debug("Fetching all services");
        try {
            List<Services> services = servicesService.AllServices();
            logger.debug("Found {} services", services.size());
            return ResponseEntity.ok(ApiResponse.success(services));
        } catch (Exception e) {
            logger.error("Failed to retrieve services", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve services: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Services>> getService(@PathVariable int id) {
        logger.debug("Fetching service with ID: {}", id);
        try {
            Services service = servicesService.getService(id);
            if (service == null) {
                logger.warn("Service not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(service));
        } catch (Exception e) {
            logger.error("Failed to retrieve service with ID: " + id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve service: " + e.getMessage()));
        }
    }
}