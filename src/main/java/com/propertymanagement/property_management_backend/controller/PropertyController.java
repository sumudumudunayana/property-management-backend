package com.propertymanagement.property_management_backend.controller;

import com.propertymanagement.property_management_backend.dto.CreatePropertyRequest;
import com.propertymanagement.property_management_backend.dto.UpdatePropertyRequest;
import com.propertymanagement.property_management_backend.entity.Property;
import com.propertymanagement.property_management_backend.entity.User;
import com.propertymanagement.property_management_backend.security.AuthenticationService;
import com.propertymanagement.property_management_backend.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final AuthenticationService authenticationService;

    public PropertyController(
            PropertyService propertyService,
            AuthenticationService authenticationService
    ) {
        this.propertyService = propertyService;
        this.authenticationService = authenticationService;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<Property> createProperty(
            @Valid @RequestBody CreatePropertyRequest request,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                propertyService.createProperty(
                        request,
                        seller.getId()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<Property>> getAvailableProperties() {

        return ResponseEntity.ok(
                propertyService.getAllAvailableProperties()
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/my")
    public ResponseEntity<List<Property>> getMyProperties(
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                propertyService.getSellerProperties(
                        seller.getId()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                propertyService.getPropertyById(id)
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyRequest request,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                propertyService.updateProperty(
                        id,
                        seller.getId(),
                        request
                )
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProperty(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        propertyService.deleteProperty(
                id,
                seller.getId()
        );

        return ResponseEntity.ok(
                "Property deleted successfully"
        );
    }
}