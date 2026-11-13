package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.BookingRequest;
import com.aditya.projects.airBnbApp.dto.GuestDto;
import com.aditya.projects.airBnbApp.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
