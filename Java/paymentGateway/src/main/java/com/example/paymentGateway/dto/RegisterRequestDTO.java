package com.example.paymentGateway.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 30, message = "username must be 3-30 characters")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$",
                message = "username must start with a letter and contain only letters, digits or underscores")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be 8-72 characters")
        @Pattern(regexp = "^\\S+$", message = "password must not contain spaces")
        @Pattern(regexp = ".*[a-z].*", message = "password must contain a lowercase letter")
        @Pattern(regexp = ".*[A-Z].*", message = "password must contain an uppercase letter")
        @Pattern(regexp = ".*\\d.*", message = "password must contain a digit")
        @Pattern(regexp = ".*[^A-Za-z0-9\\s].*", message = "password must contain a special character")
        String password
) {

    // Cross-field rule: a method named isXxx() with @AssertTrue is validated like a field
    @AssertTrue(message = "password must not contain the username")
    public boolean isPasswordNotContainingUsername() {
        if (username == null || password == null) {
            return true; // the @NotBlank rules already report missing values
        }
        return !password.toLowerCase().contains(username.toLowerCase());
    }
}