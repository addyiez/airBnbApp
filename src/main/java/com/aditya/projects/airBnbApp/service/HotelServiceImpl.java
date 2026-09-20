package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.HotelDto;
import com.aditya.projects.airBnbApp.entity.Hotel;
import com.aditya.projects.airBnbApp.exception.ResourceNotFoundException;
import com.aditya.projects.airBnbApp.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j //is used to log the information in the console
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class); // Mapping HotelDto to Hotel entity
        hotel.setActive(false);
        hotel = hotelRepository.save(hotel); // Saving the hotel entity to the database
        log.info("Hotel created with id: {}", hotelDto.getId());
        return modelMapper.map(hotel, HotelDto.class); // Mapping the saved Hotel entity back to HotelDto
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Fetching hotel with id: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        // Update the existing hotel entity with the new values from hotelDto
        modelMapper.map(hotelDto, hotel); // Mapping the values from HotelDto to the existing Hotel entity
        hotel.setId(id); // Ensure the ID remains the same for the update operation
        hotel = hotelRepository.save(hotel); // Saving the updated hotel entity to the database
        return modelMapper.map(hotel, HotelDto.class); // Mapping the updated Hotel entity back

    }

    @Override
    public void deleteHotelById(Long id) {
        log.info("Deleting hotel with id: {}", id);
        boolean exists = hotelRepository.existsById(id);
        if (!exists) {
            throw new ResourceNotFoundException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id); // Deleting the hotel entity from the database
        //TODO: delete the future inventories for this hotel


    }
}