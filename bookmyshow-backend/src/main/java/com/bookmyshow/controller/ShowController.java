package com.bookmyshow.controller;

import com.bookmyshow.dto.Dto.SeatRes;
import com.bookmyshow.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {
    private final ShowService showService;

    @GetMapping("/{id}/seats")
    public List<SeatRes> seats(@PathVariable Long id) { return showService.seats(id); }
}
