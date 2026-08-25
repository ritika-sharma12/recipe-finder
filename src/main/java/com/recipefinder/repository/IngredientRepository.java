package com.recipefinder.repository;

import com.recipefinder.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);

    Optional<Ingredient> findByBaseIngredientIgnoreCase(String baseIngredient);

    List<Ingredient> findByCategory(String category);

    List<Ingredient> findByNameContainingIgnoreCase(String name);

    List<Ingredient> findByBaseIngredientContainingIgnoreCase(String baseIngredient);
}
