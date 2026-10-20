package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.*;
import com.bookmyshow.entity.*;
import com.bookmyshow.exception.ApiException;
import com.bookmyshow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {
    private static final String ROWS = "ABCDEFGH";
    private static final int COLS = 10;

    private final ShowRepository showRepo;
    private final ShowSeatRepository seatRepo;
    private final MovieRepository movieRepo;
    private final TheatreRepository theatreRepo;

    @Transactional
    public Show createShow(Movie movie, Theatre theatre, String screen, LocalDateTime start, BigDecimal price) {
        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreenName(screen);
        show.setStartTime(start);
        show.setPrice(price);
        showRepo.save(show);

        List<ShowSeat> seats = new ArrayList<>();
        for (char row : ROWS.toCharArray()) {
            for (int c = 1; c <= COLS; c++) {
                ShowSeat s = new ShowSeat();
                s.setShow(show);
                s.setSeatNumber("" + row + c);
                seats.add(s);
            }
        }
        seatRepo.saveAll(seats);
        return show;
    }

    @Transactional
    public ShowRes createShow(ShowReq r) {
        Movie m = movieRepo.findById(r.movieId()).orElseThrow(() -> ApiException.notFound("Movie not found"));
        Theatre t = theatreRepo.findById(r.theatreId()).orElseThrow(() -> ApiException.notFound("Theatre not found"));
        return toRes(createShow(m, t, r.screenName(), r.startTime(), r.price()));
    }

    @Transactional(readOnly = true)
    public List<ShowRes> listForMovie(Long movieId, String city, LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalDateTime from = d.equals(LocalDate.now()) ? LocalDateTime.now() : d.atStartOfDay();
        LocalDateTime to = d.plusDays(1).atStartOfDay();
        return showRepo.findByMovieIdAndStartTimeBetweenOrderByStartTime(movieId, from, to).stream()
                .filter(s -> city == null || city.isBlank() || s.getTheatre().getCity().equalsIgnoreCase(city))
                .map(this::toRes).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatRes> seats(Long showId) {
        LocalDateTime now = LocalDateTime.now();
        return seatRepo.findByShowId(showId).stream()
                .sorted(Comparator.comparing((ShowSeat s) -> s.getSeatNumber().charAt(0))
                        .thenComparingInt(s -> Integer.parseInt(s.getSeatNumber().substring(1))))
                .map(s -> {
                    boolean expired = s.getStatus() == SeatStatus.LOCKED && s.getLockedUntil() != null
                            && s.getLockedUntil().isBefore(now);
                    return new SeatRes(s.getSeatNumber(), expired ? "AVAILABLE" : s.getStatus().name());
                }).toList();
    }

    private ShowRes toRes(Show s) {
        return new ShowRes(s.getId(), s.getMovie().getId(), s.getMovie().getTitle(),
                s.getTheatre().getId(), s.getTheatre().getName(), s.getTheatre().getCity(),
                s.getScreenName(), s.getStartTime(), s.getPrice(),
                seatRepo.countByShowIdAndStatus(s.getId(), SeatStatus.AVAILABLE));
    }
}
