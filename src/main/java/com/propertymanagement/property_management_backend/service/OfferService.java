package com.propertymanagement.property_management_backend.service;

import com.propertymanagement.property_management_backend.dto.CounterOfferRequest;
import com.propertymanagement.property_management_backend.dto.CreateOfferRequest;
import com.propertymanagement.property_management_backend.entity.Offer;
import com.propertymanagement.property_management_backend.entity.OfferStatus;
import com.propertymanagement.property_management_backend.entity.Property;
import com.propertymanagement.property_management_backend.entity.PropertyStatus;
import com.propertymanagement.property_management_backend.entity.User;
import com.propertymanagement.property_management_backend.entity.UserRole;
import com.propertymanagement.property_management_backend.repository.OfferRepository;
import com.propertymanagement.property_management_backend.repository.PropertyRepository;
import com.propertymanagement.property_management_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public OfferService(
            OfferRepository offerRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository
    ) {
        this.offerRepository = offerRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    // BUYER - Create offer
    public Offer createOffer(
            Long propertyId,
            Long buyerId,
            CreateOfferRequest request
    ) {

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() ->
                        new RuntimeException("Buyer not found")
                );

        if (buyer.getRole() != UserRole.BUYER) {
            throw new RuntimeException(
                    "Only buyers can make offers"
            );
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found")
                );

        // Only AVAILABLE properties can receive offers
        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new RuntimeException(
                    "This property is not available for offers"
            );
        }

        // Buyer cannot make an offer on their own property
        if (property.getSeller().getId().equals(buyerId)) {
            throw new RuntimeException(
                    "You cannot make an offer on your own property"
            );
        }

        Offer offer = new Offer();

        offer.setAmount(request.getAmount());
        offer.setMessage(request.getMessage());
        offer.setStatus(OfferStatus.PENDING);
        offer.setProperty(property);
        offer.setBuyer(buyer);

        return offerRepository.save(offer);
    }


    // BUYER - View own offers
    public List<Offer> getBuyerOffers(Long buyerId) {

        return offerRepository.findByBuyerId(buyerId);
    }


    // SELLER - View offers for property
    public List<Offer> getPropertyOffers(
            Long propertyId,
            Long sellerId
    ) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found")
                );

        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to view these offers"
            );
        }

        return offerRepository.findByPropertyId(propertyId);
    }


    // Get individual offer
    public Offer getOfferById(Long offerId) {

        return offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Offer not found")
                );
    }


    // SELLER - Accept offer
    public Offer acceptOffer(
            Long offerId,
            Long sellerId
    ) {

        Offer offer = getOfferById(offerId);

        Property property = offer.getProperty();

        // Check property ownership
        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to manage this offer"
            );
        }

        // Only PENDING or COUNTERED offers can be accepted
        if (offer.getStatus() != OfferStatus.PENDING &&
                offer.getStatus() != OfferStatus.COUNTERED) {

            throw new RuntimeException(
                    "This offer cannot be accepted"
            );
        }

        // Accept selected offer
        offer.setStatus(OfferStatus.ACCEPTED);

        // Property is now SOLD
        property.setStatus(PropertyStatus.SOLD);

        propertyRepository.save(property);

        // Reject all other active offers
        List<Offer> otherOffers =
                offerRepository.findByPropertyId(property.getId());

        for (Offer otherOffer : otherOffers) {

            if (!otherOffer.getId().equals(offer.getId()) &&
                    (otherOffer.getStatus() == OfferStatus.PENDING ||
                            otherOffer.getStatus() == OfferStatus.COUNTERED)) {

                otherOffer.setStatus(OfferStatus.REJECTED);
            }
        }

        offerRepository.saveAll(otherOffers);

        return offerRepository.save(offer);
    }


    // SELLER - Reject offer
    public Offer rejectOffer(
            Long offerId,
            Long sellerId
    ) {

        Offer offer = getOfferById(offerId);

        Property property = offer.getProperty();

        // Check property ownership
        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to manage this offer"
            );
        }

        // Only PENDING or COUNTERED offers can be rejected
        if (offer.getStatus() != OfferStatus.PENDING &&
                offer.getStatus() != OfferStatus.COUNTERED) {

            throw new RuntimeException(
                    "This offer cannot be rejected"
            );
        }

        offer.setStatus(OfferStatus.REJECTED);

        return offerRepository.save(offer);
    }


    // SELLER - Counteroffer
    public Offer counterOffer(
            Long offerId,
            Long sellerId,
            CounterOfferRequest request
    ) {

        Offer offer = getOfferById(offerId);

        Property property = offer.getProperty();

        // Check property ownership
        if (!property.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "You are not allowed to manage this offer"
            );
        }

        // Only PENDING offers can be countered
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending offers can be countered"
            );
        }

        // Update offer with counteroffer details
        offer.setAmount(request.getAmount());
        offer.setMessage(request.getMessage());
        offer.setStatus(OfferStatus.COUNTERED);

        return offerRepository.save(offer);
    }


    // BUYER - Accept counteroffer
    public Offer acceptCounterOffer(
            Long offerId,
            Long buyerId
    ) {

        Offer offer = getOfferById(offerId);

        // Check buyer ownership
        if (!offer.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException(
                    "You are not allowed to manage this offer"
            );
        }

        // Only COUNTERED offers can be accepted
        if (offer.getStatus() != OfferStatus.COUNTERED) {
            throw new RuntimeException(
                    "Only countered offers can be accepted"
            );
        }

        // Accept offer
        offer.setStatus(OfferStatus.ACCEPTED);

        Property property = offer.getProperty();

        // Property is now SOLD
        property.setStatus(PropertyStatus.SOLD);

        propertyRepository.save(property);

        // Reject all other active offers
        List<Offer> otherOffers =
                offerRepository.findByPropertyId(property.getId());

        for (Offer otherOffer : otherOffers) {

            if (!otherOffer.getId().equals(offer.getId()) &&
                    (otherOffer.getStatus() == OfferStatus.PENDING ||
                            otherOffer.getStatus() == OfferStatus.COUNTERED)) {

                otherOffer.setStatus(OfferStatus.REJECTED);
            }
        }

        offerRepository.saveAll(otherOffers);

        return offerRepository.save(offer);
    }


    // BUYER - Reject counteroffer
    public Offer rejectCounterOffer(
            Long offerId,
            Long buyerId
    ) {

        Offer offer = getOfferById(offerId);

        // Check buyer ownership
        if (!offer.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException(
                    "You are not allowed to manage this offer"
            );
        }

        // Only COUNTERED offers can be rejected
        if (offer.getStatus() != OfferStatus.COUNTERED) {
            throw new RuntimeException(
                    "Only countered offers can be rejected"
            );
        }

        offer.setStatus(OfferStatus.REJECTED);

        Property property = offer.getProperty();

        /*
         * The property normally remains AVAILABLE because
         * we only mark it SOLD when a counteroffer is accepted.
         */
        if (property.getStatus() == PropertyStatus.UNDER_OFFER) {
            property.setStatus(PropertyStatus.AVAILABLE);
            propertyRepository.save(property);
        }

        return offerRepository.save(offer);
    }
}