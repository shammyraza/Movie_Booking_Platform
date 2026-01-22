package com.bookmyshow.controller;

import com.bookmyshow.dto.BookingRequest;
import com.bookmyshow.dto.BookingResponse;
import com.bookmyshow.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Booking Controller - WRITE SCENARIO Implementation
 * Demonstrates:
 * - Single Responsibility Principle: Only handles booking-related HTTP requests
 * - Dependency Inversion Principle: Depends on BookingService abstraction
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    
    /**
     * WRITE SCENARIO: Book movie tickets by selecting a theatre, timing, and preferred seats
     * 
     * @param request Booking request containing show ID and seat IDs
     * @param authentication Spring Security authentication object
     * @return Booking response with confirmation details
     */
    @PostMapping
    public ResponseEntity<BookingResponse> bookTickets(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info(">>> Incoming Request: POST /api/bookings");
        log.info("User: {}, Show ID: {}, Seats: {}", username, request.getShowId(), request.getSeatIds());
        
        BookingResponse response = bookingService.bookTickets(request, username);
        
        log.info("<<< Response: Booking successful - Reference: {}, Final Amount: ₹{}", 
            response.getBookingReference(), response.getFinalAmount());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
