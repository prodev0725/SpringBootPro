package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLoginSuccess() {
        AppUser user = new AppUser("asdf", "encodedPassword");
        when(userRepository.findByUsername("asdf")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("asdf", "encodedPassword")).thenReturn(true);

        boolean result = authService.login("asdf", "asdf");
        assertTrue(result);
    }

    @Test
    void testLoginFail() {
        when(userRepository.findByUsername("asdf")).thenReturn(Optional.empty());
        boolean result = authService.login("asdf", "asdf");
        assertFalse(result);
    }
}
