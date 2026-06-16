package com.restomgmt.site.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.restomgmt.site.user.permission.Permission;

//import java.util.List;
//import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository <Permission, Long> {
    Permission findByName(String name);
}