package com.propertymanagement.property_management_backend.repository;

import com.propertymanagement.property_management_backend.entity.Property;
import com.propertymanagement.property_management_backend.entity.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findBySellerId(Long sellerId);

    List<Property> findByStatus(PropertyStatus status);
}