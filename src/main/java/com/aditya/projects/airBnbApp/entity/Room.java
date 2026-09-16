package com.aditya.projects.airBnbApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String type; //RoomType (e.g., Single, Double, Suite)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice; //10.11 - precision - total number of digits, scale - number of digits to the right of the decimal point

    @Column(columnDefinition = "TEXT[]")
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")
    private String[] amenities;

    @Column(nullable = false)
    private Integer totalCount; //Total number of rooms of this type in the hotel

    @Column(nullable = false)
    private Integer capacity; //Maximum number of guests that can be accommodated in this room type

    @CreationTimestamp
    @Column(updatable = false) //createdAt should not be updated after creation
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
