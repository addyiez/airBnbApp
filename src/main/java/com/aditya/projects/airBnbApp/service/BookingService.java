package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.BookingRequest;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);
}
