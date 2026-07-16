package com.restomgmt.site.menu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restomgmt.site.menu.dto.MenuItemRequest;
import com.restomgmt.site.menu.dto.MenuItemResponse;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.menu.repositories.CategoryRepository;
import com.restomgmt.site.menu.repositories.MenuItemRepository;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {
    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    private Category category;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .name("Mains")
            .build();

        menuItem = MenuItem.builder()
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(category)
            .build();
    }

    @Test
    void getAllItemsShouldReturnList() {
        when(menuItemRepository.findAll()).thenReturn(List.of(menuItem));

        List<MenuItemResponse> result = menuItemService.getAllItems();

        assertEquals(1, result.size());
        assertEquals("Grilled Chicken", result.get(0).getName());
    }

    @Test
    void getAvailableItemsShouldReturnOnlyAvailableItems() {
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of(menuItem));

        List<MenuItemResponse> result = menuItemService.getAvailableItems();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    void getAvailableItemsShouldReturnEmptyWhenNoneAvailable() {
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of());

        List<MenuItemResponse> result = menuItemService.getAvailableItems();

        assertTrue(result.isEmpty());
    }

    @Test
    void getItemsByCategoryShouldReturnItemsInCategory() {
        when(menuItemRepository.findByCategory_Id(1L)).thenReturn(List.of(menuItem));

        List<MenuItemResponse> result = menuItemService.getItemsByCategory(1L);

        assertEquals(1, result.size());
        assertEquals("Mains", result.get(0).getCategoryName());
    }

    @Test
    void getItemByIdShouldReturnItemWhenExists() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        MenuItemResponse result = menuItemService.getItemById(1L);

        assertEquals("Grilled Chicken", result.getName());
        assertEquals(new BigDecimal("12.99"), result.getCost());
    }

    @Test
    void getItemByIdShouldThrowWhenNotExists() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> menuItemService.getItemById(99L));
    }

    @Test
    void createItemShouldSaveAndReturnResponse() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Grilled Chicken");
        request.setDescription("Herb-marinated grilled chicken");
        request.setCost(new BigDecimal("12.99"));
        request.setCategoryId(1L);
        request.setAvailable(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponse result = menuItemService.createItem(request);

        assertEquals("Grilled Chicken", result.getName());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void createItemShouldThrowWhenCategoryNotFound() {
        MenuItemRequest request = new MenuItemRequest();
        request.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> menuItemService.createItem(request));
        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void updateItemShouldReturnUpdatedResponse() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Updated Chicken");
        request.setDescription("New description");
        request.setCost(new BigDecimal("14.99"));
        request.setCategoryId(1L);
        request.setAvailable(true);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponse result = menuItemService.updateItem(1L, request);

        assertNotNull(result);
        verify(menuItemRepository).save(menuItem);
    }

    @Test
    void updateItemShouldThrowWhenItemNotFound() {
        MenuItemRequest request = new MenuItemRequest();
        request.setCategoryId(1L);

        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> menuItemService.updateItem(99L, request));
        verify(menuItemRepository, never()).save(any());
    }

    @Test
    void deleteItemShouldDeleteWhenExists() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        menuItemService.deleteItem(1L);

        verify(menuItemRepository).delete(menuItem);
    }

    @Test
    void deleteItemShouldThrowWhenNotExists() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> menuItemService.deleteItem(99L));
        verify(menuItemRepository, never()).delete(any());
    }
}