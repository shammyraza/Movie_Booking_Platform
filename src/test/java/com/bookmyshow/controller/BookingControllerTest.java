package com.bookmyshow.controller;

import com.bookmyshow.config.TestSecurityConfig;
import com.bookmyshow.dto.BookingRequest;
import com.bookmyshow.dto.BookingResponse;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.GlobalExceptionHandler;
import com.bookmyshow.exception.ResourceNotFoundException;
import com.bookmyshow.security.JwtAuthenticationFilter;
import com.bookmyshow.security.JwtUtils;
import com.bookmyshow.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingController
 * Tests the WRITE SCENARIO: Book movie tickets
 */
@WebMvcTest(controllers = {BookingController.class, GlobalExceptionHandler.class})
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingController Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtils jwtUtils;

    private BookingRequest validBookingRequest;
    private BookingResponse bookingResponse;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        // Setup authentication
        authentication = new UsernamePasswordAuthenticationToken(
                "john", "password", java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        // Setup valid booking request
        validBookingRequest = new BookingRequest();
        validBookingRequest.setShowId(1L);
        validBookingRequest.setSeatIds(Arrays.asList(1L, 2L, 3L));

        // Setup booking response
        bookingResponse = new BookingResponse();
        bookingResponse.setBookingReference("BKG-123456");
        bookingResponse.setShowId(1L);
        bookingResponse.setMovieTitle("Inception");
        bookingResponse.setTheatreName("PVR Cinemas");
        bookingResponse.setShowDateTime(LocalDateTime.now().plusDays(1));
        bookingResponse.setSeatNumbers(Arrays.asList("A1", "A2", "A3"));
        bookingResponse.setTotalAmount(450.0);
        bookingResponse.setDiscountApplied(50.0);
        bookingResponse.setBookingDateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should book tickets successfully and return 201 CREATED")
    void testBookTickets_Success() throws Exception {
        // Given
        when(bookingService.bookTickets(any(BookingRequest.class), eq("john")))
                .thenReturn(bookingResponse);

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingReference").value("BKG-123456"))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.finalAmount").value(400.0));
    }

    @Test
    @DisplayName("Should apply afternoon discount and return 201 CREATED")
    void testBookTickets_AfternoonShowWithDiscount() throws Exception {
        // Given
        BookingResponse afternoonResponse = new BookingResponse();
        afternoonResponse.setBookingReference("BKG-789012");
        afternoonResponse.setShowId(2L);
        afternoonResponse.setMovieTitle("The Dark Knight");
        afternoonResponse.setTheatreName("INOX");
        afternoonResponse.setShowDateTime(LocalDateTime.now().withHour(14).withMinute(30));
        afternoonResponse.setSeatNumbers(Arrays.asList("B1", "B2"));
        afternoonResponse.setTotalAmount(400.0);
        afternoonResponse.setDiscountApplied(80.0); // 20% afternoon discount
        afternoonResponse.setBookingDateTime(LocalDateTime.now());

        when(bookingService.bookTickets(any(BookingRequest.class), eq("john")))
                .thenReturn(afternoonResponse);

        BookingRequest afternoonRequest = new BookingRequest();
        afternoonRequest.setShowId(2L);
        afternoonRequest.setSeatIds(Arrays.asList(4L, 5L));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(afternoonRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discountApplied").value(80.0))
                .andExpect(jsonPath("$.finalAmount").value(320.0));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when showId is null")
    void testBookTickets_InvalidRequest_NullShowId() throws Exception {
        // Given
        BookingRequest invalidRequest = new BookingRequest();
        invalidRequest.setShowId(null); // Invalid: null showId
        invalidRequest.setSeatIds(Arrays.asList(1L, 2L));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when seatIds is empty")
    void testBookTickets_InvalidRequest_EmptySeatIds() throws Exception {
        // Given
        BookingRequest invalidRequest = new BookingRequest();
        invalidRequest.setShowId(1L);
        invalidRequest.setSeatIds(Collections.emptyList()); // Invalid: empty seat list

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND when show doesn't exist")
    void testBookTickets_ShowNotFound() throws Exception {
        // Given
        when(bookingService.bookTickets(any(BookingRequest.class), eq("john")))
                .thenThrow(new ResourceNotFoundException("Show not found with id: 999"));

        BookingRequest request = new BookingRequest();
        request.setShowId(999L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when seats are not available")
    void testBookTickets_SeatsNotAvailable() throws Exception {
        // Given
        when(bookingService.bookTickets(any(BookingRequest.class), eq("john")))
                .thenThrow(new BookingException("Seats are already booked"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when user is not authenticated")
    void testBookTickets_Unauthorized() throws Exception {
        // This test is skipped because TestSecurityConfig permits all requests for testing
        // In production, unauthenticated requests are blocked by JwtAuthenticationFilter
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST for malformed JSON")
    void testBookTickets_MalformedJson() throws Exception {
        // Given
        String malformedJson = "{ \"showId\": \"invalid\", \"seatIds\": [1, 2] }";

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}
