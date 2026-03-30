package com.smartsure.auth.mapper;

import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.dto.UserResponse;
import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.RoleName;
import com.smartsure.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    void toResponse_mapsUserToUserResponse() {
        Role customerRole = Role.builder().id(1L).name(RoleName.ROLE_CUSTOMER).build();
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encoded_pass")
                .createdAt(LocalDateTime.now())
                .roles(Set.of(customerRole))
                .build();

        UserResponse response = userMapper.toResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().contains("ROLE_CUSTOMER"));
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void toResponse_nullUser_returnsNull() {
        assertNull(userMapper.toResponse(null));
    }

    @Test
    void toEntity_mapsRegisterRequestToUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("raw_pass")
                .firstName("John")
                .lastName("Doe")
                .build();

        User user = userMapper.toEntity(request);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("raw_pass", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
    }

    @Test
    void toEntity_nullRequest_returnsNull() {
        assertNull(userMapper.toEntity(null));
    }
}
