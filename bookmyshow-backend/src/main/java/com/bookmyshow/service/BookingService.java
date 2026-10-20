package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.BookingRes;
import com.bookmyshow.dto.Dto.LockReq;
import com.bookmyshow.entity.*;
import com.bookmyshow.exception.ApiException;
import com.bookmyshow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final int MAX_SEATS = 6;

    private final BookingRepository bookingRepo;
    private final ShowSeatRepository seatRepo;
    private final ShowRepository showRepo;
    private final UserRepository userRepo;

    @Value("${app.seat-lock-minutes}")
    private int lockMinutes;

    /** Step 1: hold the seats for a few minutes and create a PENDING booking. */
    @Transactional
    public BookingRes lockSeats(String email, LockReq req) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        Show show = showRepo.findById(req.showId()).orElseThrow(() -> ApiException.notFound("Show not found"));
        if (show.getStartTime().isBefore(LocalDateTime.now()))
            throw ApiException.badRequest("This show has already started");

        Set<String> wanted = new TreeSet<>(req.seatNumbers());
        if (wanted.size() > MAX_SEATS) throw ApiException.badRequest("You can book at most " + MAX_SEATS + " seats");

        List<ShowSeat> seats = seatRepo.findForUpdate(show.getId(), wanted);   // pessimistic lock
        if (seats.size() != wanted.size()) throw ApiException.badRequest("One or more seats are invalid");

        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat s : seats) {
            boolean free = s.getStatus() == SeatStatus.AVAILABLE
                    || (s.getStatus() == SeatStatus.LOCKED && s.getLockedUntil() != null && s.getLockedUntil().isBefore(now));
            if (!free) throw ApiException.conflict("Seat " + s.getSeatNumber() + " is not available");
        }

        Booking b = new Booking();
        b.setUser(user);
        b.setShow(show);
        b.setBookingRef("BMS" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setSeatNumbers(String.join(",", wanted));
        b.setTotalAmount(show.getPrice().multiply(BigDecimal.valueOf(wanted.size())));
        b.setStatus(BookingStatus.PENDING);
        b.setCreatedAt(now);
        bookingRepo.save(b);

        for (ShowSeat s : seats) {
            s.setStatus(SeatStatus.LOCKED);
            s.setLockedUntil(now.plusMinutes(lockMinutes));
            s.setBooking(b);
        }
        b.setSeats(new ArrayList<>(seats));
        return toRes(b);
    }

    /** Step 2: (mock) payment succeeded -> confirm the booking. */
    @Transactional
    public BookingRes confirm(String email, Long bookingId) {
        Booking b = owned(email, bookingId);
        if (b.getStatus() != BookingStatus.PENDING)
            throw ApiException.badRequest("Booking is already " + b.getStatus());

        LocalDateTime now = LocalDateTime.now();
        List<ShowSeat> seats = new ArrayList<>(b.getSeats());
        boolean valid = !seats.isEmpty() && seats.stream().allMatch(s ->
                s.getStatus() == SeatStatus.LOCKED && s.getLockedUntil() != null && s.getLockedUntil().isAfter(now));
        if (!valid) {
            release(seats);
            b.setStatus(BookingStatus.EXPIRED);
            throw ApiException.badRequest("Seat hold expired. Please select seats again.");
        }
        for (ShowSeat s : seats) {
            s.setStatus(SeatStatus.BOOKED);
            s.setLockedUntil(null);
        }
        b.setStatus(BookingStatus.CONFIRMED);
        return toRes(b);
    }

    @Transactional
    public BookingRes cancel(String email, Long bookingId) {
        Booking b = owned(email, bookingId);
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.EXPIRED)
            throw ApiException.badRequest("Booking is already " + b.getStatus());
        if (b.getStatus() == BookingStatus.CONFIRMED && b.getShow().getStartTime().isBefore(LocalDateTime.now().plusHours(2)))
            throw ApiException.badRequest("Cannot cancel within 2 hours of the show");
        release(new ArrayList<>(b.getSeats()));
        b.setStatus(BookingStatus.CANCELLED);
        return toRes(b);
    }

    @Transactional(readOnly = true)
    public List<BookingRes> myBookings(String email) {
        return bookingRepo.findByUserEmailOrderByCreatedAtDesc(email).stream().map(this::toRes).toList();
    }

    @Transactional(readOnly = true)
    public BookingRes get(String email, Long id) { return toRes(owned(email, id)); }

    /** Background job: free seats whose hold has expired. */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredLocks() {
        List<ShowSeat> expired = seatRepo.findByStatusAndLockedUntilBefore(SeatStatus.LOCKED, LocalDateTime.now());
        for (ShowSeat s : expired) {
            Booking b = s.getBooking();
            if (b != null && b.getStatus() == BookingStatus.PENDING) b.setStatus(BookingStatus.EXPIRED);
        }
        release(expired);
    }

    private void release(Collection<ShowSeat> seats) {
        for (ShowSeat s : seats) {
            s.setStatus(SeatStatus.AVAILABLE);
            s.setLockedUntil(null);
            s.setBooking(null);
        }
    }

    private Booking owned(String email, Long id) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!b.getUser().getEmail().equals(email)) throw ApiException.forbidden("Not your booking");
        return b;
    }
    private BookingRes toRes(Booking b) {
        Show s = b.getShow();
        OffsetDateTime expiresAt = b.getStatus() == BookingStatus.PENDING
                ? b.getCreatedAt().plusMinutes(lockMinutes).atZone(ZoneId.systemDefault()).toOffsetDateTime()
                : null;
        return new BookingRes(b.getId(), b.getBookingRef(), b.getStatus().name(), s.getId(),
                s.getMovie().getTitle(), s.getTheatre().getName(), s.getTheatre().getCity(),
                s.getScreenName(), s.getStartTime(), Arrays.asList(b.getSeatNumbers().split(",")),
                b.getTotalAmount(), b.getCreatedAt(), expiresAt);
    }
}
