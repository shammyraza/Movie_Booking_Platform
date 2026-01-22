package com.bookmyshow.config;

import com.bookmyshow.security.JwtAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test security configuration that mocks JWT authentication filter for controller tests.
 */
@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
}
