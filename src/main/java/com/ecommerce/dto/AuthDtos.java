package com.ecommerce.dto;

import com.ecommerce.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                    message = "password must be at least 8 characters and include uppercase, lowercase, number, and special character"
            ) String password,
            @NotNull Role role,
            @NotBlank String verificationCode
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotNull Role role
    ) {
    }

    public record AuthResponse(String token, Long userId, String name, String email, Role role) {
    }

    public record EmailVerificationRequest(@Email @NotBlank String email) {
    }

    public record EmailVerificationResponse(String email, String message, String demoCode) {
    }
}
