package com.restomgmt.site.menu.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;

@DataJpaTest
@ActiveProfiles("uat")
class MenuItemRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private Category category;
    private MenuItem availableItem;
    private MenuItem unavailableItem;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();
        categoryRepository.deleteAll();

        Category mainsCategory = Category.builder().name("Mains").build();
        categoryRepository.save(mainsCategory);

        Category drinksCategory = Category.builder().name("Drinks").build();
        categoryRepository.save(drinksCategory);

        Category combosCategory = Category.builder().name("Combos").build();
        categoryRepository.save(combosCategory);

        // 1. Available Food
        MenuItem availableFood = MenuItem.builder()
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .category(mainsCategory)
            .build();
        //ReflectionTestUtils.setField(availableFood, "id", 1L);
                
        //assertEquals(1L, availableFood.getId());

        menuItemRepository.save(availableFood);

        // 2. Unavailable Food
        MenuItem unavailableFood = MenuItem.builder()
            .name("Sold Out Burger")
            .description("Temporarily unavailable")
            .cost(new BigDecimal("9.99"))
            .available(false)
            .category(mainsCategory)
            .build();
        menuItemRepository.save(unavailableFood);

        // 3. Available Drink
        MenuItem availableDrink = MenuItem.builder()
            .name("Iced Vanilla Latte")
            .description("Cold brewed espresso with milk and vanilla syrup")
            .cost(new BigDecimal("4.50"))
            .available(true)
            .category(drinksCategory)
            .build();
        menuItemRepository.save(availableDrink);

        // 4. Unavailable Drink
        MenuItem unavailableDrink = MenuItem.builder()
            .name("Seasonal Mango Smoothie")
            .description("Fresh mango blend - out of season")
            .cost(new BigDecimal("5.50"))
            .available(false)
            .category(drinksCategory)
            .build();
        menuItemRepository.save(unavailableDrink);

        // 5. Available Combo
        MenuItem availableCombo = MenuItem.builder()
            .name("Burger & Beer Special")
            .description("Classic cheeseburger paired with a craft draft beer")
            .cost(new BigDecimal("16.99"))
            .available(true)
            .category(combosCategory)
            .build();
        menuItemRepository.save(availableCombo);

        // 6. Unavailable Combo
        MenuItem unavailableCombo = MenuItem.builder()
            .name("Family Feast Box")
            .description("2 Mains, 2 Drinks, and a shared appetizer side")
            .cost(new BigDecimal("34.99"))
            .available(false)
            .category(combosCategory)
            .build();
        menuItemRepository.save(unavailableCombo);
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
    void findByCategoryIdShouldReturnListOfMenuItemsInThatCategory() {
        List<MenuItem> items = menuItemRepository.findByCategory_Id(1L);
        boolean result = categoryRepository.existsByName("Mains");
        assertTrue(result);
        assertEquals("Grilled Chicken", items.listIterator());
    }

    @Test
    void existsByNameShouldReturnFalseWhenNameDoesNotExist() {
        boolean result = categoryRepository.existsByName("False");
        
        assertFalse(result);
    }
}
