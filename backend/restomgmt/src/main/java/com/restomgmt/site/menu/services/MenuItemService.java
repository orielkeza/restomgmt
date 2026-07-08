package com.restomgmt.site.menu.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restomgmt.site.menu.dto.MenuItemRequest;
import com.restomgmt.site.menu.dto.MenuItemResponse;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.menu.repositories.CategoryRepository;
import com.restomgmt.site.menu.repositories.MenuItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuItemService {
   private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    public List<MenuItemResponse> getAllItems() {
        return menuItemRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableItems() {
        return menuItemRepository.findByAvailableTrue()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategory_Id(categoryId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public MenuItemResponse getItemById(Long id) {
        return menuItemRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new NoSuchElementException("Menu item not found"));
    }

    @Transactional
    public MenuItemResponse createItem(MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new NoSuchElementException("Category not found"));

        MenuItem item = MenuItem.builder()
            .name(request.getName())
            .description(request.getDescription())
            .cost(request.getCost())
            .available(request.isAvailable())
            .category(category)
            .build();

        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateItem(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Menu item not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new NoSuchElementException("Category not found"));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCost(request.getCost());
        item.setAvailable(request.isAvailable());
        item.setCategory(category);

        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Menu item not found"));
        menuItemRepository.delete(item);
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .cost(item.getCost())
            .available(item.isAvailable())
            .categoryName(item.getCategory().getName())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    } 
}
