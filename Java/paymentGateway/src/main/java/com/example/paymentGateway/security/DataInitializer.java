package com.example.paymentGateway.security;

import com.example.paymentGateway.model.AppUser;
import com.example.paymentGateway.repository.AppUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DEV ONLY: creates demo users on startup (password for all: password123).
 * They own the four accounts seeded by data.sql. Remove for production.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String name : List.of("adarsh", "aarcha", "usha", "ajith")) {
            if (!userRepository.existsByUsername(name)) {
                userRepository.save(new AppUser(name, passwordEncoder.encode("password123"), "USER"));
            }
        }
    }
}