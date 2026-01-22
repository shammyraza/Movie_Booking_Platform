package com.bookmyshow.service.impl;

import com.bookmyshow.dto.BookingRequest;
import com.bookmyshow.dto.BookingResponse;
import com.bookmyshow.entity.*;
import com.bookmyshow.enums.BookingStatus;
import com.bookmyshow.enums.SeatStatus;
import com.bookmyshow.enums.SeatType;
import com.bookmyshow.enums.ShowType;
import com.bookmyshow.exception.BookingException;
import com.bookmyshow.exception.ResourceNotFoundException;
import com.bookmyshow.repository.*;
import com.bookmyshow.service.DiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingServiceImpl
 * Tests the WRITE scenario implementation
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiscountStrategy discountStrategy;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Movie movie;
    private Theatre theatre;
    private Show morningShow;
    private Show afternoonShow;
    private Seat seat1;
    private Seat seat2;
    private Seat seat3;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded_password");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR Cinemas");
        theatre.setCity("Mumbai");

        morningShow = new Show();
        morningShow.setId(1L);
        morningShow.setMovie(movie);
        morningShow.setTheatre(theatre);
        morningShow.setShowDateTime(LocalDateTime.of(2026, 1, 21, 10, 0));
        morningShow.setBasePrice(200.0);
        morningShow.setShowType(ShowType.MORNING);
        morningShow.setAvailableSeats(100);

        afternoonShow = new Show();
        afternoonShow.setId(2L);
        afternoonShow.setMovie(movie);
        afternoonShow.setTheatre(theatre);
        afternoonShow.setShowDateTime(LocalDateTime.of(2026, 1, 21, 14, 0));
        afternoonShow.setBasePrice(150.0);
        afternoonShow.setShowType(ShowType.AFTERNOON);
        afternoonShow.setAvailableSeats(100);

        seat1 = new Seat();
        seat1.setId(1L);
        seat1.setShow(morningShow);
        seat1.setSeatNumber("R1");
        seat1.setSeatType(SeatType.REGULAR);
        seat1.setStatus(SeatStatus.AVAILABLE);
        seat1.setPrice(200.0);

        seat2 = new Seat();
        seat2.setId(2L);
        seat2.setShow(morningShow);
        seat2.setSeatNumber("R2");
        seat2.setSeatType(SeatType.REGULAR);
        seat2.setStatus(SeatStatus.AVAILABLE);
        seat2.setPrice(200.0);

        seat3 = new Seat();
        seat3.setId(3L);
        seat3.setShow(morningShow);
        seat3.setSeatNumber("R3");
        seat3.setSeatType(SeatType.REGULAR);
        seat3.setStatus(SeatStatus.AVAILABLE);
        seat3.setPrice(200.0);
    }

    @Test
    void testBookTickets_Success_ThreeSeats_WithDiscount() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L, 3L));

        List<Seat> seats = Arrays.asList(seat1, seat2, seat3);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);
        when(discountStrategy.calculateDiscount(600.0, 3, false)).thenReturn(100.0);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setUser(user);
        savedBooking.setShow(morningShow);
        savedBooking.setSeats(seats);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        savedBooking.setTotalAmount(500.0);
        savedBooking.setDiscountApplied(100.0);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setBookingReference("BMS-TEST123");
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        BookingResponse response = bookingService.bookTickets(request, "john");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        assertEquals("BMS-TEST123", response.getBookingReference());
        assertEquals("Inception", response.getMovieTitle());
        assertEquals("PVR Cinemas", response.getTheatreName());
        assertEquals(500.0, response.getTotalAmount());
        assertEquals(100.0, response.getDiscountApplied());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(3, response.getSeatNumbers().size());

        verify(userRepository, times(1)).findByUsername("john");
        verify(showRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).findAllById(request.getSeatIds());
        verify(discountStrategy, times(1)).calculateDiscount(600.0, 3, false);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(showRepository, times(1)).save(morningShow);
        verify(seatRepository, times(3)).save(any(Seat.class));
    }

    @Test
    void testBookTickets_AfternoonShow_WithDiscount() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(2L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        seat1.setShow(afternoonShow);
        seat2.setShow(afternoonShow);
        seat1.setPrice(150.0);
        seat2.setPrice(150.0);
        
        List<Seat> seats = Arrays.asList(seat1, seat2);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(2L)).thenReturn(Optional.of(afternoonShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);
        when(discountStrategy.calculateDiscount(300.0, 2, true)).thenReturn(60.0);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setUser(user);
        savedBooking.setShow(afternoonShow);
        savedBooking.setSeats(seats);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        savedBooking.setTotalAmount(240.0);
        savedBooking.setDiscountApplied(60.0);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setBookingReference("BMS-AFTER123");
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        BookingResponse response = bookingService.bookTickets(request, "john");

        // Assert
        assertNotNull(response);
        assertEquals(240.0, response.getTotalAmount());
        assertEquals(60.0, response.getDiscountApplied());
        
        // Verify afternoon show discount was calculated
        verify(discountStrategy, times(1)).calculateDiscount(300.0, 2, true);
    }

    @Test
    void testBookTickets_UserNotFound() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> bookingService.bookTickets(request, "nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(showRepository, never()).findById(any());
    }

    @Test
    void testBookTickets_ShowNotFound() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(999L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> bookingService.bookTickets(request, "john")
        );

        assertTrue(exception.getMessage().contains("Show not found"));
        verify(userRepository, times(1)).findByUsername("john");
        verify(showRepository, times(1)).findById(999L);
        verify(seatRepository, never()).findAllById(any());
    }

    @Test
    void testBookTickets_SeatsNotFound() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L, 3L));

        List<Seat> seats = Arrays.asList(seat1, seat2); // Only 2 seats found, 3 requested

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);

        // Act & Assert
        BookingException exception = assertThrows(
            BookingException.class,
            () -> bookingService.bookTickets(request, "john")
        );

        assertEquals("Some seats were not found", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testBookTickets_SeatsNotAvailable() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        seat1.setStatus(SeatStatus.BOOKED); // Already booked
        List<Seat> seats = Arrays.asList(seat1, seat2);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);

        // Act & Assert
        BookingException exception = assertThrows(
            BookingException.class,
            () -> bookingService.bookTickets(request, "john")
        );

        assertTrue(exception.getMessage().contains("Seats not available"));
        assertTrue(exception.getMessage().contains("R1"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testBookTickets_UpdatesSeatStatus() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L));

        List<Seat> seats = Arrays.asList(seat1, seat2);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);
        when(discountStrategy.calculateDiscount(anyDouble(), anyInt(), anyBoolean())).thenReturn(0.0);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setBookingReference("BMS-TEST");
        savedBooking.setShow(morningShow);
        savedBooking.setSeats(seats);
        savedBooking.setTotalAmount(400.0);
        savedBooking.setDiscountApplied(0.0);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        bookingService.bookTickets(request, "john");

        // Assert - Verify seat status is updated
        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository, times(2)).save(seatCaptor.capture());
        
        List<Seat> savedSeats = seatCaptor.getAllValues();
        assertTrue(savedSeats.stream().allMatch(s -> s.getStatus() == SeatStatus.BOOKED));
    }

    @Test
    void testBookTickets_UpdatesAvailableSeats() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L, 2L, 3L));

        List<Seat> seats = Arrays.asList(seat1, seat2, seat3);
        int initialAvailableSeats = morningShow.getAvailableSeats();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);
        when(discountStrategy.calculateDiscount(anyDouble(), anyInt(), anyBoolean())).thenReturn(0.0);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setBookingReference("BMS-TEST");
        savedBooking.setShow(morningShow);
        savedBooking.setSeats(seats);
        savedBooking.setTotalAmount(600.0);
        savedBooking.setDiscountApplied(0.0);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        bookingService.bookTickets(request, "john");

        // Assert - Verify available seats decreased
        assertEquals(initialAvailableSeats - 3, morningShow.getAvailableSeats());
        verify(showRepository, times(1)).save(morningShow);
    }

    @Test
    void testBookTickets_GeneratesUniqueBookingReference() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(Arrays.asList(1L));

        List<Seat> seats = Arrays.asList(seat1);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(morningShow));
        when(seatRepository.findAllById(request.getSeatIds())).thenReturn(seats);
        when(discountStrategy.calculateDiscount(anyDouble(), anyInt(), anyBoolean())).thenReturn(0.0);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setBookingReference("BMS-UNIQUE");
        savedBooking.setShow(morningShow);
        savedBooking.setSeats(seats);
        savedBooking.setTotalAmount(200.0);
        savedBooking.setDiscountApplied(0.0);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        BookingResponse response = bookingService.bookTickets(request, "john");

        // Assert - Verify booking reference format
        assertNotNull(response.getBookingReference());
        assertTrue(response.getBookingReference().startsWith("BMS-"));
    }
}
