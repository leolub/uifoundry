package dev.uifoundry.auth;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.uifoundry.auth.dto.RegisterRequest;
import dev.uifoundry.auth.dto.RegisterResponse;
import dev.uifoundry.common.exception.DuplicateEmailException;
import dev.uifoundry.user.User;
import dev.uifoundry.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
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
}
