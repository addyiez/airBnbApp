package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.BookingRequest;
import com.aditya.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
