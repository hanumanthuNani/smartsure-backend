package com.smartsure.auth.controller;

import com.smartsure.auth.dto.ApiResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.LoginOtpRequest;
import com.smartsure.auth.dto.VerifyLoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.dto.UserResponse;
import com.smartsure.auth.dto.TokenRefreshRequest;
import com.smartsure.auth.dto.TokenRefreshResponse;
import com.smartsure.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and session management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "🌐 [PUBLIC] Register a new user", description = "Creates a new customer account in the system.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register user: {}", request.getEmail());
        ApiResponse<UserResponse> response = authService.register(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "🌐 [PUBLIC] Authenticate user", description = "Returns an Access Token (15m) and a Refresh Token (7d).")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user: {}", request.getEmail());
        ApiResponse<UserResponse> response = authService.login(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-otp/initiate")
    @Operation(summary = "🔒 [SECURE] Initiate MFA Login", description = "Verifies password and emails a 6-digit OTP to the user.")
    public ResponseEntity<ApiResponse<String>> initiateOtpLogin(@Valid @RequestBody LoginOtpRequest request) {
        log.info("REST request to initiate OTP login for user: {}", request.getEmail());
        ApiResponse<String> response = authService.initiateOtpLogin(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-otp/verify")
    @Operation(summary = "🔒 [SECURE] Verify MFA Login", description = "Validates the 6-digit OTP and returns both Access and Refresh Tokens.")
    public ResponseEntity<ApiResponse<UserResponse>> verifyOtpLogin(@Valid @RequestBody VerifyLoginRequest request) {
        log.info("REST request to verify OTP login for user: {}", request.getEmail());
        ApiResponse<UserResponse> response = authService.verifyOtpLogin(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "🌐 [PUBLIC] Refresh Access Token", description = "Uses a valid Refresh Token to issue a new short-lived Access Token.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("REST request to refresh token");
        ApiResponse<TokenRefreshResponse> response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "👤 [USER] Logout user", description = "Invalidates and deletes the Refresh Token from the database.")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam String refreshToken) {
        log.info("REST request to logout user");
        ApiResponse<String> response = authService.logout(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "🌐 [PUBLIC] Request Password Reset OTP", description = "Sends a 6-digit OTP to the registered email address.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody com.smartsure.auth.dto.ForgotPasswordRequest request) {
        log.info("REST request to trigger forgot password for: {}", request.getEmail());
        ApiResponse<String> response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "🌐 [PUBLIC] Reset Password with OTP", description = "Verifies the OTP and updates the user password.")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody com.smartsure.auth.dto.ResetPasswordRequest request) {
        log.info("REST request to reset password for: {}", request.getEmail());
        ApiResponse<String> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
