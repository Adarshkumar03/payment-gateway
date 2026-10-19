package com.example.paymentGateway.model;

import jakarta.persistence.*;

// Table is called app_user because "user" is a reserved word in many databases
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // Never the plain password: only the BCrypt hash is stored
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}