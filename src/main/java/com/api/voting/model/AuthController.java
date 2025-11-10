package com.api.voting.model;

import com.api.voting.model.user.User;
import com.api.voting.model.user.UserRepository;
import com.api.voting.security.auth.LoginRequest;
import com.api.voting.security.auth.TokenResponse;
import com.api.voting.security.jwt.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        authManager.authenticate(auth);

        User user = userRepository.findByEmailAndDeletedFalse(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String token = tokenService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name())
        );
        long expires = 60L * 15L;

        return ResponseEntity.ok(new TokenResponse(token, "Bearer", expires));
    }

    // opcional: cadastro para o lab
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}

