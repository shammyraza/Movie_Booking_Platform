package com.bookmyshow.service.impl;

import com.bookmyshow.dto.ShowDTO;
import com.bookmyshow.entity.Movie;
import com.bookmyshow.entity.Show;
import com.bookmyshow.entity.Theatre;
import com.bookmyshow.enums.ShowType;
import com.bookmyshow.exception.ResourceNotFoundException;
import com.bookmyshow.repository.MovieRepository;
import com.bookmyshow.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShowBrowsingServiceImpl
 * Tests the READ scenario implementation
 */
@ExtendWith(MockitoExtension.class)
class ShowBrowsingServiceImplTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ShowBrowsingServiceImpl showBrowsingService;

    private Movie movie;
    private Theatre theatre;
    private Show morningShow;
    private Show afternoonShow;

    @BeforeEach
    void setUp() {
        // Setup test data
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setLanguage("English");
        movie.setGenre("Sci-Fi");
        movie.setDurationMinutes(148);
        movie.setRating("UA");

        theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR Cinemas");
        theatre.setCity("Mumbai");
        theatre.setAddress("Phoenix Mall, Lower Parel");
        theatre.setTotalSeats(100);

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
        afternoonShow.setAvailableSeats(95);
    }

    @Test
    void testBrowseShowsByMovieCityAndDate_Success() {
        // Arrange
        Long movieId = 1L;
        String city = "Mumbai";
        LocalDate date = LocalDate.of(2026, 1, 21);
        
        List<Show> expectedShows = Arrays.asList(morningShow, afternoonShow);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showRepository.findShowsByMovieAndCityBetweenDates(
            eq(movieId), eq(city), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedShows);

        // Act
        List<ShowDTO> result = showBrowsingService.browseShowsByMovieCityAndDate(movieId, city, date);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        ShowDTO firstShow = result.get(0);
        assertEquals(1L, firstShow.getId());
        assertEquals("Inception", firstShow.getMovieTitle());
        assertEquals("PVR Cinemas", firstShow.getTheatreName());
        assertEquals("Mumbai", firstShow.getTheatreCity());
        assertEquals(200.0, firstShow.getBasePrice());
        assertEquals("MORNING", firstShow.getShowType());
        assertEquals(100, firstShow.getAvailableSeats());

        ShowDTO secondShow = result.get(1);
        assertEquals(2L, secondShow.getId());
        assertEquals("AFTERNOON", secondShow.getShowType());
        assertEquals(95, secondShow.getAvailableSeats());

        verify(movieRepository, times(1)).findById(movieId);
        verify(showRepository, times(1)).findShowsByMovieAndCityBetweenDates(
            eq(movieId), eq(city), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testBrowseShowsByMovieCityAndDate_MovieNotFound() {
        // Arrange
        Long movieId = 999L;
        String city = "Mumbai";
        LocalDate date = LocalDate.of(2026, 1, 21);

        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> showBrowsingService.browseShowsByMovieCityAndDate(movieId, city, date)
        );

        assertEquals("Movie not found with id: 999", exception.getMessage());
        verify(movieRepository, times(1)).findById(movieId);
        verify(showRepository, never()).findShowsByMovieAndCityBetweenDates(
            any(), any(), any(), any());
    }

    @Test
    void testBrowseShowsByMovieCityAndDate_NoShowsAvailable() {
        // Arrange
        Long movieId = 1L;
        String city = "Delhi";
        LocalDate date = LocalDate.of(2026, 1, 21);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showRepository.findShowsByMovieAndCityBetweenDates(
            eq(movieId), eq(city), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        // Act
        List<ShowDTO> result = showBrowsingService.browseShowsByMovieCityAndDate(movieId, city, date);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(movieRepository, times(1)).findById(movieId);
        verify(showRepository, times(1)).findShowsByMovieAndCityBetweenDates(
            eq(movieId), eq(city), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testBrowseShowsByMovieCityAndDate_DateRangeCorrect() {
        // Arrange
        Long movieId = 1L;
        String city = "Mumbai";
        LocalDate date = LocalDate.of(2026, 1, 21);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showRepository.findShowsByMovieAndCityBetweenDates(
            any(), any(), any(), any())).thenReturn(Arrays.asList(morningShow));

        // Act
        showBrowsingService.browseShowsByMovieCityAndDate(movieId, city, date);

        // Assert - Verify the date range is from start to end of day
        verify(showRepository).findShowsByMovieAndCityBetweenDates(
            eq(movieId),
            eq(city),
            eq(date.atStartOfDay()),
            eq(date.atTime(LocalTime.MAX))
        );
    }
}
