package com.smartsure.auth.dto;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testApiResponse() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("test");
        response.setData("data");

        assertTrue(response.isSuccess());
        assertEquals("test", response.getMessage());
        assertEquals("data", response.getData());

        ApiResponse<String> success = ApiResponse.success("ok", "val");
        assertTrue(success.isSuccess());
        assertEquals("val", success.getData());

        ApiResponse<String> error = ApiResponse.error("fail");
        assertFalse(error.isSuccess());
        assertNull(error.getData());
        
        ApiResponse<String> allArgs = new ApiResponse<>(true, "msg", "data");
        assertEquals("msg", allArgs.getMessage());
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass");

        assertEquals("test@test.com", request.getEmail());
        assertEquals("pass", request.getPassword());

        LoginRequest allArgs = new LoginRequest("test@test.com", "pass");
        assertEquals("pass", allArgs.getPassword());
        
        LoginRequest builder = LoginRequest.builder().email("e").password("p").build();
        assertEquals("e", builder.getEmail());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());

        RegisterRequest allArgs = new RegisterRequest("e", "p", "f", "l");
        assertEquals("f", allArgs.getFirstName());
    }

    @Test
    void testUserResponse() {
        UserResponse response = new UserResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setId(1L);
        response.setEmail("e");
        response.setFirstName("f");
        response.setLastName("l");
        response.setActive(true);
        response.setRoles(new HashSet<>());
        response.setCreatedAt(now);
        response.setToken("t");

        assertEquals(1L, response.getId());
        assertEquals("e", response.getEmail());
        assertTrue(response.isActive());
        assertEquals("t", response.getToken());
        assertEquals(now, response.getCreatedAt());

        UserResponse allArgs = new UserResponse(1L, "e", "f", "l", true, new HashSet<>(), now, "t", "rt");
        assertEquals("t", allArgs.getToken());
        assertEquals("rt", allArgs.getRefreshToken());
    }
}
