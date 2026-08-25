package com.recipefinder.controller;

import com.recipefinder.dto.RecipeDTO;
import com.recipefinder.dto.RecipeSearchRequest;
import com.recipefinder.dto.PageResponse;
import com.recipefinder.service.RecipeService;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/recipes")
@AllArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Recipes", description = "Recipe search and retrieval endpoints")
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * Search recipes by available ingredients and filters
     */
    @PostMapping("/search")
    @Operation(summary = "Search recipes by available ingredients",
            description = "Returns recipes that can be made with all supplied base ingredients and optional filters.")
    public ResponseEntity<PageResponse<RecipeDTO>> searchRecipes(
            @Valid @RequestBody RecipeSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(recipeService.searchRecipesByIngredients(searchRequest, pageable));
    }

    /**
     * Get all recipes
     */
    @GetMapping
    @Operation(summary = "Get all recipes")
    public ResponseEntity<PageResponse<RecipeDTO>> getAllRecipes(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(recipeService.getAllRecipes(pageable));
    }

    /**
     * Get recipe by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a recipe by ID")
    public ResponseEntity<RecipeDTO> getRecipeById(
            @Parameter(description = "Recipe database ID", example = "1")
            @PathVariable Long id
    ) {
        RecipeDTO recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    /**
     * Get recipes by max cook time (in minutes)
     */
    @GetMapping("/time/{maxTime}")
    @Operation(summary = "Get recipes by maximum cook time")
    public ResponseEntity<PageResponse<RecipeDTO>> getByMaxCookTime(@PathVariable Integer maxTime, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipesByMaxCookTime(maxTime, pageable));
    }
}
