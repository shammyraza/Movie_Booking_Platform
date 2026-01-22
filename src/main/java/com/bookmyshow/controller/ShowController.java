package com.bookmyshow.controller;

import com.bookmyshow.dto.ShowDTO;
import com.bookmyshow.service.ShowBrowsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Show Controller - READ SCENARIO Implementation
 * Demonstrates:
 * - Single Responsibility Principle: Only handles show-related HTTP requests
 * - Dependency Inversion Principle: Depends on ShowBrowsingService abstraction
 */
@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Slf4j
public class ShowController {
    
    private final ShowBrowsingService showBrowsingService;
    
    /**
     * READ SCENARIO: Browse theatres showing a selected movie in a town with show timings by date
     * 
     * @param movieId Movie ID to search for
     * @param city City name
     * @param date Date to search shows (format: yyyy-MM-dd)
     * @return List of shows matching the criteria
     */
    @GetMapping("/browse")
    public ResponseEntity<List<ShowDTO>> browseShows(
            @RequestParam Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info(">>> Incoming Request: GET /api/shows/browse");
        log.info("Parameters - Movie ID: {}, City: {}, Date: {}", movieId, city, date);
        
        List<ShowDTO> shows = showBrowsingService.browseShowsByMovieCityAndDate(movieId, city, date);
        
        log.info("<<< Response: {} shows found", shows.size());
        return ResponseEntity.ok(shows);
    }
}
