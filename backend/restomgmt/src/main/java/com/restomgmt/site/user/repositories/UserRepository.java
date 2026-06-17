package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.User;

import jakarta.validation.constraints.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByUsername(String username);

    User findByEmail(@Email String email);
}