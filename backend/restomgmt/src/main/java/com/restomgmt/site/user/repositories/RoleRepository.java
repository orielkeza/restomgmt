package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository <Role, Long> {
    Optional<Role> findByName(String name);
}