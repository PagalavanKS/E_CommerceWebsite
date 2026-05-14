package com.ecommerce.service;

import com.ecommerce.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {
    private static final long CODE_TTL_SECONDS = 600;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, VerificationCode> codes = new ConcurrentHashMap<>();

    public String sendCode(String email) {
        String normalizedEmail = normalize(email);
        String code = String.format("%06d", random.nextInt(1_000_000));
        codes.put(normalizedEmail, new VerificationCode(code, Instant.now().plusSeconds(CODE_TTL_SECONDS)));
        System.out.println("Demo email verification code for " + normalizedEmail + ": " + code);
        return code;
    }

    public void verify(String email, String code) {
        String normalizedEmail = normalize(email);
        VerificationCode saved = codes.get(normalizedEmail);
        if (saved == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please send a verification code first");
        }
        if (saved.expiresAt().isBefore(Instant.now())) {
            codes.remove(normalizedEmail);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification code expired. Send a new code");
        }
        if (!saved.code().equals(code == null ? "" : code.trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid email verification code");
        }
        codes.remove(normalizedEmail);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record VerificationCode(String code, Instant expiresAt) {
    }
}
