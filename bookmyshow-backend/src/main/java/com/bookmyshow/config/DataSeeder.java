package com.bookmyshow.config;

import com.bookmyshow.entity.*;
import com.bookmyshow.repository.*;
import com.bookmyshow.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Inserts demo data on first run so the frontend has something to show. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    private final MovieRepository movieRepo;
    private final TheatreRepository theatreRepo;
    private final ShowService showService;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (movieRepo.count() > 0) return;

        user("Admin", "admin@bms.com", System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123"), Role.ADMIN);
        user("Demo User", "user@bms.com", "user123", Role.USER);

        List<Movie> movies = List.of(
                movie("Inception", "A thief who steals secrets through dreams is given a final impossible task.", "Sci-Fi", "English", 148, 8.8),
                movie("Interstellar", "Explorers travel through a wormhole in search of a new home for humanity.", "Sci-Fi", "English", 169, 8.7),
                movie("3 Idiots", "Three engineering students navigate friendship, pressure and finding their passion.", "Comedy", "Hindi", 170, 8.4),
                movie("Dangal", "A former wrestler trains his daughters to become world-class champions.", "Drama", "Hindi", 161, 8.3),
                movie("The Dark Knight", "Batman faces the Joker, a criminal mastermind who plunges Gotham into chaos.", "Action", "English", 152, 9.0),
                movie("RRR", "Two revolutionaries form an unlikely friendship while fighting British rule.", "Action", "Telugu", 182, 7.8));

        List<Theatre> theatres = List.of(
                theatre("PVR Cinemas", "Nagpur", "Empress Mall, Nagpur"),
                theatre("INOX Razdan", "Nagpur", "Dharampeth, Nagpur"),
                theatre("Cinepolis Andheri", "Mumbai", "Andheri West, Mumbai"));

        String[] screens = {"Audi 1", "Audi 2", "Audi 3"};
        LocalTime[] slots = {LocalTime.of(14, 0), LocalTime.of(18, 30), LocalTime.of(21, 45)};
        BigDecimal[] prices = {BigDecimal.valueOf(200), BigDecimal.valueOf(250), BigDecimal.valueOf(300)};

        for (int d = 0; d < 3; d++) {
            LocalDate date = LocalDate.now().plusDays(d);
            for (int m = 0; m < movies.size(); m++) {
                for (int t = 0; t < theatres.size(); t++) {
                    int slot = (m + t) % slots.length;
                    showService.createShow(movies.get(m), theatres.get(t), screens[(m + t) % 3],
                            date.atTime(slots[slot]), prices[slot]);
                }
            }
        }
        System.out.println("Seeded demo data. Login: admin@bms.com/admin123 or user@bms.com/user123");
    }

    private void user(String name, String email, String pw, Role role) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setPassword(encoder.encode(pw)); u.setRole(role);
        userRepo.save(u);
    }

    private Movie movie(String title, String desc, String genre, String lang, int mins, double rating) {
        Movie m = new Movie();
        m.setTitle(title); m.setDescription(desc); m.setGenre(genre); m.setLanguage(lang);
        m.setDurationMins(mins); m.setRating(rating); m.setReleaseDate(LocalDate.now().minusDays(30));
//        m.setPosterUrl("https://placehold.co/300x450?text=" + title.replace(" ", "+"));
        m.setPosterUrl("/posters/" + title.toLowerCase().replaceAll("[^a-z0-9]+", "-") + ".jpg");
        return movieRepo.save(m);
    }

    private Theatre theatre(String name, String city, String address) {
        Theatre t = new Theatre();
        t.setName(name); t.setCity(city); t.setAddress(address);
        return theatreRepo.save(t);
    }
}
