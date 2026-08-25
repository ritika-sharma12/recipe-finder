package com.recipefinder.repository;

import com.recipefinder.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    List<Recipe> findByAuthor(String author);

    @Query("SELECT r FROM Recipe r WHERE r.cookTimeMinutes <= :maxTime")
    List<Recipe> findByMaxCookTime(@Param("maxTime") Integer maxTime);

    @Query("""
        SELECT DISTINCT r
        FROM Recipe r
        LEFT JOIN FETCH r.ingredients ri
        LEFT JOIN FETCH ri.ingredient
        ORDER BY r.title ASC
        """)
    List<Recipe> findAllWithIngredients();

    @Query("""
        SELECT DISTINCT r
        FROM Recipe r
        LEFT JOIN FETCH r.ingredients ri
        LEFT JOIN FETCH ri.ingredient
        WHERE r.id = :id
        """)
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);
}
