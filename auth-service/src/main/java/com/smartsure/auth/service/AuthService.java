package com.smartsure.auth.service;

import com.smartsure.auth.dto.ApiResponse;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.dto.UserResponse;

public interface AuthService {
    ApiResponse<UserResponse> register(RegisterRequest request);
    ApiResponse<UserResponse> login(com.smartsure.auth.dto.LoginRequest request);
    ApiResponse<String> initiateOtpLogin(com.smartsure.auth.dto.LoginOtpRequest request);
    ApiResponse<UserResponse> verifyOtpLogin(com.smartsure.auth.dto.VerifyLoginRequest request);
    ApiResponse<String> forgotPassword(com.smartsure.auth.dto.ForgotPasswordRequest request);
    ApiResponse<String> resetPassword(com.smartsure.auth.dto.ResetPasswordRequest request);
    ApiResponse<com.smartsure.auth.dto.TokenRefreshResponse> refreshToken(com.smartsure.auth.dto.TokenRefreshRequest request);
    ApiResponse<String> logout(String refreshToken);
}
