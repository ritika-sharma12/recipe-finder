package com.recipefinder.service;

import com.recipefinder.dto.RecipeDTO;
import com.recipefinder.dto.RecipeSearchRequest;
import com.recipefinder.dto.PageResponse;
import com.recipefinder.model.Ingredient;
import com.recipefinder.model.Recipe;
import com.recipefinder.model.RecipeIngredient;
import com.recipefinder.repository.IngredientRepository;
import com.recipefinder.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe testRecipe;
    private Ingredient chicken;
    private Ingredient rice;

    @BeforeEach
    void setUp() {
        chicken = Ingredient.builder()
                .id(1L)
                .name("Chicken")
                .baseIngredient("Chicken")
                .category("Protein")
                .build();

        rice = Ingredient.builder()
                .id(2L)
                .name("Rice")
                .baseIngredient("Rice")
                .category("Grains")
                .build();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Chicken Fried Rice")
                .description("Quick Asian stir-fry")
                .cookTimeMinutes(25)
                .prepTimeMinutes(15)
                .servings(4)
                .instructions("1. Cook rice\n2. Fry chicken")
                .category("Main Dish")
                .cuisine("Asian")
                .build();

        RecipeIngredient chickenIngredient = RecipeIngredient.builder()
                .recipe(recipe)
                .ingredient(chicken)
                .build();
        RecipeIngredient riceIngredient = RecipeIngredient.builder()
                .recipe(recipe)
                .ingredient(rice)
                .build();
        recipe.setIngredients(new HashSet<>(Arrays.asList(chickenIngredient, riceIngredient)));
        testRecipe = recipe;
    }

    @Test
    void testSearchRecipesByIngredients_Success() {
        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .maxCookTime(30)
                .build();

        when(recipeRepository.findAllWithIngredients())
                .thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        assertNotNull(results);
        assertEquals("Chicken Fried Rice", results.getContent().get(0).getTitle());
        assertEquals("Main Dish", results.getContent().get(0).getCategory());
        assertEquals("Asian", results.getContent().get(0).getCuisine());
    }

    @Test
    void testSearchRecipesByIngredients_NoMatch() {
        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Fish"))
                .build();

        when(recipeRepository.findAllWithIngredients())
                .thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        assertEquals(0, results.getContent().size());
    }

    @Test
    void testSearchRecipesByIngredients_NonExactMatchIncludesRecipesWithAdditionalIngredients() {
        Ingredient onion = Ingredient.builder()
                .id(3L)
                .name("Onion")
                .baseIngredient("Onion")
                .build();
        RecipeIngredient onionIngredient = RecipeIngredient.builder()
                .recipe(testRecipe)
                .ingredient(onion)
                .build();
        testRecipe.getIngredients().add(onionIngredient);

        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .exactIngredientsMatch(false)
                .build();
        when(recipeRepository.findAllWithIngredients()).thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        assertEquals(1, results.getContent().size());
    }

    @Test
    void testSearchRecipesByIngredients_ExactMatchExcludesRecipesWithAdditionalIngredients() {
        Ingredient onion = Ingredient.builder()
                .id(3L)
                .name("Onion")
                .baseIngredient("Onion")
                .build();
        testRecipe.getIngredients().add(RecipeIngredient.builder()
                .recipe(testRecipe)
                .ingredient(onion)
                .build());

        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .exactIngredientsMatch(true)
                .build();
        when(recipeRepository.findAllWithIngredients()).thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        assertEquals(0, results.getContent().size());
    }

    @Test
    void testSearchRecipesByIngredients_ExactMatchIgnoresBasicPantryVariants() {
        addIngredient("Salt", "Salt", 3L);
        addIngredient("Water", "Water", 4L);
        addIngredient("Vegetable oil", "Vegetable oil", 5L);

        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .exactIngredientsMatch(true)
                .build();
        when(recipeRepository.findAllWithIngredients()).thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        assertEquals(1, results.getContent().size());
    }

    private void addIngredient(String name, String baseIngredient, Long id) {
        Ingredient ingredient = Ingredient.builder()
                .id(id)
                .name(name)
                .baseIngredient(baseIngredient)
                .build();
        testRecipe.getIngredients().add(RecipeIngredient.builder()
                .recipe(testRecipe)
                .ingredient(ingredient)
                .build());
    }

    @Test
    void testSearchRecipesByIngredients_AssumesBasicPantryIngredients() {
        Ingredient salt = Ingredient.builder()
                .id(3L)
                .name("Salt")
                .baseIngredient("Salt")
                .build();
        testRecipe.getIngredients().add(RecipeIngredient.builder()
                .recipe(testRecipe)
                .ingredient(salt)
                .build());

        when(recipeRepository.findAllWithIngredients()).thenReturn(Arrays.asList(testRecipe));

        RecipeSearchRequest nonExactRequest = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .exactIngredientsMatch(false)
                .build();
        RecipeSearchRequest exactRequest = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .exactIngredientsMatch(true)
                .build();

        assertEquals(1, recipeService.searchRecipesByIngredients(
                nonExactRequest, PageRequest.of(0, 20)).getContent().size());
        assertEquals(1, recipeService.searchRecipesByIngredients(
                exactRequest, PageRequest.of(0, 20)).getContent().size());
    }

    @Test
    void testGetAllRecipes() {
        when(recipeRepository.findAll()).thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.getAllRecipes(PageRequest.of(0, 20));

        assertNotNull(results);
        assertEquals(1, results.getContent().size());
    }

    @Test
    void testGetRecipeById_Found() {
        when(recipeRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(testRecipe));

        RecipeDTO result = recipeService.getRecipeById(1L);

        assertNotNull(result);
        assertEquals("Chicken Fried Rice", result.getTitle());
    }

    @Test
    void testGetRecipeById_NotFound() {
        when(recipeRepository.findByIdWithIngredients(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> recipeService.getRecipeById(999L));
    }

    @Test
    void testFilterByCookTime() {
        RecipeSearchRequest request = RecipeSearchRequest.builder()
                .availableBaseIngredients(Arrays.asList("Chicken", "Rice"))
                .maxCookTime(20)
                .build();

        when(recipeRepository.findAllWithIngredients())
                .thenReturn(Arrays.asList(testRecipe));

        PageResponse<RecipeDTO> results = recipeService.searchRecipesByIngredients(request, PageRequest.of(0, 20));

        // Recipe takes 25 minutes, so it should be filtered out
        assertEquals(0, results.getContent().size());
    }
}
