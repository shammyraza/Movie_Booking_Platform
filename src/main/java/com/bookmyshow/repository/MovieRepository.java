package com.bookmyshow.repository;

import com.bookmyshow.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * MovieRepository - Demonstrates Dependency Inversion Principle
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    List<Movie> findByLanguage(String language);
    
    List<Movie> findByGenre(String genre);
    
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
