package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.BookingRequest;
import com.aditya.projects.airBnbApp.dto.GuestDto;
import com.aditya.projects.airBnbApp.dto.HotelReportDto;
import com.aditya.projects.airBnbApp.entity.*;
import com.aditya.projects.airBnbApp.entity.enums.BookingStatus;
import com.aditya.projects.airBnbApp.exception.ResourceNotFoundException;
import com.aditya.projects.airBnbApp.exception.UnAuthorizedException;
import com.aditya.projects.airBnbApp.repository.*;
import com.aditya.projects.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.aditya.projects.airBnbApp.util.AppUtils.getCurrentUser;
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
    private final PricingService pricingService;

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

        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                //.paymentStatus(PaymentStatus.PENDING)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
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
        if (!"checkout.session.completed".equals(event.getType())) {
            log.warn("Unhandled event type: {}", event.getType());
            return;
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        if (deserializer.getObject().isPresent()) {
            stripeObject = deserializer.getObject().get();
        } else {
            try {
                stripeObject = deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Could not deserialize Stripe session for event: {}", event.getId(), e);
                throw new IllegalStateException("Stripe deserialization failed", e);
            }
        }

        Session session = (Session) stripeObject;
        String sessionId = session.getId();

        Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found for session ID: " + sessionId));

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        int updated = inventoryRepository.confirmBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        long days = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());
        if (updated != days) {
            throw new IllegalStateException("Inventory confirm updated " + updated
                    + " rows, expected " + days + " for booking " + booking.getId());
        }

        log.info("Booking {} confirmed and inventory updated", booking.getId());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking Not found with id: " +bookingId));
        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorizedException(
                    "Booking does not belong to the user with id: " + user.getId()
            );
        }
        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Booking is not confirmed, cannot cancel");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        //handle the refund
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking Not found with id: " +bookingId));
        User user = getCurrentUser();

        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorizedException(
                    "Booking does not belong to the user with id: " + user.getId()
            );
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID:" +hotelId));
        User user = getCurrentUser();

        log.info("Getting all bookings for the hotel with ID: {}",hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with ID:"+hotelId);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID:" +hotelId));
        User user = getCurrentUser();

        log.info("Generating Report for hotel with ID: {}",hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with ID:"+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());

    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

}
