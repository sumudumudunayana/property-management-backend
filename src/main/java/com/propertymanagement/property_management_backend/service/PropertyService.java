package com.propertymanagement.property_management_backend.service;

import com.propertymanagement.property_management_backend.dto.CreatePropertyRequest;
import com.propertymanagement.property_management_backend.dto.UpdatePropertyRequest;
import com.propertymanagement.property_management_backend.entity.Property;
import com.propertymanagement.property_management_backend.entity.PropertyStatus;
import com.propertymanagement.property_management_backend.entity.User;
import com.propertymanagement.property_management_backend.repository.PropertyRepository;
import com.propertymanagement.property_management_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            UserRepository userRepository
    ) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public Property createProperty(
            CreatePropertyRequest request,
            Long sellerId
    ) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        if (!seller.getRole().name().equals("SELLER")) {
            throw new RuntimeException(
                    "Only sellers can create properties"
            );
        }

        Property property = new Property();

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setLocation(request.getLocation());
        property.setPropertyType(request.getPropertyType());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());

        // Set property image URL
        property.setImageUrl(request.getImageUrl());

        property.setStatus(PropertyStatus.AVAILABLE);
        property.setSeller(seller);

        return propertyRepository.save(property);
    }


    public List<Property> getAllAvailableProperties() {

        return propertyRepository.findByStatus(
                PropertyStatus.AVAILABLE
        );
    }


    public List<Property> getSellerProperties(Long sellerId) {

        return propertyRepository.findBySellerId(sellerId);
    }


    public Property getPropertyById(Long id) {

        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Property not found")
                );
    }


    public Property updateProperty(
            Long propertyId,
            Long sellerId,
            UpdatePropertyRequest request
    ) {

        Property property = getPropertyById(propertyId);

        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to update this property"
            );
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new RuntimeException(
                    "Only available properties can be updated"
            );
        }

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setLocation(request.getLocation());
        property.setPropertyType(request.getPropertyType());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());

        // Update property image URL
        property.setImageUrl(request.getImageUrl());

        return propertyRepository.save(property);
    }


    public void deleteProperty(
            Long propertyId,
            Long sellerId
    ) {

        Property property = getPropertyById(propertyId);

        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to delete this property"
            );
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new RuntimeException(
                    "Only available properties can be deleted"
            );
        }

        propertyRepository.delete(property);
    }
}