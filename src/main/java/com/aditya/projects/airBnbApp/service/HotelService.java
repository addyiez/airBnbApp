package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.HotelDto;
import com.aditya.projects.airBnbApp.entity.Hotel;

public interface HotelService {
    Hotel createNewHotel(HotelDto hotelDto);

    Hotel getHotelById(Long id);
}
