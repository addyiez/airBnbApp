package com.aditya.projects.airBnbApp.repository;

import com.aditya.projects.airBnbApp.entity.Hotel;
import com.aditya.projects.airBnbApp.entity.Inventory;
import com.aditya.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;


public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);


    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Integer dateCount
    );
}
