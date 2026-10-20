package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.LockReq;
import com.bookmyshow.entity.*;
import com.bookmyshow.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired BookingService bookingService;
    @Autowired ShowService showService;
    @Autowired UserRepository userRepo;
    @Autowired MovieRepository movieRepo;
    @Autowired TheatreRepository theatreRepo;
    @Autowired ShowSeatRepository seatRepo;

    @Test
    void tenUsersRaceForSameSeat_onlyOneWins() throws Exception {
        int threads = 10;

        // a fresh show far in the future (seed data comes from DataSeeder)
        Show show = showService.createShow(movieRepo.findAll().get(0), theatreRepo.findAll().get(0),
                "Test Audi", LocalDateTime.now().plusDays(10), BigDecimal.valueOf(100));
        Long showId = show.getId();

        List<String> emails = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            User u = new User();
            u.setName("Racer " + i);
            u.setEmail("racer" + i + "@test.com");
            u.setPassword("x");
            userRepo.save(u);
            emails.add(u.getEmail());
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (String email : emails) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();   // all threads fire at the same moment
                    bookingService.lockSeats(email, new LockReq(showId, List.of("A1")));
                    success.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                }
            });
        }

        ready.await();
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "threads did not finish in time");

        assertEquals(1, success.get(), "exactly one user must get the seat");
        assertEquals(threads - 1, failed.get(), "everyone else must be rejected");

        ShowSeat a1 = seatRepo.findByShowId(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A1")).findFirst().orElseThrow();
        assertEquals(SeatStatus.LOCKED, a1.getStatus());
    }
}