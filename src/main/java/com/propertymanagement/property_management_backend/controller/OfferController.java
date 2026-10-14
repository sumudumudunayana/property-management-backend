package com.propertymanagement.property_management_backend.controller;

import com.propertymanagement.property_management_backend.dto.CounterOfferRequest;
import com.propertymanagement.property_management_backend.dto.CreateOfferRequest;
import com.propertymanagement.property_management_backend.entity.Offer;
import com.propertymanagement.property_management_backend.entity.User;
import com.propertymanagement.property_management_backend.security.AuthenticationService;
import com.propertymanagement.property_management_backend.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;
    private final AuthenticationService authenticationService;

    public OfferController(
            OfferService offerService,
            AuthenticationService authenticationService
    ) {
        this.offerService = offerService;
        this.authenticationService = authenticationService;
    }

    // BUYER - Create offer
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/property/{propertyId}")
    public ResponseEntity<Offer> createOffer(
            @PathVariable Long propertyId,
            @Valid @RequestBody CreateOfferRequest request,
            Authentication authentication
    ) {

        User buyer =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.createOffer(
                        propertyId,
                        buyer.getId(),
                        request
                )
        );
    }

    // BUYER - Get own offers
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/my")
    public ResponseEntity<List<Offer>> getBuyerOffers(
            Authentication authentication
    ) {

        User buyer =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.getBuyerOffers(
                        buyer.getId()
                )
        );
    }

    // SELLER - Get offers for property
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<Offer>> getPropertyOffers(
            @PathVariable Long propertyId,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.getPropertyOffers(
                        propertyId,
                        seller.getId()
                )
        );
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> getOffer(
            @PathVariable Long offerId
    ) {

        return ResponseEntity.ok(
                offerService.getOfferById(offerId)
        );
    }

    // SELLER - Accept
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{offerId}/accept")
    public ResponseEntity<Offer> acceptOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.acceptOffer(
                        offerId,
                        seller.getId()
                )
        );
    }

    // SELLER - Reject
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{offerId}/reject")
    public ResponseEntity<Offer> rejectOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.rejectOffer(
                        offerId,
                        seller.getId()
                )
        );
    }

    // SELLER - Counter
    @PutMapping("/{offerId}/counter")
    public ResponseEntity<Offer> counterOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody CounterOfferRequest request,
            Authentication authentication
    ) {

        User seller =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.counterOffer(
                        offerId,
                        seller.getId(),
                        request
                )
        );
    }

    // BUYER - Accept counteroffer
    @PreAuthorize("hasRole('BUYER')")
    @PutMapping("/{offerId}/accept-counter")
    public ResponseEntity<Offer> acceptCounterOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {

        User buyer =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.acceptCounterOffer(
                        offerId,
                        buyer.getId()
                )
        );
    }


    // BUYER - Reject counteroffer
    @PreAuthorize("hasRole('BUYER')")
    @PutMapping("/{offerId}/reject-counter")
    public ResponseEntity<Offer> rejectCounterOffer(
            @PathVariable Long offerId,
            Authentication authentication
    ) {

        User buyer =
                authenticationService.getCurrentUser(authentication);

        return ResponseEntity.ok(
                offerService.rejectCounterOffer(
                        offerId,
                        buyer.getId()
                )
        );
    }
}