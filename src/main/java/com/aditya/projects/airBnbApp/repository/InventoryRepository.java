package com.aditya.projects.airBnbApp.repository;

import com.aditya.projects.airBnbApp.entity.Inventory;
import com.aditya.projects.airBnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);
}
