package com.cs.common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void shouldGenerateAndValidateToken() {
        String token = JwtUtil.generate(1L, "testuser", "USER");

        assertNotNull(token);
        assertTrue(JwtUtil.validate(token));
        assertEquals(1L, JwtUtil.getUserId(token));
        assertEquals("testuser", JwtUtil.getUserName(token));
        assertEquals("USER", JwtUtil.getRole(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(JwtUtil.validate("invalid.token.here"));
        assertFalse(JwtUtil.validate(""));
        assertFalse(JwtUtil.validate(null));
    }

    @Test
    void shouldDefaultRoleToUserWhenMissing() {
        String token = JwtUtil.generate(2L, "norole", null);
        assertEquals("USER", JwtUtil.getRole(token));
    }

    @Test
    void shouldContainAdminRole() {
        String token = JwtUtil.generate(3L, "admin", "ADMIN");
        assertEquals("ADMIN", JwtUtil.getRole(token));
    }

    @Test
    void shouldParseMultipleTokensIndependently() {
        String t1 = JwtUtil.generate(1L, "alice", "USER");
        String t2 = JwtUtil.generate(2L, "bob", "ADMIN");

        assertEquals("alice", JwtUtil.getUserName(t1));
        assertEquals("bob", JwtUtil.getUserName(t2));
        assertEquals("USER", JwtUtil.getRole(t1));
        assertEquals("ADMIN", JwtUtil.getRole(t2));
    }
}
