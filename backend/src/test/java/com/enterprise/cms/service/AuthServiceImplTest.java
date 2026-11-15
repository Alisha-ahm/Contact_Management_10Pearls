package com.enterprise.cms.service;

import com.enterprise.cms.dto.*;
import com.enterprise.cms.entity.User;
import com.enterprise.cms.exception.DuplicateResourceException;
import com.enterprise.cms.exception.InvalidCredentialsException;
import com.enterprise.cms.repository.UserRepository;
import com.enterprise.cms.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId("usr_001");
        existingUser.setFullName("Alexander Wright");
        existingUser.setEmail("alex@apex.com");
        existingUser.setPasswordHash("$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Should register user with phone number and issue JWT")
    void registerWithPhone_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Priya Patel");
        req.setIdentifier("+14155550199");
        req.setPassword("Password123!");

        when(userRepository.existsByPhone("+14155550199")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token-123");

        AuthResponse response = authService.register(req);

        assertEquals("jwt-token-123", response.getToken());
        assertEquals("+14155550199", response.getUser().getPhone());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void registerDuplicateEmail_ThrowsConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Alex");
        req.setIdentifier("alex@apex.com");
        req.setPassword("Password123!");

        when(userRepository.existsByEmailIgnoreCase("alex@apex.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Should change password when current password matches")
    void changePassword_Success() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("OldPass123!");
        req.setNewPassword("NewPass123!");

        when(userRepository.findById("usr_001")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("OldPass123!", "$2a$10$hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newEncodedHash");

        authService.changePassword("usr_001", req);

        assertEquals("newEncodedHash", existingUser.getPasswordHash());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Should reject password change on wrong current password")
    void changePassword_WrongCurrent_Throws() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("WrongPass!");
        req.setNewPassword("NewPass123!");

        when(userRepository.findById("usr_001")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPass!", "$2a$10$hashedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.changePassword("usr_001", req));
    }
}
