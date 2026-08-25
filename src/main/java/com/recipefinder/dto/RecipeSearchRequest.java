package com.recipefinder.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeSearchRequest {

    @JsonAlias("availableIngredients")
    @Size(max = 100, message = "availableBaseIngredients must contain at most 100 items")
    private List<String> availableBaseIngredients;
    private Boolean exactIngredientsMatch;
    @Min(value = 0, message = "maxCookTime must be greater than or equal to 0")
    private Integer maxCookTime;
}
