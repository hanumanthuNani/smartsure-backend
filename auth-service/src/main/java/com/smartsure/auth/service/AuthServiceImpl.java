package com.smartsure.auth.service;

import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartsure.auth.dto.ApiResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.LoginOtpRequest;
import com.smartsure.auth.dto.VerifyLoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.dto.UserResponse;
import com.smartsure.auth.dto.TokenRefreshRequest;
import com.smartsure.auth.dto.TokenRefreshResponse;
import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.RoleName;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.entity.RefreshToken;
import com.smartsure.auth.exception.AuthServiceException;
import com.smartsure.auth.mapper.UserMapper;
import com.smartsure.auth.repository.RoleRepository;
import com.smartsure.auth.repository.UserRepository;
import com.smartsure.auth.security.JwtUtil;
import com.smartsure.auth.messaging.EmailEventPublisher;
import com.smartsure.auth.messaging.EmailRequest;
import com.smartsure.auth.dto.ForgotPasswordRequest;
import com.smartsure.auth.dto.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailEventPublisher emailEventPublisher;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Registration failed: Email already exists - {}", request.getEmail());
            return ApiResponse.error("Email is already registered");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new AuthServiceException("Role not found"));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(customerRole));

        User savedUser = userRepository.save(user);

        log.info("Successfully registered user: {}", request.getEmail());
        
        emailEventPublisher.publishEmailEvent(EmailRequest.builder()
                .to(savedUser.getEmail())
                .subject("Welcome to the SmartSure Family! 🛡️")
                .body("Hello " + savedUser.getFirstName() + ",\n\n" +
                      "Welcome to SmartSure! We are thrilled to have you on board. Your account has been successfully created, and you are now part of a community that prioritizes safety and peace of mind.\n\n" +
                      "You can now explore our insurance policies and manage your claims with ease. If you have any questions, our support team is always here to help.\n\n" +
                      "Thank you for choosing SmartSure—where your security is our priority.\n\n" +
                      "Best regards,\n" +
                      "The SmartSure Team")
                .build());

        return ApiResponse.success("User registered successfully", userMapper.toResponse(savedUser));
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthServiceException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Login failed: Invalid password for {}", request.getEmail());
            throw new AuthServiceException("Invalid credentials");
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        
        String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        UserResponse userResponse = userMapper.toResponse(user);
        userResponse.setToken(token);
        userResponse.setRefreshToken(refreshToken.getToken());

        log.info("Successfully logged in user: {}", request.getEmail());
        
        emailEventPublisher.publishEmailEvent(EmailRequest.builder()
                .to(user.getEmail())
                .subject("SmartSure Login Alert 🔐")
                .body("Hello " + user.getFirstName() + ",\n\n" +
                      "This is a quick security notification to let you know that a successful login to your SmartSure account occurred on " + LocalDateTime.now() + ".\n\n" +
                      "If this was you, you can safely ignore this email. If you did not perform this login, please contact our support team immediately to secure your account.\n\n" +
                      "Thank you for trusting us with your protection.\n\n" +
                      "Stay safe,\n" +
                      "The SmartSure Team")
                .build());

        return ApiResponse.success("Login successful", userResponse);
    }

    @Override
    @Transactional
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthServiceException("User not found with email: " + request.getEmail()));

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        log.info("Generated OTP for user {}: {}", request.getEmail(), otp);

        emailEventPublisher.publishEmailEvent(EmailRequest.builder()
                .to(user.getEmail())
                .subject("SmartSure Password Reset OTP 🗝️")
                .body("Hello " + user.getFirstName() + ",\n\n" +
                      "We received a request to reset the password for your SmartSure account. Your one-time password (OTP) is:\n\n" +
                      "👉 " + otp + "\n\n" +
                      "This OTP is valid for the next 15 minutes. If you did not request a password reset, you can safely ignore this email.\n\n" +
                      "Thank you for your commitment to security.\n\n" +
                      "Best regards,\n" +
                      "The SmartSure Security Team")
                .build());

        return ApiResponse.success("OTP sent to your registered email address.", "OTP_SENT");
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthServiceException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            log.error("Password reset failed: Invalid OTP for {}", request.getEmail());
            throw new AuthServiceException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            log.error("Password reset failed: Expired OTP for {}", request.getEmail());
            throw new AuthServiceException("OTP has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        log.info("Successfully reset password for user: {}", request.getEmail());

        emailEventPublisher.publishEmailEvent(EmailRequest.builder()
                .to(user.getEmail())
                .subject("SmartSure Password Reset Successful 🛡️")
                .body("Hello " + user.getFirstName() + ",\n\n" +
                      "This email confirms that your SmartSure account password has been successfully updated on " + LocalDateTime.now() + ".\n\n" +
                      "Security Tip: If you did not make this change, please contact our support team immediately.\n\n" +
                      "Thank you for choosing SmartSure for your protection.\n\n" +
                      "Warm regards,\n" +
                      "The SmartSure Team")
                .build());

        return ApiResponse.success("Password reset successful", "PASSWORD_UPDATED");
    }

    @Override
    public ApiResponse<TokenRefreshResponse> refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .toList();
                    String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
                    return ApiResponse.success("Token refreshed successfully", new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new AuthServiceException("Refresh token is not in database!"));
    }

    @Override
    @Transactional
    public ApiResponse<String> initiateOtpLogin(LoginOtpRequest request) {
        log.info("Processing OTP login initiation for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthServiceException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Login MFA failed: Invalid password for {}", request.getEmail());
            throw new AuthServiceException("Invalid credentials");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setLoginOtp(otp);
        user.setLoginOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        log.info("Generated Login OTP for user {}: {}", request.getEmail(), otp);

        emailEventPublisher.publishEmailEvent(EmailRequest.builder()
                .to(user.getEmail())
                .subject("SmartSure Secure Login OTP 🔐")
                .body("Hello " + user.getFirstName() + ",\n\n" +
                      "A login attempt was made to your SmartSure account at exactly " + LocalDateTime.now() + " (Local Time).\n\n" +
                      "To proceed and securely access your dashboard, please enter the following One-Time Password (OTP):\n\n" +
                      "👉 " + otp + "\n\n" +
                      "This code is valid for 5 minutes. If you did not attempt to log in, please contact our SmartSure Security team immediately.\n\n" +
                      "Thank you for trusting us with your protection.\n\n" +
                      "Securely yours,\n" +
                      "The SmartSure Security Gateway")
                .build());

        return ApiResponse.success("OTP sent to your registered email address.", "OTP_SENT");
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> verifyOtpLogin(VerifyLoginRequest request) {
        log.info("Processing MFA verification for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthServiceException("User not found"));

        if (user.getLoginOtp() == null || !user.getLoginOtp().equals(request.getOtp())) {
            log.error("MFA verification failed: Invalid OTP for {}", request.getEmail());
            throw new AuthServiceException("Invalid MFA OTP");
        }

        if (user.getLoginOtpExpiry().isBefore(LocalDateTime.now())) {
            log.error("MFA verification failed: Expired OTP for {}", request.getEmail());
            throw new AuthServiceException("MFA OTP has expired");
        }

        // OTP verified successfully! Generate standard tokens
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        UserResponse userResponse = userMapper.toResponse(user);
        userResponse.setToken(token);
        userResponse.setRefreshToken(refreshToken.getToken());

        // Clear the OTP for specific login so it cannot be reused
        user.setLoginOtp(null);
        user.setLoginOtpExpiry(null);
        userRepository.save(user);

        log.info("User {} successfully passed MFA authorization.", request.getEmail());

        return ApiResponse.success("Login MFA successful", userResponse);
    }

    @Override
    @Transactional
    public ApiResponse<String> logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
        return ApiResponse.success("Logged out successfully", "LOGOUT_SUCCESS");
    }
}