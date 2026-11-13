package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.aditya.projects.airBnbApp.entity.User;
import com.aditya.projects.airBnbApp.exception.ResourceNotFoundException;
import com.aditya.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.aditya.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: "+id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();
        // Update user profile with the provided details

        if(profileUpdateRequestDto.getName() != null) {
            user.setName(profileUpdateRequestDto.getName());
        }
        if(profileUpdateRequestDto.getDateOfBirth() != null) {
            user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        }
        if(profileUpdateRequestDto.getGender() != null) {
            user.setGender(profileUpdateRequestDto.getGender());
        }
        
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
