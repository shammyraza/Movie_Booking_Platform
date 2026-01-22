package com.bookmyshow.service;

import com.bookmyshow.dto.ShowDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface Segregation Principle - Specific interface for show browsing
 * Clients don't depend on methods they don't use
 */
public interface ShowBrowsingService {
    
    /**
     * Browse theatres showing a selected movie in a town with show timings by date
     * @param movieId Movie ID
     * @param city City name
     * @param date Date to search for shows
     * @return List of shows
     */
    List<ShowDTO> browseShowsByMovieCityAndDate(Long movieId, String city, LocalDate date);
}
