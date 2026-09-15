package com.aditya.projects.airBnbApp.entity;

import com.aditya.projects.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //Reference to the User entity

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Integer age;

    /*
    @ManyToMany(mappedBy = "guests", fetch = FetchType.LAZY)
    //no need to specify joinTable because manyToMany relationship is already specified in Booking entity otherwise it creates a new table
    //only the owning side of the relationship should specify the joinTable, in this case Booking entity is the owning side
    private Set<Booking> bookings; //List of bookings made by the guest
     */
}

