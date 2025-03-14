package com.gaviria.farmatodo_user_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaviria.farmatodo_user_service.dto.LoginRequest;
import com.gaviria.farmatodo_user_service.dto.LoginResponse;
import com.gaviria.farmatodo_user_service.dto.RegisterRequest;
import com.gaviria.farmatodo_user_service.dto.RegisterResponse;
import com.gaviria.farmatodo_user_service.models.User;
import com.gaviria.farmatodo_user_service.repositories.UserRepository;
import com.gaviria.farmatodo_user_service.security.JwtUtil;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones de autenticación")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        String token = jwtUtil.generateToken(loginRequest.getEmail());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un usuario")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        userRepository.save(user);
        return ResponseEntity.ok(new RegisterResponse("User registered successfully"));
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validar token")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        String cleanToken = token.replace("Bearer ", "");
        Claims claims = jwtUtil.extractAllClaims(cleanToken);
        return ResponseEntity.ok(claims.getSubject());
    }
}
