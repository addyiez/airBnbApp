package com.aditya.projects.airBnbApp.repository;

import com.aditya.projects.airBnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}