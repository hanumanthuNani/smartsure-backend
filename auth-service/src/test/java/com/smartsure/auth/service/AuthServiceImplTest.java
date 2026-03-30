package com.smartsure.auth.service;

import com.smartsure.auth.dto.ApiResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.dto.UserResponse;
import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.RoleName;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.exception.AuthServiceException;
import com.smartsure.auth.mapper.UserMapper;
import com.smartsure.auth.repository.RoleRepository;
import com.smartsure.auth.repository.UserRepository;
import com.smartsure.auth.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── REGISTER TESTS ───────────────────────────────────

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Role role = new Role();
        role.setName(RoleName.ROLE_CUSTOMER);

        User user = new User();
        user.setEmail("test@example.com");

        User savedUser = new User();
        savedUser.setId(1L);

        UserResponse userResponse = new UserResponse();

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).willReturn(Optional.of(role));
        given(userMapper.toEntity(request)).willReturn(user);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(user)).willReturn(savedUser);
        given(userMapper.toResponse(savedUser)).willReturn(userResponse);

        ApiResponse<UserResponse> response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());

        then(passwordEncoder).should().encode("password123");
        then(userRepository).should().save(user);
    }

    @Test
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        ApiResponse<UserResponse> response = authService.register(request);

        assertFalse(response.isSuccess());
        assertEquals("Email is already registered", response.getMessage());
        assertNull(response.getData());

        then(userRepository).should(never()).save(any());
    }

    @Test
    void register_roleNotFound() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).willReturn(Optional.empty());

        AuthServiceException exception = assertThrows(AuthServiceException.class,
                () -> authService.register(request));
        assertEquals("Role not found", exception.getMessage());

        then(userRepository).should(never()).save(any());
    }

    // ─── LOGIN TESTS ───────────────────────────────────────

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        UserResponse userResponse = new UserResponse();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(userMapper.toResponse(user)).willReturn(userResponse);
        given(jwtUtil.generateToken(anyString(), anyList(), anyLong())).willReturn("mock-token");
        

        ApiResponse<UserResponse> response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        AuthServiceException exception = assertThrows(AuthServiceException.class,
                () -> authService.login(request));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setPassword("encodedPassword");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

        AuthServiceException exception = assertThrows(AuthServiceException.class,
                () -> authService.login(request));
        assertEquals("Invalid credentials", exception.getMessage());
    }
}