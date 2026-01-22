package com.bookmyshow.repository;

import com.bookmyshow.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * TheatreRepository - Demonstrates Dependency Inversion Principle
 */
@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    
    List<Theatre> findByCity(String city);
    
    List<Theatre> findByCityIgnoreCase(String city);
    
    List<Theatre> findByNameContainingIgnoreCase(String name);
}
