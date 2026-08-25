package com.recipefinder.service;

import com.recipefinder.dto.RecipeDTO;
import com.recipefinder.dto.RecipeSearchRequest;
import com.recipefinder.dto.PageResponse;
import com.recipefinder.exception.RecipeNotFoundException;
import com.recipefinder.model.Ingredient;
import com.recipefinder.model.Recipe;
import com.recipefinder.model.RecipeIngredient;
import com.recipefinder.repository.RecipeRepository;
import com.recipefinder.repository.IngredientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

@Service
@AllArgsConstructor
public class RecipeService {

    private static final Set<String> BASIC_PANTRY_INGREDIENTS = Set.of("salt", "pepper", "oil","water");

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    /**
     * Search recipes based on available base ingredients
     */
    public PageResponse<RecipeDTO> searchRecipesByIngredients(RecipeSearchRequest searchRequest, Pageable pageable) {
        List<Recipe> allRecipes = recipeRepository.findAllWithIngredients();
        
        Set<String> availableBaseIngredients = searchRequest.getAvailableBaseIngredients() != null
            ? searchRequest.getAvailableBaseIngredients().stream()
                .map(this::normalizeIngredientForMatch)
                .filter(ingredient -> !isBasicPantryIngredient(ingredient))
                .collect(Collectors.toSet())
            : new HashSet<>();

        List<RecipeDTO> recipes = allRecipes.stream()
            .filter(recipe -> availableBaseIngredients.isEmpty()
                || matchesIngredients(recipe, availableBaseIngredients,
                    Boolean.TRUE.equals(searchRequest.getExactIngredientsMatch())))
            .filter(recipe -> matchesCookTime(recipe, searchRequest.getMaxCookTime()))
            .map(this::convertToDTO)
            .sorted(Comparator.comparing(RecipeDTO::getCookTimeMinutes, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
            return toPageResponse(recipes, pageable);
    }

    /**
     * Check if user has all required ingredients for a recipe
     */
    private boolean matchesIngredients(Recipe recipe, Set<String> availableBaseIngredients, boolean exactMatch) {
        Set<String> recipeIngredients = recipe.getIngredients().stream()
            .map(ri -> normalizeIngredientForMatch(ri.getIngredient().getBaseIngredient()))
            .filter(ingredient -> !isBasicPantryIngredient(ingredient))
            .collect(Collectors.toSet());

        if (exactMatch) {
            return availableBaseIngredients.stream()
                    .allMatch(available -> recipeIngredients.stream()
                            .anyMatch(recipeIngredient -> recipeIngredient.contains(available)
                                    || available.contains(recipeIngredient)));
        }

        return availableBaseIngredients.stream()
                .allMatch(available -> recipeIngredients.stream()
                        .anyMatch(recipeIngredient -> recipeIngredient.contains(available)
                                || available.contains(recipeIngredient)));
    }

    private boolean isBasicPantryIngredient(String ingredient) {
        return BASIC_PANTRY_INGREDIENTS.stream().anyMatch(basicIngredient -> ingredient.contains(basicIngredient));
    }

    private String normalizeIngredientForMatch(String ingredient) {
        if (ingredient == null) return "";
        String normalized = ingredient.toLowerCase();
        normalized = normalized.replaceAll("^(pint|quart|quarts|cup|cups|tablespoon|tablespoons|tbsp|teaspoon|teaspoons|tsp|ounce|ounces|oz|pound|pounds|lb|lbs|gram|grams|g|kg|ml|l|mm|can|cans|jar|jars|bottle|bottles|package|packages|pkg|pkgs|stick|sticks|bunch|bunches|head|heads|piece|pieces|pinch|pinches|dash|dashes|clove|cloves|slice|slices|sprigs)\\s+", "");
        return normalized.trim();
    }

    /**
     * Check if recipe matches cook time range
     */
    private boolean matchesCookTime(Recipe recipe, Integer maxTime) {
        Integer cookTime = recipe.getCookTimeMinutes();
        Integer prepTime = recipe.getPrepTimeMinutes();

        if (cookTime == null && prepTime == null) {
            return true;
        }

        if (maxTime == null) {
            return true;
        }

        int totalTime = Objects.requireNonNullElse(cookTime, 0) + Objects.requireNonNullElse(prepTime, 0);
        return totalTime <= maxTime;
    }

    /**
     * Get all recipes
     */
    public PageResponse<RecipeDTO> getAllRecipes(Pageable pageable) {
        List<RecipeDTO> recipes = recipeRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return toPageResponse(recipes, pageable);
    }

    /**
     * Get recipe by ID
     */
    public RecipeDTO getRecipeById(Long id) {
        return recipeRepository.findByIdWithIngredients(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new RecipeNotFoundException(id));
    }

    /**
     * Search recipes by title
     */
    public PageResponse<RecipeDTO> searchRecipesByTitle(String title, Pageable pageable) {
        List<RecipeDTO> recipes = recipeRepository.findByTitleContainingIgnoreCase(title).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return toPageResponse(recipes, pageable);
    }

    /**
     * Get recipes by max cook time
     */
    public PageResponse<RecipeDTO> getRecipesByMaxCookTime(Integer maxTime, Pageable pageable) {
        List<RecipeDTO> recipes = recipeRepository.findByMaxCookTime(maxTime).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return toPageResponse(recipes, pageable);
    }

    private <T> PageResponse<T> toPageResponse(List<T> items, Pageable pageable) {
        int fromIndex = Math.min((int) pageable.getOffset(), items.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), items.size());
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / pageable.getPageSize());
        return PageResponse.<T>builder()
            .content(items.subList(fromIndex, toIndex))
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(items.size())
            .totalPages(totalPages)
            .build();
    }

    /**
     * Convert Recipe entity to RecipeDTO
     */
    private RecipeDTO convertToDTO(Recipe recipe) {
        List<RecipeDTO.IngredientDetail> ingredientDetails = recipe.getIngredients().stream()
            .map(ri -> RecipeDTO.IngredientDetail.builder()
                .id(ri.getIngredient().getId())
                .name(ri.getIngredient().getName())
                .baseIngredient(ri.getIngredient().getBaseIngredient())
                .quantityWithUnit(ri.getQuantityWithUnit())
                .build())
            .collect(Collectors.toList());

        return RecipeDTO.builder()
            .id(recipe.getId())
            .title(recipe.getTitle())
            .cookTimeMinutes(recipe.getCookTimeMinutes())
            .prepTimeMinutes(recipe.getPrepTimeMinutes())
            .instructions(recipe.getInstructions())
            .author(recipe.getAuthor())
            .ratings(recipe.getRatings())
            .imageUrl(recipe.getImageUrl())
            .category(recipe.getCategory())
            .cuisine(recipe.getCuisine())
            .ingredients(ingredientDetails)
            .build();
    }
}
