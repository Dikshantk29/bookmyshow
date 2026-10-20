package com.bookmyshow.controller;

import com.bookmyshow.dto.Dto.*;
import com.bookmyshow.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/lock")
    public BookingRes lock(Authentication auth, @Valid @RequestBody LockReq req) {
        return bookingService.lockSeats(auth.getName(), req);
    }

    @PostMapping("/{id}/pay")
    public BookingRes pay(Authentication auth, @PathVariable Long id, @RequestBody(required = false) PayReq req) {
        return bookingService.confirm(auth.getName(), id);   // mock payment gateway
    }

    @PostMapping("/{id}/cancel")
    public BookingRes cancel(Authentication auth, @PathVariable Long id) {
        return bookingService.cancel(auth.getName(), id);
    }

    @GetMapping("/my")
    public List<BookingRes> my(Authentication auth) { return bookingService.myBookings(auth.getName()); }

    @GetMapping("/{id}")
    public BookingRes get(Authentication auth, @PathVariable Long id) { return bookingService.get(auth.getName(), id); }
}
