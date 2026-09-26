package com.aditya.projects.airBnbApp.dto;

import com.aditya.projects.airBnbApp.entity.Hotel;
import com.aditya.projects.airBnbApp.entity.Room;
import com.aditya.projects.airBnbApp.entity.User;
import com.aditya.projects.airBnbApp.entity.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Hotel hotel;
    private Room room;
    private User user;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus status;
    private Set<GuestDto> guests;

}
