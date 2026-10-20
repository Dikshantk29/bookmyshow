package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.MovieReq;
import com.bookmyshow.entity.Movie;
import com.bookmyshow.exception.ApiException;
import com.bookmyshow.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepo;

    public List<Movie> list(String q) {
        return (q == null || q.isBlank()) ? movieRepo.findAll() : movieRepo.findByTitleContainingIgnoreCase(q.trim());
    }

    public Movie get(Long id) {
        return movieRepo.findById(id).orElseThrow(() -> ApiException.notFound("Movie not found"));
    }

    public Movie create(MovieReq r) {
        Movie m = new Movie();
        m.setTitle(r.title());
        m.setDescription(r.description());
        m.setGenre(r.genre());
        m.setLanguage(r.language());
        m.setDurationMins(r.durationMins());
        m.setRating(r.rating());
        m.setPosterUrl(r.posterUrl());
        m.setReleaseDate(r.releaseDate());
        return movieRepo.save(m);
    }
}
