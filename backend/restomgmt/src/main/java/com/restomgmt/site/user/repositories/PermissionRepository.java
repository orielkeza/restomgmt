package com.restomgmt.site.user.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.restomgmt.site.user.models.Permission;

public interface PermissionRepository extends JpaRepository <Permission, Long> {
    Optional<Permission> findByName(String name);
}