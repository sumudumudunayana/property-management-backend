package com.propertymanagement.property_management_backend.security;

import com.propertymanagement.property_management_backend.entity.User;
import com.propertymanagement.property_management_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        System.out.println("Authenticated email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}