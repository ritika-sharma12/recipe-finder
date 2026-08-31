# Database Schema Update - From recipes-en.json

## Summary of Changes

The database schema has been updated to accommodate the `recipes-en.json` file structure. This includes new fields for recipe metadata and a restructured ingredient system with quantity tracking.

---

## New Database Schema

### Tables

#### 1. **recipes**
Enhanced with new fields from JSON data:
```sql
CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,          -- Recipe name from JSON
    description TEXT,                      -- Recipe description
    cook_time_minutes INTEGER,             -- Cooking time
    prep_time_minutes INTEGER,             -- Preparation time
    difficulty VARCHAR(50),                -- Difficulty level (EASY, MEDIUM, HARD)
    servings INTEGER,                      -- Number of servings
    instructions TEXT,                     -- Cooking instructions
    author VARCHAR(255),                   -- Author/Creator name (from JSON)
    category VARCHAR(100),                 -- Recipe category (from JSON)
    cuisine VARCHAR(100),                  -- Cuisine type (from JSON)
    ratings DECIMAL(3,2),                  -- Rating score (e.g., 4.74, from JSON)
    image_url TEXT,                        -- Recipe image URL (from JSON)
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**New Fields:**
- `author` - User who created the recipe
- `category` - Type of recipe (Cornbread, Quick Bread, etc.)
- `cuisine` - Cuisine type (empty in current data)
- `ratings` - Average rating (decimal value like 4.74)
- `image_url` - URL to recipe image
- `title` - Renamed from `name` for JSON compatibility

#### 2. **ingredients**
Updated to support ingredient variations with base ingredient tracking:
```sql
CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,            -- Full ingredient name with measurement
    base_ingredient VARCHAR(255) NOT NULL, -- Base ingredient (e.g., "flour")
    category VARCHAR(100),                 -- Category (Grains, Protein, etc.)
    created_at TIMESTAMP
);
```

**New Fields:**
- `base_ingredient` - Standardized ingredient name for searching

**Example:**
```
name: "1 cup all-purpose flour"
base_ingredient: "all-purpose flour"
category: "Grains"
```

#### 3. **recipe_ingredients** (RESTRUCTURED)
Complete redesign to support ingredient quantities:
```sql
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,             -- Foreign key to recipes
    ingredient_id BIGINT NOT NULL,         -- Foreign key to ingredients
    quantity_with_unit VARCHAR(255),       -- Full quantity (e.g., "1 cup", "2 teaspoons")
    FOREIGN KEY (recipe_id) REFERENCES recipes(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);
```

**Key Change:** Changed from simple many-to-many to entity-based junction table with quantity tracking.

#### 4. **users** (Unchanged)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### 5. **user_favorite_recipes** (Unchanged)
```sql
CREATE TABLE user_favorite_recipes (
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, recipe_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
);
```

---

## Indexes Added

```sql
CREATE INDEX idx_recipes_title ON recipes(title);
CREATE INDEX idx_recipes_category ON recipes(category);
CREATE INDEX idx_recipes_cuisine ON recipes(cuisine);
CREATE INDEX idx_recipes_difficulty ON recipes(difficulty);
CREATE INDEX idx_recipes_author ON recipes(author);
CREATE INDEX idx_recipes_ratings ON recipes(ratings);
CREATE INDEX idx_ingredients_name ON ingredients(name);
CREATE INDEX idx_ingredients_base_ingredient ON ingredients(base_ingredient);
CREATE INDEX idx_ingredients_category ON ingredients(category);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

---

## Java Entity Changes

### Recipe Entity
**Fields Added:**
- `author: String` - Recipe creator
- `category: String` - Recipe category
- `cuisine: String` - Cuisine type
- `ratings: Double` - Rating score
- `imageUrl: String` - Image URL
- Changed `name` → `title`
- Changed `difficulty` from Enum to String
- Relationship changed from `Set<Ingredient>` to `Set<RecipeIngredient>`

