package com.restomgmt.site.menu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restomgmt.site.menu.dto.CategoryRequest;
import com.restomgmt.site.menu.dto.CategoryResponse;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.repositories.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .name("Mains")
            .build();
    }

    @Test
    void getAllCategoriesShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Mains", result.get(0).getName());
    }

    @Test
    void getAllCategoriesShouldReturnEmptyListWhenNoneExist() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCategoryByIdShouldReturnCategoryWhenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertEquals("Mains", result.getName());
    }

    @Test
    void getCategoryByIdShouldThrowWhenNotExists() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void createCategoryShouldSaveAndReturnResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Drinks");

        when(categoryRepository.existsByName("Drinks")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(
            Category.builder().name("Drinks").build()
        );

        CategoryResponse result = categoryService.createCategory(request);

        assertEquals("Drinks", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategoryShouldThrowWhenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Mains");

        when(categoryRepository.existsByName("Mains")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategoryShouldReturnUpdatedResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Mains");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategoryShouldThrowWhenNotExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Ghost");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoryService.updateCategory(99L, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategoryShouldDeleteWhenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategoryShouldThrowWhenNotExists() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoryService.deleteCategory(99L));
        verify(categoryRepository, never()).delete(any());
    }
}
