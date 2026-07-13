package com.restomgmt.site.menu.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.restomgmt.site.menu.dto.MenuItemRequest;
import com.restomgmt.site.menu.dto.MenuItemResponse;
import com.restomgmt.site.menu.services.MenuItemService;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;

@WebMvcTest(
    controllers = MenuItemController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
    })
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
})
@ActiveProfiles("uat")
@ExtendWith(MockitoExtension.class)
class MenuItemControllerTest {

    @SpringBootConfiguration
    @Import(MenuItemController.class)
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuItemService menuItemService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private MenuItemResponse buildResponse() {
        return MenuItemResponse.builder()
            .id(1L)
            .name("Grilled Chicken")
            .description("Herb-marinated grilled chicken")
            .cost(new BigDecimal("12.99"))
            .available(true)
            .categoryName("Mains")
            .build();
    }

    @Test
    void getAllItemsShouldReturn200() throws Exception {
        when(menuItemService.getAvailableItems()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Grilled Chicken"));
    }

    @Test
    void getAllItemsIncludingUnavailableShouldReturn200() throws Exception {
        when(menuItemService.getAllItems()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/items/all"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getItemsByCategoryShouldReturn200() throws Exception {
        when(menuItemService.getItemsByCategory(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/items/category/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].categoryName").value("Mains"));
    }

    @Test
    void getItemByIdShouldReturn200WhenExists() throws Exception {
        when(menuItemService.getItemById(1L)).thenReturn(buildResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/items/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Grilled Chicken"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cost").value(12.99));
    }

    @Test
    void getItemByIdShouldReturn404WhenNotExists() throws Exception {
        when(menuItemService.getItemById(99L))
            .thenThrow(new NoSuchElementException("Menu item not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/items/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void createItemShouldReturn201WhenValid() throws Exception {
        when(menuItemService.createItem(any(MenuItemRequest.class))).thenReturn(buildResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/menu/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Grilled Chicken\",\"cost\":12.99,\"categoryId\":1,\"available\":true}"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Grilled Chicken"));
    }

    @Test
    void createItemShouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/menu/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"cost\":12.99,\"categoryId\":1}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void createItemShouldReturn404WhenCategoryNotFound() throws Exception {
        when(menuItemService.createItem(any(MenuItemRequest.class)))
            .thenThrow(new NoSuchElementException("Category not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/menu/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Grilled Chicken\",\"cost\":12.99,\"categoryId\":99,\"available\":true}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void updateItemShouldReturn200WhenValid() throws Exception {
        when(menuItemService.updateItem(eq(1L), any(MenuItemRequest.class)))
            .thenReturn(buildResponse());

        mockMvc.perform(MockMvcRequestBuilders.put("/menu/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Grilled Chicken\",\"cost\":14.99,\"categoryId\":1,\"available\":true}"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateItemShouldReturn404WhenNotExists() throws Exception {
        when(menuItemService.updateItem(eq(99L), any(MenuItemRequest.class)))
            .thenThrow(new NoSuchElementException("Menu item not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/menu/items/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ghost\",\"cost\":9.99,\"categoryId\":1,\"available\":true}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deleteItemShouldReturn204WhenExists() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/menu/items/1"))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void deleteItemShouldReturn404WhenNotExists() throws Exception {
        doThrow(new NoSuchElementException("Menu item not found"))
            .when(menuItemService).deleteItem(99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/menu/items/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
