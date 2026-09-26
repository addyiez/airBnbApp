package com.aditya.projects.airBnbApp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(
        name = "unique_hotel_room_date",
        columnNames = {"hotel_id", "room_id", "date"}
))
@Builder
@AllArgsConstructor //JPA requires a constructor with all arguments for entity classes
@NoArgsConstructor //JPA requires a no-argument constructor for entity classes
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel; //Foreign key to Hotel entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; //Foreign key to Room entity

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer bookedCount; //Number of rooms booked for this date

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer reservedCount;

    @Column(nullable = false)
    private Integer totalCount; //Total number of rooms available for this date

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal surgeFactor; //Indicates whether surge pricing is applied for this date (true) or not (false)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; //Price of the room for this date = basePrice * surgeFactor

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Boolean closed; //Indicates whether the hotel is closed for this date (true) or not (false)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
