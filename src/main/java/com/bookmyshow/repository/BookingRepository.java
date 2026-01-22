package com.bookmyshow.repository;

import com.bookmyshow.entity.Booking;
import com.bookmyshow.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * BookingRepository - Demonstrates Dependency Inversion Principle
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);
    
    List<Booking> findByShowId(Long showId);
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByStatus(BookingStatus status);
    
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
}
