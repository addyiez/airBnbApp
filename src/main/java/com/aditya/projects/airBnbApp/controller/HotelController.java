package com.aditya.projects.airBnbApp.controller;

import com.aditya.projects.airBnbApp.dto.HotelDto;
import com.aditya.projects.airBnbApp.repository.HotelRepository;
import com.aditya.projects.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
        log.info("Received request to create new hotel: {}", hotelDto);
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<HotelDto> getHotelById(@RequestParam Long id) {
        log.info("Received request to fetch hotel with id: {}", id);
        HotelDto hotel = hotelService.getHotelById(id);
        return new ResponseEntity<>(hotel, HttpStatus.OK);
    }
}
