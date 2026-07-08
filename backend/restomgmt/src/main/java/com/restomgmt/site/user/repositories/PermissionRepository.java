package com.restomgmt.site.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.restomgmt.site.user.permission.Permission;

//import java.util.List;
//import java.util.Optional;

public interface PermissionRepository extends JpaRepository <Permission, Long> {
    Permission findByName(String name);
}