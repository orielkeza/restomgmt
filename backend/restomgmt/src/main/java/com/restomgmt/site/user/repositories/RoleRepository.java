package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.util.List;
//import java.util.Optional;

public interface RoleRepository extends JpaRepository <Role, Long> {
    Role findByName(String name);
}