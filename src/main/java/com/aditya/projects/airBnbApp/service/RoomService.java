package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.RoomDto;

import java.util.List;

public interface RoomService {

    RoomDto createNewRoom(Long hotelId,RoomDto roomDto);

    List<RoomDto> getAllRoomInHotel(Long hotelId);

    RoomDto getRoomById(Long id);

    void deleteRoomById(Long id);
}
