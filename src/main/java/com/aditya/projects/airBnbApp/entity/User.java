package com.aditya.projects.airBnbApp.entity;

import com.aditya.projects.airBnbApp.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER) //creates a separate table to store the roles for each user
    @Enumerated(EnumType.STRING)
    private Set<Role> roles; //List of roles assigned to the user (e.g., ROLE_USER, ROLE_ADMIN)

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; //encoded password for security
}
