package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aditya.projects.airBnbApp.dto.UserDto;
import com.aditya.projects.airBnbApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
