package com.aditya.projects.airBnbApp.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class HotelContactInfo {

    private String phoneNumber;

    private String address;
}
