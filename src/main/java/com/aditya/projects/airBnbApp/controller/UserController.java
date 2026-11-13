package com.aditya.projects.airBnbApp.controller;

import com.aditya.projects.airBnbApp.dto.BookingDto;
import com.aditya.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aditya.projects.airBnbApp.service.BookingService;
import com.aditya.projects.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }
}
