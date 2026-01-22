package com.bookmyshow.service.impl;

import com.bookmyshow.dto.BookingRequest;
import com.bookmyshow.dto.BookingResponse;
import com.bookmyshow.entity.*;
import com.bookmyshow.enums.BookingStatus;
import com.bookmyshow.enums.SeatStatus;
import com.bookmyshow.enums.ShowType;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.ResourceNotFoundException;
import com.bookmyshow.repository.*;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.service.DiscountStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BookingServiceImpl
 * Demonstrates:
 * - Single Responsibility Principle: Only handles booking logic
 * - Dependency Inversion Principle: Depends on abstractions
 * - Strategy Pattern: Uses DiscountStrategy for flexible discount calculation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final DiscountStrategy discountStrategy;
    
    @Override
    @Transactional
    public BookingResponse bookTickets(BookingRequest request, String username) {
        log.info("=== Starting Booking Process ===");
        log.info("User: {}, Show ID: {}, Seat IDs: {}", 
            username, request.getShowId(), request.getSeatIds());
        
        // Fetch user
        log.debug("Fetching user details for username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("User not found: {}", username);
                return new ResourceNotFoundException("User not found: " + username);
            });
        log.debug("User found - ID: {}, Email: {}", user.getId(), user.getEmail());
        
        // Fetch show
        log.debug("Fetching show details for Show ID: {}", request.getShowId());
        Show show = showRepository.findById(request.getShowId())
            .orElseThrow(() -> {
                log.error("Show not found - Show ID: {}", request.getShowId());
                return new ResourceNotFoundException("Show not found: " + request.getShowId());
            });
        log.info("Show found - Movie: {}, Theatre: {}, DateTime: {}, Available Seats: {}", 
            show.getMovie().getTitle(), show.getTheatre().getName(), 
            show.getShowDateTime(), show.getAvailableSeats());
        
        // Fetch and validate seats
        log.debug("Fetching {} seats", request.getSeatIds().size());
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        
        if (seats.size() != request.getSeatIds().size()) {
            log.error("Seat count mismatch - Requested: {}, Found: {}", 
                request.getSeatIds().size(), seats.size());
            throw new BookingException("Some seats were not found");
        }
        log.debug("All {} seats found successfully", seats.size());
        
        // Validate all seats are available
        log.debug("Validating seat availability");
        validateSeatsAvailable(seats);
        log.debug("All seats are available for booking");
        
        // Calculate total amount
        double totalAmount = seats.stream()
            .mapToDouble(Seat::getPrice)
            .sum();
        log.info("Total amount calculated: ₹{} for {} seats", totalAmount, seats.size());
        
        // Apply discount strategy
        boolean isAfternoonShow = show.getShowType() == ShowType.AFTERNOON;
        log.debug("Applying discount strategy - Afternoon Show: {}, Seat Count: {}", 
            isAfternoonShow, seats.size());
        double discount = discountStrategy.calculateDiscount(
            totalAmount, seats.size(), isAfternoonShow
        );
        log.info("Discount applied: ₹{} ({}%)", discount, 
            String.format("%.2f", (discount / totalAmount) * 100));
        
        // Create booking
        log.debug("Creating booking entity");
        Booking booking = createBooking(user, show, seats, totalAmount, discount);
        
        // Update seat status
        log.debug("Updating seat status to BOOKED");
        updateSeatStatus(seats, booking);
        
        // Update show available seats
        int previousAvailableSeats = show.getAvailableSeats();
        show.setAvailableSeats(show.getAvailableSeats() - seats.size());
        showRepository.save(show);
        log.debug("Show available seats updated: {} -> {}", 
            previousAvailableSeats, show.getAvailableSeats());
        
        // Save booking
        booking = bookingRepository.save(booking);
        
        log.info("=== Booking Completed Successfully ===");
        log.info("Booking Reference: {}, Total Amount: ₹{}, Discount: ₹{}, Final Amount: ₹{}", 
            booking.getBookingReference(), booking.getTotalAmount(), 
            booking.getDiscountApplied(), booking.getFinalAmount());
        
        return convertToBookingResponse(booking);
    }
    
    /**
     * Validates that all seats are available for booking
     */
    private void validateSeatsAvailable(List<Seat> seats) {
        List<Seat> unavailableSeats = seats.stream()
            .filter(seat -> seat.getStatus() != SeatStatus.AVAILABLE)
            .collect(Collectors.toList());
        
        if (!unavailableSeats.isEmpty()) {
            String seatNumbers = unavailableSeats.stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.joining(", "));
            log.error("Seat validation failed - Unavailable seats: {}", seatNumbers);
            throw new BookingException("Seats not available: " + seatNumbers);
        }
    }
    
    /**
     * Creates a new booking entity
     */
    private Booking createBooking(User user, Show show, List<Seat> seats, 
                                  double totalAmount, double discount) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setSeats(seats);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setTotalAmount(totalAmount - discount);
        booking.setDiscountApplied(discount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingReference(generateBookingReference());
        return booking;
    }
    
    /**
     * Updates seat status and links them to booking
     */
    private void updateSeatStatus(List<Seat> seats, Booking booking) {
        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBooking(booking);
            seatRepository.save(seat);
        });
    }
    
    /**
     * Generates a unique booking reference
     */
    private String generateBookingReference() {
        return "BMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Converts booking entity to response DTO
     */
    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setShowId(booking.getShow().getId());
        response.setMovieTitle(booking.getShow().getMovie().getTitle());
        response.setTheatreName(booking.getShow().getTheatre().getName());
        response.setShowDateTime(booking.getShow().getShowDateTime());
        response.setSeatNumbers(booking.getSeats().stream()
            .map(Seat::getSeatNumber)
            .collect(Collectors.toList()));
        response.setTotalAmount(booking.getTotalAmount());
        response.setDiscountApplied(booking.getDiscountApplied());
        response.setStatus(booking.getStatus().name());
        response.setBookingDateTime(booking.getBookingDateTime());
        return response;
    }
}
