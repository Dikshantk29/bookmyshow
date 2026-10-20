package com.bookmyshow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "shows")
@Getter @Setter @NoArgsConstructor
public class Show {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Movie movie;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Theatre theatre;
    private String screenName;
    private LocalDateTime startTime;
    private BigDecimal price;
}
