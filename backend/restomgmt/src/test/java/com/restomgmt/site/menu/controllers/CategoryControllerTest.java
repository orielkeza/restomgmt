package com.restomgmt.site.menu.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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

import com.restomgmt.site.menu.dto.CategoryRequest;
import com.restomgmt.site.menu.dto.CategoryResponse;
import com.restomgmt.site.menu.services.CategoryService;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;

@WebMvcTest(
    controllers = CategoryController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
    })
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
})
@ActiveProfiles("uat")
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @SpringBootConfiguration
    @Import(CategoryController.class)
    static class TestConfig {
    }

   @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void getAllCategoriesShouldReturn200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(
            CategoryResponse.builder().id(1L).name("Mains").build()
        ));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/categories"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Mains"));
    }

    @Test
    void getCategoryByIdShouldReturn200WhenExists() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(
            CategoryResponse.builder().id(1L).name("Mains").build()
        );

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/categories/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mains"));
    }

    @Test
    void getCategoryByIdShouldReturn404WhenNotExists() throws Exception {
        when(categoryService.getCategoryById(99L))
            .thenThrow(new NoSuchElementException("Category not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/menu/categories/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void createCategoryShouldReturn201WhenValid() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(
            CategoryResponse.builder().id(1L).name("Drinks").build()
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Drinks\"}"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Drinks"));
    }

    @Test
    void createCategoryShouldReturn400WhenNameBlank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void createCategoryShouldReturn400WhenNameAlreadyExists() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class)))
            .thenThrow(new IllegalArgumentException("Category already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mains\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateCategoryShouldReturn200WhenValid() throws Exception {
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(
            CategoryResponse.builder().id(1L).name("Updated Mains").build()
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/menu/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Mains\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Mains"));
    }

    @Test
    void updateCategoryShouldReturn404WhenNotExists() throws Exception {
        when(categoryService.updateCategory(eq(99L), any(CategoryRequest.class)))
            .thenThrow(new NoSuchElementException("Category not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/menu/categories/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ghost\"}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void deleteCategoryShouldReturn204WhenExists() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/menu/categories/1"))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void deleteCategoryShouldReturn404WhenNotExists() throws Exception {
        doThrow(new NoSuchElementException("Category not found"))
            .when(categoryService).deleteCategory(99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/menu/categories/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    } 
}
