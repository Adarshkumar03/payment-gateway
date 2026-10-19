package com.example.paymentGateway.service;

import com.example.paymentGateway.dto.RegisterRequestDTO;
import com.example.paymentGateway.exception.DuplicateRequestException;
import com.example.paymentGateway.model.AppUser;
import com.example.paymentGateway.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String register(RegisterRequestDTO request) {
        String username = request.username().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateRequestException("Username already taken");
        }
        userRepository.save(new AppUser(username, passwordEncoder.encode(request.password()), "USER"));
        return username;
    }
}