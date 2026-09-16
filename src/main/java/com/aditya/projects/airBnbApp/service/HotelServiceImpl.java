package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.HotelDto;
import com.aditya.projects.airBnbApp.entity.Hotel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j //is used to log the information in the console
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    @Override
    public Hotel createNewHotel(HotelDto hotelDto) {
        return null;
    }

    @Override
    public Hotel getHotelById(Long id) {
        return null;
    }
}
