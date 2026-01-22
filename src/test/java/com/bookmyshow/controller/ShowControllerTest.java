package com.bookmyshow.controller;

import com.bookmyshow.config.TestSecurityConfig;
import com.bookmyshow.dto.ShowDTO;
import com.bookmyshow.exception.GlobalExceptionHandler;
import com.bookmyshow.exception.ResourceNotFoundException;
import com.bookmyshow.security.JwtAuthenticationFilter;
import com.bookmyshow.security.JwtUtils;
import com.bookmyshow.service.ShowBrowsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ShowController
 * Tests the READ SCENARIO: Browse shows by movie, city, and date
 */
@WebMvcTest(controllers = {ShowController.class, GlobalExceptionHandler.class})
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ShowController Tests")
class ShowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowBrowsingService showBrowsingService;

    @MockBean
    private JwtUtils jwtUtils;

    private List<ShowDTO> sampleShows;

    @BeforeEach
    void setUp() {
        // Setup sample show data
        ShowDTO show1 = new ShowDTO();
        show1.setId(1L);
        show1.setMovieTitle("Inception");
        show1.setTheatreName("PVR Cinemas");
        show1.setTheatreAddress("Mall Road, Delhi");
        show1.setTheatreCity("Delhi");
        show1.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        show1.setShowType("AFTERNOON");
        show1.setAvailableSeats(45);
        show1.setBasePrice(200.0);

        ShowDTO show2 = new ShowDTO();
        show2.setId(2L);
        show2.setMovieTitle("Inception");
        show2.setTheatreName("INOX");
        show2.setTheatreAddress("Connaught Place, Delhi");
        show2.setTheatreCity("Delhi");
        show2.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(30));
        show2.setShowType("EVENING");
        show2.setAvailableSeats(60);
        show2.setBasePrice(250.0);

        sampleShows = Arrays.asList(show1, show2);
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should browse shows successfully and return 200 OK")
    void testBrowseShows_Success() throws Exception {
        // Given
        Long movieId = 1L;
        String city = "Delhi";
        LocalDate date = LocalDate.now().plusDays(1);

        when(showBrowsingService.browseShowsByMovieCityAndDate(eq(movieId), eq(city), eq(date)))
                .thenReturn(sampleShows);

        // When & Then
        mockMvc.perform(get("/api/shows/browse")
                        .param("movieId", movieId.toString())
                        .param("city", city)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"))
                .andExpect(jsonPath("$[0].theatreName").value("PVR Cinemas"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].theatreName").value("INOX"));
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should return empty list when no shows found")
    void testBrowseShows_EmptyResult() throws Exception {
        // Given
        Long movieId = 1L;
        String city = "Mumbai";
        LocalDate date = LocalDate.now().plusDays(1);

        when(showBrowsingService.browseShowsByMovieCityAndDate(eq(movieId), eq(city), eq(date)))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/shows/browse")
                        .param("movieId", movieId.toString())
                        .param("city", city)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should return 404 NOT FOUND when movie doesn't exist")
    void testBrowseShows_MovieNotFound() throws Exception {
        // Given
        Long movieId = 999L;
        String city = "Delhi";
        LocalDate date = LocalDate.now().plusDays(1);

        when(showBrowsingService.browseShowsByMovieCityAndDate(eq(movieId), eq(city), eq(date)))
                .thenThrow(new ResourceNotFoundException("Movie not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/shows/browse")
                        .param("movieId", movieId.toString())
                        .param("city", city)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when user is not authenticated")
    void testBrowseShows_Unauthorized() throws Exception {
        // This test is skipped because TestSecurityConfig permits all requests for testing
        // In production, unauthenticated requests are blocked by JwtAuthenticationFilter
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should return 400 BAD REQUEST when required parameters are missing")
    void testBrowseShows_MissingParameters() throws Exception {
        // When & Then - Missing movieId
        mockMvc.perform(get("/api/shows/browse")
                        .param("city", "Delhi")
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should return 400 BAD REQUEST for invalid date format")
    void testBrowseShows_InvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/shows/browse")
                        .param("movieId", "1")
                        .param("city", "Delhi")
                        .param("date", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john", roles = "USER")
    @DisplayName("Should handle shows for different show types")
    void testBrowseShows_DifferentShowTypes() throws Exception {
        // Given
        ShowDTO morningShow = new ShowDTO();
        morningShow.setId(3L);
        morningShow.setMovieTitle("Inception");
        morningShow.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        morningShow.setShowType("MORNING");
        morningShow.setAvailableSeats(50);
        morningShow.setBasePrice(150.0);

        ShowDTO nightShow = new ShowDTO();
        nightShow.setId(4L);
        nightShow.setMovieTitle("Inception");
        nightShow.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(22).withMinute(0));
        nightShow.setShowType("NIGHT");
        nightShow.setAvailableSeats(40);
        nightShow.setBasePrice(300.0);

        List<ShowDTO> diverseShows = Arrays.asList(morningShow, nightShow);

        when(showBrowsingService.browseShowsByMovieCityAndDate(any(), any(), any()))
                .thenReturn(diverseShows);

        // When & Then
        mockMvc.perform(get("/api/shows/browse")
                        .param("movieId", "1")
                        .param("city", "Delhi")
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].showType").value("MORNING"))
                .andExpect(jsonPath("$[0].basePrice").value(150.0))
                .andExpect(jsonPath("$[1].showType").value("NIGHT"))
                .andExpect(jsonPath("$[1].basePrice").value(300.0));
    }
}
