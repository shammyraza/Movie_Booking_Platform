package com.bookmyshow.service;

import com.bookmyshow.dto.BookingRequest;
import com.bookmyshow.dto.BookingResponse;

/**
 * Interface Segregation Principle - Specific interface for booking operations
 */
public interface BookingService {
    
    /**
     * Book movie tickets by selecting a theatre, timing, and preferred seats
     * @param request Booking request
     * @param username Username of the user making the booking
     * @return Booking response
     */
    BookingResponse bookTickets(BookingRequest request, String username);
}
