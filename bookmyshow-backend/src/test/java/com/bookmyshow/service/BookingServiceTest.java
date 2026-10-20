package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.BookingRes;
import com.bookmyshow.dto.Dto.LockReq;
import com.bookmyshow.entity.*;
import com.bookmyshow.exception.ApiException;
import com.bookmyshow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepo;
    @Mock ShowSeatRepository seatRepo;
    @Mock ShowRepository showRepo;
    @Mock UserRepository userRepo;
    @InjectMocks BookingService service;

    private User user;
    private Show show;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "lockMinutes", 5);
        user = new User();
        user.setEmail("a@test.com");
        user.setName("A");

        Movie movie = new Movie();
        movie.setTitle("Inception");
        Theatre theatre = new Theatre();
        theatre.setName("PVR");
        theatre.setCity("Nagpur");

        show = new Show();
        show.setId(1L);
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreenName("Audi 1");
        show.setPrice(BigDecimal.valueOf(200));
        show.setStartTime(LocalDateTime.now().plusDays(1));
    }

    // ---------- helpers ----------
    private void stubUserAndShow() {
        when(userRepo.findByEmail("a@test.com")).thenReturn(Optional.of(user));
        when(showRepo.findById(1L)).thenReturn(Optional.of(show));
    }

    private ShowSeat seat(String number, SeatStatus status, LocalDateTime lockedUntil) {
        ShowSeat s = new ShowSeat();
        s.setShow(show);
        s.setSeatNumber(number);
        s.setStatus(status);
        s.setLockedUntil(lockedUntil);
        return s;
    }

    private Booking booking(BookingStatus status, List<ShowSeat> seats) {
        Booking b = new Booking();
        b.setId(10L);
        b.setBookingRef("BMSTEST01");
        b.setUser(user);
        b.setShow(show);
        b.setStatus(status);
        b.setSeatNumbers("A1");
        b.setTotalAmount(BigDecimal.valueOf(200));
        b.setCreatedAt(LocalDateTime.now());
        b.setSeats(new ArrayList<>(seats));
        seats.forEach(s -> s.setBooking(b));
        return b;
    }

    // ---------- lockSeats ----------
    @Test
    void lockSeats_success_createsPendingBookingAndLocksSeats() {
        stubUserAndShow();
        List<ShowSeat> seats = List.of(seat("A1", SeatStatus.AVAILABLE, null), seat("A2", SeatStatus.AVAILABLE, null));
        when(seatRepo.findForUpdate(eq(1L), anyCollection())).thenReturn(seats);

        BookingRes res = service.lockSeats("a@test.com", new LockReq(1L, List.of("A1", "A2")));

        assertEquals("PENDING", res.status());
        assertEquals(0, BigDecimal.valueOf(400).compareTo(res.totalAmount()));
        assertEquals(List.of("A1", "A2"), res.seats());
        assertTrue(seats.stream().allMatch(s -> s.getStatus() == SeatStatus.LOCKED));
        verify(bookingRepo).save(any(Booking.class));
    }

    @Test
    void lockSeats_alreadyBookedSeat_throwsConflict() {
        stubUserAndShow();
        when(seatRepo.findForUpdate(eq(1L), anyCollection()))
                .thenReturn(List.of(seat("A1", SeatStatus.BOOKED, null)));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.lockSeats("a@test.com", new LockReq(1L, List.of("A1"))));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void lockSeats_expiredLockCanBeTakenAgain() {
        stubUserAndShow();
        ShowSeat expired = seat("A1", SeatStatus.LOCKED, LocalDateTime.now().minusMinutes(1));
        when(seatRepo.findForUpdate(eq(1L), anyCollection())).thenReturn(List.of(expired));

        BookingRes res = service.lockSeats("a@test.com", new LockReq(1L, List.of("A1")));

        assertEquals("PENDING", res.status());
        assertEquals(SeatStatus.LOCKED, expired.getStatus());
        assertTrue(expired.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void lockSeats_moreThanSixSeats_throwsBadRequest() {
        stubUserAndShow();
        List<String> seven = List.of("A1", "A2", "A3", "A4", "A5", "A6", "A7");

        ApiException ex = assertThrows(ApiException.class,
                () -> service.lockSeats("a@test.com", new LockReq(1L, seven)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(seatRepo, never()).findForUpdate(any(), any());
    }

    @Test
    void lockSeats_invalidSeatNumber_throwsBadRequest() {
        stubUserAndShow();
        when(seatRepo.findForUpdate(eq(1L), anyCollection()))
                .thenReturn(List.of(seat("A1", SeatStatus.AVAILABLE, null)));   // only 1 of 2 found

        ApiException ex = assertThrows(ApiException.class,
                () -> service.lockSeats("a@test.com", new LockReq(1L, List.of("A1", "Z99"))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void lockSeats_showAlreadyStarted_throwsBadRequest() {
        show.setStartTime(LocalDateTime.now().minusHours(1));
        stubUserAndShow();

        ApiException ex = assertThrows(ApiException.class,
                () -> service.lockSeats("a@test.com", new LockReq(1L, List.of("A1"))));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    // ---------- confirm (payment) ----------
    @Test
    void confirm_validHold_marksSeatsBookedAndBookingConfirmed() {
        ShowSeat s = seat("A1", SeatStatus.LOCKED, LocalDateTime.now().plusMinutes(3));
        Booking b = booking(BookingStatus.PENDING, List.of(s));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        BookingRes res = service.confirm("a@test.com", 10L);

        assertEquals("CONFIRMED", res.status());
        assertEquals(SeatStatus.BOOKED, s.getStatus());
        assertNull(s.getLockedUntil());
    }

    @Test
    void confirm_expiredHold_expiresBookingAndReleasesSeats() {
        ShowSeat s = seat("A1", SeatStatus.LOCKED, LocalDateTime.now().minusMinutes(1));
        Booking b = booking(BookingStatus.PENDING, List.of(s));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        ApiException ex = assertThrows(ApiException.class, () -> service.confirm("a@test.com", 10L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(BookingStatus.EXPIRED, b.getStatus());
        assertEquals(SeatStatus.AVAILABLE, s.getStatus());
    }

    @Test
    void confirm_someoneElsesBooking_throwsForbidden() {
        Booking b = booking(BookingStatus.PENDING, List.of(seat("A1", SeatStatus.LOCKED, LocalDateTime.now().plusMinutes(3))));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        ApiException ex = assertThrows(ApiException.class, () -> service.confirm("other@test.com", 10L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    // ---------- cancel ----------
    @Test
    void cancel_pendingBooking_releasesSeats() {
        ShowSeat s = seat("A1", SeatStatus.LOCKED, LocalDateTime.now().plusMinutes(3));
        Booking b = booking(BookingStatus.PENDING, List.of(s));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        BookingRes res = service.cancel("a@test.com", 10L);

        assertEquals("CANCELLED", res.status());
        assertEquals(SeatStatus.AVAILABLE, s.getStatus());
        assertNull(s.getBooking());
    }

    @Test
    void cancel_confirmedBookingWithinTwoHours_throwsBadRequest() {
        show.setStartTime(LocalDateTime.now().plusHours(1));
        ShowSeat s = seat("A1", SeatStatus.BOOKED, null);
        Booking b = booking(BookingStatus.CONFIRMED, List.of(s));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(b));

        ApiException ex = assertThrows(ApiException.class, () -> service.cancel("a@test.com", 10L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(SeatStatus.BOOKED, s.getStatus());   // seats untouched
    }
}