package com.restomgmt.site.user.repositories;

import com.restomgmt.site.user.models.RoleNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.List;
//import java.util.Optional;

@Repository
public interface RoleNewRepository extends JpaRepository <RoleNew, Long> {
    RoleNew findByName(String name);
}