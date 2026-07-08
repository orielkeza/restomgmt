package com.restomgmt.site.menu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restomgmt.site.menu.models.MenuItem;

public interface MenuItemRepository extends JpaRepository <MenuItem, Long> {
    List<MenuItem> findByCategory_Id(Long categoryId);
    List<MenuItem> findByAvailableTrue();
    Optional<MenuItem> findByName(String name);
}
