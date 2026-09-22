package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);
}
