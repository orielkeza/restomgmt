package com.restomgmt.site.menu.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;

@DataJpaTest
@ActiveProfiles("uat")
class MenuItemRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private Category mainsCategory;
    private Category emptyCategory;
    private MenuItem availableFood;
    private MenuItem availableFood2;
    private MenuItem unavailableFood;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();
        categoryRepository.deleteAll();

        mainsCategory = Category.builder().name("Mains").build();
        categoryRepository.save(mainsCategory);

        emptyCategory = Category.builder().name("Drinks").build();
        categoryRepository.save(emptyCategory);

        availableFood = MenuItem.builder()
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(mainsCategory)
            .build();
        menuItemRepository.save(availableFood);

        availableFood2 = MenuItem.builder()
            .name("Igitoki")
            .description("Plantain cooked with vegetable sauce")
            .cost(new BigDecimal("7.00"))
            .available(true)
            .category(mainsCategory)
            .build();
        menuItemRepository.save(availableFood2);

        unavailableFood = MenuItem.builder()
            .name("Sold Out Burger")
            .description("Temporarily unavailable")
            .cost(new BigDecimal("9.99"))
            .available(false)
            .category(mainsCategory)
            .build();
        menuItemRepository.save(unavailableFood);
    }

    @Test
    void findByNameShouldReturnCorrectMenuItemWhenNameIsCorrect() {
        Optional<MenuItem> menuItem = menuItemRepository.findByName("Grilled Chicken");
        assertTrue(menuItem.isPresent());
        assertFalse(menuItem.isEmpty());
        assertEquals("Grilled Chicken", menuItem.get().getName());
    }


    @Test
    void findByNameShouldReturnNullWhenNameIsIncorrect() {
        Optional<MenuItem> menuItem = menuItemRepository.findByName("FalseItem");        
        assertFalse(menuItem.isPresent());
        assertTrue(menuItem.isEmpty());
    }

    @Test
    void findByCategoryIdShouldReturnListOfMenuItemsInThatCategoryWhenIdIsCorrect() {
        List<MenuItem> items = menuItemRepository.findByCategory_Id(mainsCategory.getId());
        boolean result = categoryRepository.existsByName("Mains");
        assertTrue(result);
        assertEquals(3, items.size());
        assertEquals("Grilled Chicken", items.get(0).getName());
        assertEquals("Igitoki", items.get(1).getName());
        assertEquals("Sold Out Burger", items.get(2).getName());
    }

    @Test
    void findByCategoryIdShouldReturnEmptyListOfMenuItemsInThatCategoryWhenIdIsIncorrect() {
        List<MenuItem> items = menuItemRepository.findByCategory_Id(mainsCategory.getId());
        boolean result = categoryRepository.existsByName("Mains");
        assertTrue(result);
        assertEquals(3, items.size());
        assertEquals("Grilled Chicken", items.get(0).getName());
        assertEquals("Igitoki", items.get(1).getName());
        assertEquals("Sold Out Burger", items.get(2).getName());
    }
   @Test
    void findByAvailableTrueShouldReturnOnlyAvailableItems() {
        List<MenuItem> result = menuItemRepository.findByAvailableTrue();

        assertEquals(2, result.size());
        assertEquals("Grilled Chicken", result.get(0).getName());
        assertEquals("Igitoki", result.get(1).getName());
    }

    @Test
    void findByCategory_IdShouldReturnEmptyWhenCategoryHasNoItems() {
        List<MenuItem> result = menuItemRepository.findByCategory_Id(emptyCategory.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void saveShouldPopulateTimestamps() {
        assertNotNull(availableFood.getCreatedAt());
        assertNotNull(availableFood.getUpdatedAt());
    }
}
