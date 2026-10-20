package com.bookmyshow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "theatres")
@Getter @Setter @NoArgsConstructor
public class Theatre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String city;
    private String address;
}
