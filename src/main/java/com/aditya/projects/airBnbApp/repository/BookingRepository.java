package com.aditya.projects.airBnbApp.repository;

import com.aditya.projects.airBnbApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
