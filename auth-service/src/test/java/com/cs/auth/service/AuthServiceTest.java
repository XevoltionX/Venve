package com.cs.auth.service;

import com.cs.auth.dto.LoginRequest;
import com.cs.auth.dto.RegisterRequest;
import com.cs.auth.entity.User;
import com.cs.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // AuthService uses field injection via constructor, Mockito handles it
    }

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUserName("newuser");
        req.setPassword("123456");

        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        Map<String, Object> result = authService.register(req);

        assertEquals(1L, result.get("userId"));
        assertEquals("newuser", result.get("userName"));
        assertEquals("ADMIN", result.get("role")); // first user = admin
        assertNotNull(result.get("token"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUserName("existing");
        req.setPassword("123456");

        when(userRepository.existsByUserName("existing")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(req));
        assertTrue(ex.getMessage().contains("已存在"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUserName("test");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("pw123"));
        user.setRole("USER");

        when(userRepository.findByUserName("test")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setUserName("test");
        req.setPassword("pw123");

        Map<String, Object> result = authService.login(req);

        assertEquals(1L, result.get("userId"));
        assertEquals("USER", result.get("role"));
        assertNotNull(result.get("token"));
    }

    @Test
    void shouldRejectWrongPassword() {
        User user = new User();
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("pw123"));
        when(userRepository.findByUserName("test")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setUserName("test");
        req.setPassword("wrongpassword");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().contains("错误"));
    }
}
