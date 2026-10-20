package com.bookmyshow.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Dto {
    public record RegisterReq(@NotBlank String name, @Email @NotBlank String email,
                              @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}
    public record LoginReq(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthRes(String token, String name, String email, String role) {}

    public record MovieReq(@NotBlank String title, String description, String genre, String language,
                           @NotNull Integer durationMins, Double rating, String posterUrl, LocalDate releaseDate) {}
    public record TheatreReq(@NotBlank String name, @NotBlank String city, String address) {}
    public record ShowReq(@NotNull Long movieId, @NotNull Long theatreId, @NotBlank String screenName,
                          @NotNull LocalDateTime startTime, @NotNull @Positive BigDecimal price) {}

    public record ShowRes(Long id, Long movieId, String movieTitle, Long theatreId, String theatreName,
                          String city, String screenName, LocalDateTime startTime, BigDecimal price,
                          long availableSeats) {}
    public record SeatRes(String seatNumber, String status) {}

    public record LockReq(@NotNull Long showId, @NotEmpty List<String> seatNumbers) {}
    public record PayReq(String paymentMethod) {}
    public record BookingRes(Long id, String bookingRef, String status, Long showId, String movieTitle,
                             String theatreName, String city, String screenName, LocalDateTime startTime,
                             List<String> seats, BigDecimal totalAmount, LocalDateTime createdAt,
                             LocalDateTime expiresAt) {}
}
