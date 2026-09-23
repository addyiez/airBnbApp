package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.HotelDto;
import com.aditya.projects.airBnbApp.dto.HotelSearchRequest;
import com.aditya.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
