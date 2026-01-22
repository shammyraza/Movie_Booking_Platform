package com.bookmyshow.repository;

import com.bookmyshow.entity.Seat;
import com.bookmyshow.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * SeatRepository - Demonstrates Dependency Inversion Principle
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByShowId(Long showId);
    
    List<Seat> findByShowIdAndStatus(Long showId, SeatStatus status);
    
    List<Seat> findByBookingId(Long bookingId);
}
