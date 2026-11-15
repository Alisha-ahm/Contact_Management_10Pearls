package com.enterprise.cms.controller;

import com.enterprise.cms.dto.AuthResponse;
import com.enterprise.cms.dto.LoginRequest;
import com.enterprise.cms.dto.RegisterRequest;
import com.enterprise.cms.dto.UserResponse;
import com.enterprise.cms.exception.GlobalExceptionHandler;
import com.enterprise.cms.exception.InvalidCredentialsException;
import com.enterprise.cms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register returns 201 Created")
    void register_ReturnsCreated() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Sarah Chen");
        req.setIdentifier("sarah@stratacloud.io");
        req.setPassword("Password123!");

        UserResponse userDto = new UserResponse();
        userDto.setId("usr_1");
        userDto.setFullName("Sarah Chen");
        userDto.setEmail("sarah@stratacloud.io");

        when(authService.register(any())).thenReturn(new AuthResponse("jwt-abc", "Registered", userDto));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-abc"))
                .andExpect(jsonPath("$.user.email").value("sarah@stratacloud.io"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 401 on invalid credentials")
    void login_InvalidCredentials_Returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setIdentifier("bad@user.com");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
