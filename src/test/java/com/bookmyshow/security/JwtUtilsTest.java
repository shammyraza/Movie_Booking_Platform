package com.bookmyshow.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtils
 * Tests JWT token generation and validation
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long jwtExpirationMs = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    void testGenerateJwtToken_Success() {
        // Arrange
        UserDetails userDetails = User.builder()
            .username("john")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUserNameFromJwtToken_Success() {
        // Arrange
        UserDetails userDetails = User.builder()
            .username("john")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals("john", username);
    }

    @Test
    void testValidateJwtToken_ValidToken() {
        // Arrange
        UserDetails userDetails = User.builder()
            .username("john")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateJwtToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_ExpiredToken() {
        // Arrange - Create an expired token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String expiredToken = Jwts.builder()
            .subject("john")
            .issuedAt(new Date(System.currentTimeMillis() - 100000))
            .expiration(new Date(System.currentTimeMillis() - 50000)) // Already expired
            .signWith(key)
            .compact();

        // Act
        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_EmptyToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_NullToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testTokenContainsCorrectSubject() {
        // Arrange
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        // Act
        String token = jwtUtils.generateJwtToken(authentication);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testGenerateTokenForDifferentUsers() {
        // Arrange
        UserDetails user1 = User.builder()
            .username("john")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        UserDetails user2 = User.builder()
            .username("jane")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();
        
        Authentication auth1 = new UsernamePasswordAuthenticationToken(
            user1, null, user1.getAuthorities());
        Authentication auth2 = new UsernamePasswordAuthenticationToken(
            user2, null, user2.getAuthorities());

        // Act
        String token1 = jwtUtils.generateJwtToken(auth1);
        String token2 = jwtUtils.generateJwtToken(auth2);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("john", jwtUtils.getUserNameFromJwtToken(token1));
        assertEquals("jane", jwtUtils.getUserNameFromJwtToken(token2));
    }
}
