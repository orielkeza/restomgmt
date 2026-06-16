package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.UserNew;

import jakarta.validation.constraints.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.List;
import java.util.Optional;

@Repository
public interface UserNewRepository extends JpaRepository <UserNew, Long> {
    Optional<UserNew> findByUsername(String username);

    UserNew findByEmail(@Email String email);
}