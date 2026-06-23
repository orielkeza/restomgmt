package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.User;

import org.springframework.data.jpa.repository.JpaRepository;

//import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}