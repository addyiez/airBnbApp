package com.aditya.projects.airBnbApp.repository;

import com.aditya.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
