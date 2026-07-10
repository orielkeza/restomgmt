package com.restomgmt.site.menu.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.restomgmt.site.menu.models.Category;

@DataJpaTest
@ActiveProfiles("uat")
class CategoryRepositoryTest {
    
    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        category = Category.builder()
                            .name("Mains")
                            .build();

        categoryRepository.save(category);
    }

    @Test
    void findByNameShouldReturnCorrectCategoryWhenNameIsCorrect() {
        Optional<Category> result = categoryRepository.findByName("Mains");
        assertTrue(result.isPresent());
        assertFalse(result.isEmpty());
        assertEquals("Mains", result.get().getName());
    }

    @Test
    void findByNameShouldReturnNullWhenNameIsIncorrect() {
        Optional<Category> category = categoryRepository.findByName("falseCategory");
        
        assertFalse(category.isPresent());
        assertTrue(category.isEmpty());
    }

    @Test
    void existsByNameShouldReturnCorrectUserWhenNameExists() {
        boolean result = categoryRepository.existsByName("Mains");
        assertTrue(result);
        assertEquals(true, result);
    }

    @Test
    void existsByNameShouldReturnFalseWhenNameDoesNotExist() {
        boolean result = categoryRepository.existsByName("False");
        
        assertFalse(result);
    }
}