### Ingredient Entity
**Fields Added:**
- `baseIngredient: String` - Standardized ingredient name

### New RecipeIngredient Entity
```java
@Entity
public class RecipeIngredient {
    @Id
    private Long id;
    
    @ManyToOne
    private Recipe recipe;
    
    @ManyToOne
    private Ingredient ingredient;
    
    @Column
    private String quantityWithUnit;  // e.g., "1 cup all-purpose flour"
}
```

---

## API Changes

### Old vs New Request Format

**Before:**
```json
{
  "availableIngredients": ["Chicken", "Rice", "Onion"],
  "difficulty": "EASY",
  "maxCookTime": 30
}
```

**After:**
```json
{
  "availableBaseIngredients": ["flour", "sugar", "egg"],
  "difficulty": "EASY",
  "maxCookTime": 30,
  "minCookTime": 10,
  "category": "Quick Bread",
  "cuisine": "American",
  "minRating": 4.5,
  "searchByTitle": "Bread"
}
```

### Old vs New Response Format

**Before:**
```json
{
  "id": 1,
  "name": "Recipe Name",
  "difficulty": "EASY",
  "ingredients": ["Chicken", "Rice"],
  "cookTimeMinutes": 25
}
```

**After:**
```json
{
  "id": 1,
  "title": "Golden Sweet Cornbread",
  "category": "Cornbread",
  "author": "bluegirl",
  "ratings": 4.74,
  "cuisine": "",
  "imageUrl": "https://...",
  "difficulty": "EASY",
  "cookTimeMinutes": 25,
  "prepTimeMinutes": 10,
  "ingredients": [
    {
      "id": 1,
      "name": "1 cup all-purpose flour",
      "baseIngredient": "all-purpose flour",
      "quantityWithUnit": "1 cup all-purpose flour",
      "category": "Grains"
    }
  ]
}
```

---

## New API Endpoints

### Filter by Category
```
GET /api/recipes/category/{category}
GET /api/recipes/category/Cornbread
```

### Filter by Cuisine
```
GET /api/recipes/cuisine/{cuisine}
GET /api/recipes/cuisine/Italian
```

### Filter by Author
```
GET /api/recipes/author/{author}
GET /api/recipes/author/bluegirl
```

### Filter by Rating
```
GET /api/recipes/rating/{minRating}
GET /api/recipes/rating/4.5
```

### Search by Title
```
GET /api/recipes/search/by-title?title=Bread
```

---

## Sample Data

The schema comes pre-populated with 5 sample recipes from recipes-en.json:
1. **Golden Sweet Cornbread** - by bluegirl (4.74 rating)
2. **Monkey Bread I** - by deleteduser (4.74 rating)
3. **Whole Wheat Beer Bread** - by Betty Latvala (4.52 rating)
4. **Winter Squash Rolls** - by TRACEY_MITCHELL (4.71 rating)
5. **Homemade Flour Tortillas** - by LaDonna (4.54 rating)

---

## Backward Compatibility Notes

### Breaking Changes
1. `name` field renamed to `title`
2. Ingredient search now uses `baseIngredient` instead of exact name match
3. `difficulty` is now a String (was Enum)
4. `RecipeSearchRequest` uses `availableBaseIngredients` instead of `availableIngredients`

### Required Code Updates
- **Service Layer**: Updated to search by base ingredient
- **Controller**: Added new filter endpoints
- **DTOs**: Updated to include new fields and nested ingredient details
- **Repositories**: Added new query methods for new filters

---

## Migration Notes

If migrating existing data:
1. The schema uses `DROP TABLE IF EXISTS ... CASCADE` to clear old structure
2. New tables are created with fresh structure
3. Sample data from recipes-en.json is loaded

To preserve old recipes, modify the schema to add migration columns and implement ETL logic.

---

## Future Enhancements

- Add `instructions` field to recipes
- Add `servings` field
- Support for dietary restrictions (vegetarian, vegan, gluten-free)
- Add nutrition information
- Support for recipe variations/substitutions
- Add tags for easier searching

