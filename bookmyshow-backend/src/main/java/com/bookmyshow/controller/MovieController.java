package com.bookmyshow.controller;

import com.bookmyshow.dto.Dto.ShowRes;
import com.bookmyshow.entity.Movie;
import com.bookmyshow.entity.Theatre;
import com.bookmyshow.repository.TheatreRepository;
import com.bookmyshow.service.MovieService;
import com.bookmyshow.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final ShowService showService;
    private final TheatreRepository theatreRepo;

    @GetMapping("/movies")
    public List<Movie> movies(@RequestParam(required = false) String q) { return movieService.list(q); }

    @GetMapping("/movies/{id}")
    public Movie movie(@PathVariable Long id) { return movieService.get(id); }

    /** e.g. /api/movies/1/shows?city=Nagpur&date=2026-10-20 */
    @GetMapping("/movies/{id}/shows")
    public List<ShowRes> shows(@PathVariable Long id,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showService.listForMovie(id, city, date);
    }

    @GetMapping("/theatres")
    public List<Theatre> theatres(@RequestParam(required = false) String city) {
        return city == null ? theatreRepo.findAll() : theatreRepo.findByCityIgnoreCase(city);
    }
}
