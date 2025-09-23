package com.debajyoti.controller;

import com.debajyoti.dto.AuthRequest;
import com.debajyoti.dto.AuthResponse;
import com.debajyoti.dto.RegisterRequest;
import com.debajyoti.entity.User;
import com.debajyoti.security.JwtUtil;
import com.debajyoti.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), 
                    authRequest.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid username or password"));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final User user = userService.findByUsername(authRequest.getUsername());
        
        // Add user role to JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        
        final String jwt = jwtUtil.generateToken(userDetails, claims);

        return ResponseEntity.ok(new AuthResponse(jwt, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Username already exists"));
        }

        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email already exists"));
        }

        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.CUSTOMER);

            User savedUser = userService.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "userId", savedUser.getId(),
                "username", savedUser.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username
                ));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("valid", false));
    }
}