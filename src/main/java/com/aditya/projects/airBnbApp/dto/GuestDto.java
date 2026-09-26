package com.aditya.projects.airBnbApp.dto;

import com.aditya.projects.airBnbApp.entity.User;
import com.aditya.projects.airBnbApp.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
