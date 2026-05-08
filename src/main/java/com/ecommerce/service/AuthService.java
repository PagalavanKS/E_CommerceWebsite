package com.ecommerce.service;

import com.ecommerce.dto.AuthDtos.AuthResponse;
import com.ecommerce.dto.AuthDtos.LoginRequest;
import com.ecommerce.dto.AuthDtos.RegisterRequest;
import com.ecommerce.exception.ApiException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final UserDetailsService detailsService;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, UserDetailsService detailsService, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.detailsService = detailsService;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setRole(request.role());
        return tokenResponse(users.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getRole() != request.role()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "This account is not registered as " + request.role().name().toLowerCase());
        }
        return tokenResponse(user);
    }

    private AuthResponse tokenResponse(User user) {
        UserDetails details = detailsService.loadUserByUsername(user.getEmail());
        return new AuthResponse(jwt.generateToken(details), user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
