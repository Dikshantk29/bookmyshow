package com.bookmyshow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "bookings")
@Getter @Setter @NoArgsConstructor
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bookingRef;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Show show;
    @OneToMany(mappedBy = "booking")
    private List<ShowSeat> seats = new ArrayList<>();
    /** Stored as text so history survives after seats are released, e.g. "A1,A2" */
    private String seatNumbers;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();
}
