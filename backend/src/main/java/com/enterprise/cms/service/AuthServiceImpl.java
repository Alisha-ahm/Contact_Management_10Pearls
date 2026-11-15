package com.enterprise.cms.service;

import com.enterprise.cms.dto.*;
import com.enterprise.cms.entity.User;
import com.enterprise.cms.exception.DuplicateResourceException;
import com.enterprise.cms.exception.InvalidCredentialsException;
import com.enterprise.cms.exception.ResourceNotFoundException;
import com.enterprise.cms.repository.UserRepository;
import com.enterprise.cms.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String identifier = request.getIdentifier().trim();
        boolean isEmail = identifier.contains("@");
        log.info("Processing user registration using {} identifier [{}]", isEmail ? "email" : "phone", identifier);

        if (isEmail && userRepository.existsByEmailIgnoreCase(identifier)) {
            log.warn("Duplicate email registration attempt: [{}]", identifier);
            throw new DuplicateResourceException("An account with this email address already exists.");
        }
        if (!isEmail && userRepository.existsByPhone(identifier)) {
            log.warn("Duplicate phone registration attempt: [{}]", identifier);
            throw new DuplicateResourceException("An account with this phone number already exists.");
        }

        User user = new User();
        user.setId("usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        user.setFullName(request.getFullName().trim());
        if (isEmail) {
            user.setEmail(identifier.toLowerCase());
        } else {
            user.setPhone(identifier);
        }
        user.setCompany(request.getCompany() != null && !request.getCompany().isBlank()
                ? request.getCompany().trim()
                : "Enterprise Organization");
        user.setRole("Standard User");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        String token = tokenProvider.generateToken(saved.getId());
        log.info("Successfully registered new user ID [{}]", saved.getId());

        return new AuthResponse(token, "Account registered successfully.", UserResponse.fromEntity(saved));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Authenticating user with identifier [{}]", identifier);

        Optional<User> userOpt = identifier.contains("@")
                ? userRepository.findByEmailIgnoreCase(identifier)
                : userRepository.findByPhone(identifier);

        User user = userOpt.orElseThrow(() -> {
            log.warn("Login failed: no account found for [{}]", identifier);
            return new InvalidCredentialsException("Invalid email/phone or password.");
        });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: incorrect password for user [{}]", user.getId());
            throw new InvalidCredentialsException("Invalid email/phone or password.");
        }

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getId());
        log.info("User [{}] authenticated successfully.", user.getId());

        return new AuthResponse(token, "Welcome back, " + user.getFullName() + "!", UserResponse.fromEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for ID: " + userId));
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for ID: " + userId));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone().trim() : null);
        user.setCompany(request.getCompany() != null && !request.getCompany().isBlank() ? request.getCompany().trim() : null);

        User updated = userRepository.save(user);
        log.info("Updated profile for user ID [{}]", userId);
        return UserResponse.fromEntity(updated);
    }

    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("Password change requested for user ID [{}]", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Password change rejected: current password mismatch for user [{}]", userId);
            throw new InvalidCredentialsException("Current password does not match.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user ID [{}]", userId);
    }
}
