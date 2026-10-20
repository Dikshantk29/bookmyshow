package com.bookmyshow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "show_seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seatNumber"}))
@Getter @Setter @NoArgsConstructor
public class ShowSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Show show;
    private String seatNumber;
    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;
    private LocalDateTime lockedUntil;
    @ManyToOne(fetch = FetchType.LAZY)
    private Booking booking;
    @Version
    private Long version;
}
