package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.RoomDto;
import com.aditya.projects.airBnbApp.entity.Hotel;
import com.aditya.projects.airBnbApp.entity.Room;
import com.aditya.projects.airBnbApp.exception.ResourceNotFoundException;
import com.aditya.projects.airBnbApp.repository.HotelRepository;
import com.aditya.projects.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("Creating new room in hotel with Id: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"+hotelId));
        Room room = modelMapper.map(roomDto, Room.class); // Mapping RoomDto to Room entity
        room.setHotel(hotel);
        room = roomRepository.save(room); // Saving the room entity to the database

        //TODO: create inventory as soon as room is created and if hotel is active

        return modelMapper.map(room, RoomDto.class); // Mapping the saved Room entity back to RoomDto
    }

    @Override
    public List<RoomDto> getAllRoomInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Fetching room with id: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with id: {}", roomId);
        boolean exists = roomRepository.existsById(roomId);
        if (!exists) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }
        roomRepository.deleteById(roomId);
    }

    //TODO: delete the future inventories for this room when room is deleted
}
