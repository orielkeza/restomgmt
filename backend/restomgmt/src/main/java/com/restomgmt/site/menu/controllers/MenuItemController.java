package com.restomgmt.site.menu.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restomgmt.site.menu.dto.MenuItemRequest;
import com.restomgmt.site.menu.dto.MenuItemResponse;
import com.restomgmt.site.menu.services.MenuItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/menu/items")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping("")
    public ResponseEntity<List<MenuItemResponse>> getAllItems() {
        return ResponseEntity.ok(menuItemService.getAvailableItems());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MGMT')")
    public ResponseEntity<List<MenuItemResponse>> getAllItemsIncludingUnavailable() {
        return ResponseEntity.ok(menuItemService.getAllItems());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MenuItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuItemService.getItemsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(menuItemService.getItemById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MGMT')")
    public ResponseEntity<MenuItemResponse> createItem(@Valid @RequestBody MenuItemRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.createItem(request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MGMT')")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        try {
            return ResponseEntity.ok(menuItemService.updateItem(id, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            menuItemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
