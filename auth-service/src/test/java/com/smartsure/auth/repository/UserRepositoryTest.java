package com.smartsure.auth.repository;

import com.smartsure.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void findByEmail_found() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        Optional<User> result = userRepository.findByEmail("test@test.com");
        assertTrue(result.isPresent());
    }

    @Test
    void findByEmail_notFound() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());
        Optional<User> result = userRepository.findByEmail("none@test.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_true() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        boolean result = userRepository.existsByEmail("test@test.com");
        assertTrue(result);
    }

    @Test
    void existsByEmail_false() {
        when(userRepository.existsByEmail("none@test.com")).thenReturn(false);
        boolean result = userRepository.existsByEmail("none@test.com");
        assertFalse(result);
    }
}
