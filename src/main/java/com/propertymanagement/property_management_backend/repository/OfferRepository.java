package com.propertymanagement.property_management_backend.repository;

import com.propertymanagement.property_management_backend.entity.Offer;
import com.propertymanagement.property_management_backend.entity.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByBuyerId(Long buyerId);

    List<Offer> findByPropertyId(Long propertyId);

    List<Offer> findByPropertyIdAndStatus(
            Long propertyId,
            OfferStatus status
    );
}