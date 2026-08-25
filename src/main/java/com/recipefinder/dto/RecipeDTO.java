package com.recipefinder.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {

    private Long id;
    private String title;
    private Integer cookTimeMinutes;
    private Integer prepTimeMinutes;
    private String instructions;
    private String author;
    private Double ratings;
    private String imageUrl;
    private String cuisine;
    private String category;
    private List<IngredientDetail> ingredients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientDetail {
        private Long id;
        private String name;
        private String baseIngredient;
        private String quantityWithUnit;
    }
}
