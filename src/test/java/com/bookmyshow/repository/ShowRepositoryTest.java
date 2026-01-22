package com.bookmyshow.repository;

import com.bookmyshow.entity.Movie;
import com.bookmyshow.entity.Show;
import com.bookmyshow.entity.Theatre;
import com.bookmyshow.enums.ShowType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShowRepository
 * Tests custom query methods
 */
@DataJpaTest
class ShowRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShowRepository showRepository;

    private Movie movie;
    private Theatre theatre1;
    private Theatre theatre2;

    @BeforeEach
    void setUp() {
        // Create and persist movie
        movie = new Movie();
        movie.setTitle("Inception");
        movie.setDescription("A mind-bending thriller");
        movie.setLanguage("English");
        movie.setGenre("Sci-Fi");
        movie.setDurationMinutes(148);
        movie.setRating("UA");
        entityManager.persist(movie);

        // Create and persist theatres
        theatre1 = new Theatre();
        theatre1.setName("PVR Cinemas");
        theatre1.setCity("Mumbai");
        theatre1.setAddress("Phoenix Mall");
        theatre1.setTotalSeats(100);
        entityManager.persist(theatre1);

        theatre2 = new Theatre();
        theatre2.setName("INOX");
        theatre2.setCity("Delhi");
        theatre2.setAddress("DLF Mall");
        theatre2.setTotalSeats(120);
        entityManager.persist(theatre2);

        entityManager.flush();
    }

    @Test
    void testFindShowsByMovieAndCityBetweenDates_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        Show show = createShow(movie, theatre1, today.atTime(10, 0), ShowType.MORNING, 200.0);
        entityManager.persist(show);
        entityManager.flush();

        // Act
        List<Show> shows = showRepository.findShowsByMovieAndCityBetweenDates(
            movie.getId(),
            "Mumbai",
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX)
        );

        // Assert
        assertNotNull(shows);
        assertEquals(1, shows.size());
        assertEquals("Inception", shows.get(0).getMovie().getTitle());
        assertEquals("Mumbai", shows.get(0).getTheatre().getCity());
    }

    @Test
    void testFindShowsByMovieAndCityBetweenDates_MultipleShows() {
        // Arrange
        LocalDate today = LocalDate.now();
        Show show1 = createShow(movie, theatre1, today.atTime(10, 0), ShowType.MORNING, 200.0);
        Show show2 = createShow(movie, theatre1, today.atTime(14, 0), ShowType.AFTERNOON, 150.0);
        Show show3 = createShow(movie, theatre1, today.atTime(18, 0), ShowType.EVENING, 250.0);
        
        entityManager.persist(show1);
        entityManager.persist(show2);
        entityManager.persist(show3);
        entityManager.flush();

        // Act
        List<Show> shows = showRepository.findShowsByMovieAndCityBetweenDates(
            movie.getId(),
            "Mumbai",
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX)
        );

        // Assert
        assertEquals(3, shows.size());
    }

    @Test
    void testFindShowsByMovieAndCityBetweenDates_DifferentCity() {
        // Arrange
        LocalDate today = LocalDate.now();
        Show mumbaiShow = createShow(movie, theatre1, today.atTime(10, 0), ShowType.MORNING, 200.0);
        Show delhiShow = createShow(movie, theatre2, today.atTime(10, 0), ShowType.MORNING, 200.0);
        
        entityManager.persist(mumbaiShow);
        entityManager.persist(delhiShow);
        entityManager.flush();

        // Act
        List<Show> mumbaiShows = showRepository.findShowsByMovieAndCityBetweenDates(
            movie.getId(),
            "Mumbai",
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX)
        );

        // Assert
        assertEquals(1, mumbaiShows.size());
        assertEquals("Mumbai", mumbaiShows.get(0).getTheatre().getCity());
    }

    @Test
    void testFindShowsByMovieAndCityBetweenDates_NoShows() {
        // Act
        List<Show> shows = showRepository.findShowsByMovieAndCityBetweenDates(
            movie.getId(),
            "Mumbai",
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atTime(LocalTime.MAX)
        );

        // Assert
        assertTrue(shows.isEmpty());
    }

    @Test
    void testFindByMovieId() {
        // Arrange
        LocalDate today = LocalDate.now();
        Show show = createShow(movie, theatre1, today.atTime(10, 0), ShowType.MORNING, 200.0);
        entityManager.persist(show);
        entityManager.flush();

        // Act
        List<Show> shows = showRepository.findByMovieId(movie.getId());

        // Assert
        assertEquals(1, shows.size());
        assertEquals(movie.getId(), shows.get(0).getMovie().getId());
    }

    @Test
    void testFindByTheatreId() {
        // Arrange
        LocalDate today = LocalDate.now();
        Show show = createShow(movie, theatre1, today.atTime(10, 0), ShowType.MORNING, 200.0);
        entityManager.persist(show);
        entityManager.flush();

        // Act
        List<Show> shows = showRepository.findByTheatreId(theatre1.getId());

        // Assert
        assertEquals(1, shows.size());
        assertEquals(theatre1.getId(), shows.get(0).getTheatre().getId());
    }

    private Show createShow(Movie movie, Theatre theatre, LocalDateTime dateTime, 
                           ShowType showType, Double basePrice) {
        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setShowDateTime(dateTime);
        show.setShowType(showType);
        show.setBasePrice(basePrice);
        show.setAvailableSeats(theatre.getTotalSeats());
        return show;
    }
}
