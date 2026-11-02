package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.BookingRequest;
import com.aditya.projects.airBnbApp.dto.GuestDto;
import com.aditya.projects.airBnbApp.entity.*;
import com.aditya.projects.airBnbApp.entity.enums.BookingStatus;
import com.aditya.projects.airBnbApp.entity.enums.PaymentStatus;
import com.aditya.projects.airBnbApp.exception.ResourceNotFoundException;
import com.aditya.projects.airBnbApp.exception.UnAuthorizedException;
import com.aditya.projects.airBnbApp.repository.*;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;

    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional //is use for if something happen we can roll back
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Initialize booking for Hotel: {}, room: {}, date {}-{}", bookingRequest.getHotelId(),
                bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository
                .findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not found with id: " +bookingRequest.getHotelId()));

        Room room = roomRepository
                .findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room was not found with id: " +bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())+1;

        if(inventoryList.size() != daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/ update the booked count of inventories

        for(Inventory inventory : inventoryList){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //create the booking

        //TODO: calculate dynamic amount

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .paymentStatus(PaymentStatus.PENDING)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(new BigDecimal("500.00"))
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

        log.info("adding guest for booking with id: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                        new ResourceNotFoundException("Booking Not found with id: " +bookingId));
        User user = getCurrentUser();

        log.info("Authenticated User ID: {}", user.getId());
        log.info("Booking ID: {}", booking.getId());
        log.info("Booking Owner ID: {}", booking.getUser().getId());
        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorizedException(
                    "Booking does not belong to the user with id: " + user.getId()
            );
        }

        log.info("Booking Status: {}", booking.getBookingStatus());
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking has been reserved, cannot add guests");
        }

        for(GuestDto guestDto : guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking Not found with id: " +bookingId));
        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorizedException(
                    "Booking does not belong to the user with id: " + user.getId()
            );
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found for session ID: " + sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Payment captured and booking confirmed for booking ID: {}", booking.getId());

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                        booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),
                        booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed booking and updated inventory for booking ID: {}", booking.getId());
        } else {
                log.warn("Unhandled event type: {}", event.getType());
            }

    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
