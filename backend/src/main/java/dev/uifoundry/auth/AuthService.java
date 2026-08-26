package dev.uifoundry.auth;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.uifoundry.auth.dto.RegisterRequest;
import dev.uifoundry.auth.dto.RegisterResponse;
import dev.uifoundry.auth.dto.LoginRequest;
import dev.uifoundry.auth.dto.LoginResponse;
import dev.uifoundry.auth.dto.UserResponse;
import dev.uifoundry.common.exception.DuplicateEmailException;
import dev.uifoundry.common.exception.InvalidCredentialsException;
import dev.uifoundry.common.security.JwtService;
import dev.uifoundry.user.User;
import dev.uifoundry.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        User user = new User(normalizedEmail, passwordEncoder.encode(request.password()));
        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getCreatedAt());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException(exception);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.createAccessToken(user);
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
