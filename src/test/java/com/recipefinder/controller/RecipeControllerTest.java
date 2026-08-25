package com.recipefinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipefinder.dto.RecipeDTO;
import com.recipefinder.dto.RecipeSearchRequest;
import com.recipefinder.dto.PageResponse;
import com.recipefinder.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecipeDTO testRecipeDTO;

    @BeforeEach
    void setUp() {
        testRecipeDTO = RecipeDTO.builder()
            .id(1L)
            .title("Chicken Fried Rice")
            .cookTimeMinutes(25)
            .prepTimeMinutes(15)
            .instructions("1. Cook rice\n2. Fry chicken")
            .ingredients(Collections.emptyList())
            .build();
    }

    @Test
    void testSearchRecipes_Success() throws Exception {
        RecipeSearchRequest request = RecipeSearchRequest.builder()
            .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
            .maxCookTime(30)
            .build();

        when(recipeService.searchRecipesByIngredients(any(), any(Pageable.class)))
            .thenReturn(pageResponse());

        mockMvc.perform(post("/recipes/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Chicken Fried Rice"));

        verify(recipeService, times(1)).searchRecipesByIngredients(any(), any(Pageable.class));
    }

    @Test
    void testSearchRecipes_WithAvailableIngredientsAlias() throws Exception {
        when(recipeService.searchRecipesByIngredients(any(), any(Pageable.class)))
            .thenReturn(pageResponse());

        String requestJson = """
            {
              "availableIngredients": ["Chicken", "Rice"],
              "maxCookTime": 30,
              "exactIngredientsMatch": true
            }
            """;

        mockMvc.perform(post("/recipes/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk());

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(RecipeSearchRequest.class);
        verify(recipeService, times(1)).searchRecipesByIngredients(requestCaptor.capture(), any(Pageable.class));
        assertEquals(2, requestCaptor.getValue().getAvailableBaseIngredients().size());
        assertEquals("Chicken", requestCaptor.getValue().getAvailableBaseIngredients().get(0));
        assertEquals("Rice", requestCaptor.getValue().getAvailableBaseIngredients().get(1));
        assertEquals(true, requestCaptor.getValue().getExactIngredientsMatch());
    }

    @Test
    void testGetAllRecipes() throws Exception {
        when(recipeService.getAllRecipes(any(Pageable.class)))
            .thenReturn(pageResponse());

        mockMvc.perform(get("/recipes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Chicken Fried Rice"));

        verify(recipeService, times(1)).getAllRecipes(any(Pageable.class));
    }

    @Test
    void testGetRecipeById_Found() throws Exception {
        when(recipeService.getRecipeById(1L))
            .thenReturn(testRecipeDTO);

        mockMvc.perform(get("/recipes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Chicken Fried Rice"));

        verify(recipeService, times(1)).getRecipeById(1L);
    }

    @Test
    void testGetRecipeById_NotFound() throws Exception {
        when(recipeService.getRecipeById(999L))
            .thenThrow(new RuntimeException("Recipe not found"));

        mockMvc.perform(get("/recipes/999"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchByName() throws Exception {
        when(recipeService.searchRecipesByTitle(eq("Chicken"), any(Pageable.class)))
            .thenReturn(pageResponse());

        mockMvc.perform(get("/recipes/search/by-title?title=Chicken"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Chicken Fried Rice"));

        verify(recipeService, times(1)).searchRecipesByTitle(eq("Chicken"), any(Pageable.class));
    }

    @Test
    void testGetByMaxCookTime() throws Exception {
        when(recipeService.getRecipesByMaxCookTime(eq(30), any(Pageable.class)))
            .thenReturn(pageResponse());

        mockMvc.perform(get("/recipes/time/30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].cookTimeMinutes").value(25));

        verify(recipeService, times(1)).getRecipesByMaxCookTime(eq(30), any(Pageable.class));
    }

    private PageResponse<RecipeDTO> pageResponse() {
        return PageResponse.<RecipeDTO>builder()
            .content(List.of(testRecipeDTO))
            .page(0)
            .size(20)
            .totalElements(1)
            .totalPages(1)
            .build();
    }
}
