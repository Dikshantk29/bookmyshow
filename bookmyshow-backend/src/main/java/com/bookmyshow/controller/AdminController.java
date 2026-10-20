package com.bookmyshow.controller;

import com.bookmyshow.dto.Dto.*;
import com.bookmyshow.entity.Movie;
import com.bookmyshow.entity.Theatre;
import com.bookmyshow.repository.TheatreRepository;
import com.bookmyshow.service.MovieService;
import com.bookmyshow.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final MovieService movieService;
    private final ShowService showService;
    private final TheatreRepository theatreRepo;

    @PostMapping("/movies")
    public Movie addMovie(@Valid @RequestBody MovieReq req) { return movieService.create(req); }

    @PostMapping("/theatres")
    public Theatre addTheatre(@Valid @RequestBody TheatreReq r) {
        Theatre t = new Theatre();
        t.setName(r.name());
        t.setCity(r.city());
        t.setAddress(r.address());
        return theatreRepo.save(t);
    }

    @PostMapping("/shows")
    public ShowRes addShow(@Valid @RequestBody ShowReq req) { return showService.createShow(req); }
}
