# Recipe Finder API Documentation

## Base URL
```
http://localhost:8181/api
```

## Authentication
Currently, the API does not require authentication. (Future versions will include JWT token support)

## Response Format

All responses are in JSON format.

Collection endpoints support the following optional query parameters:

- `page` (zero-based, default `0`)
- `size` (default `20`, maximum `100`)

Paginated responses use this format:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

### Success Response (200)
```json
{
  "data": {...},
  "message": "Success"
}
```

### Error Response (400, 500)
```json
{
  "error": "Error message",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Endpoints

### 1. Search Recipes by Ingredients

**Endpoint:** `POST /recipes/search`

Pagination example: `POST /recipes/search?page=0&size=20`

**Description:** Find recipes that contain the given ingredients, with optional exact ingredient and cook time matching.

**Request Body:**
```json
{
  "availableIngredients": ["Chicken", "Rice", "Onion"],
  "maxCookTime": 30,
  "exactIngredientsMatch": false
}
```

**Request Parameters:**
- `availableIngredients` (Array, required): List of available ingredients
- `maxCookTime` (Integer, optional): Maximum cook time in minutes
- `exactIngredientsMatch` (Boolean, optional): When `true`, only recipes with exactly the submitted normalized ingredients are returned. When `false` or omitted, recipes containing all submitted ingredients are returned, including recipes with additional ingredients.

Salt, pepper, and oil are treated as basic pantry ingredients and are excluded from ingredient matching. They do not need to be selected in the UI.

**Response (200):**
```json
[
  {
    "id": 2,
    "name": "Chicken Fried Rice",
    "description": "Quick and easy Asian stir-fry dish",
    "cookTimeMinutes": 25,
    "prepTimeMinutes": 15,
    "difficulty": "EASY",
    "servings": 4,
    "instructions": "1. Cook rice...",
    "ingredients": ["Chicken", "Rice", "Onion", "Carrot"]
  }
]
```

**Example:**
```bash
curl -X POST http://localhost:8181/api/recipes/search \
  -H "Content-Type: application/json" \
  -d '{
    "availableIngredients": ["Chicken", "Rice"],
    "difficulty": "EASY",
    "maxCookTime": 30
  }'
```

---

### 2. Get All Recipes

**Endpoint:** `GET /recipes`

**Description:** Retrieve all available recipes.

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Spaghetti Carbonara",
    "description": "Classic Italian pasta with bacon and cream sauce",
    "cookTimeMinutes": 20,
    "prepTimeMinutes": 10,
    "difficulty": "MEDIUM",
    "servings": 4,
    "instructions": "...",
    "ingredients": ["Pasta", "Egg", "Salt", "Pepper"]
  },
  {...}
]
```

**Example:**
```bash
curl http://localhost:8181/api/recipes
```

---

### 3. Get Recipe by ID

**Endpoint:** `GET /recipes/{id}`

**Description:** Retrieve a specific recipe by its ID.

**Path Parameters:**
- `id` (Integer, required): Recipe ID

**Response (200):**
```json
{
  "id": 1,
  "name": "Spaghetti Carbonara",
  "description": "Classic Italian pasta with bacon and cream sauce",
  "cookTimeMinutes": 20,
  "prepTimeMinutes": 10,
  "difficulty": "MEDIUM",
  "servings": 4,
  "instructions": "1. Cook pasta until al dente\n2. Fry bacon...",
  "ingredients": ["Pasta", "Egg", "Salt", "Pepper"]
}
```

**Response (404):**
```json
{
  "error": "Recipe not found with id: 999"
}
```

**Example:**
```bash
curl http://localhost:8181/api/recipes/1
```

---

### 4. Search Recipes by Name

**Endpoint:** `GET /recipes/search/by-name`

**Description:** Search for recipes by name (partial match, case-insensitive).

**Query Parameters:**
- `name` (String, required): Recipe name to search for

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Spaghetti Carbonara",
    ...
  },
  {
    "id": 3,
    "name": "Tomato Basil Pasta",
    ...
  }
]
```

**Example:**
```bash
curl "http://localhost:8181/api/recipes/search/by-name?name=Pasta"
```

---

### 5. Get Recipes by Difficulty Level

**Endpoint:** `GET /recipes/difficulty/{difficulty}`

**Description:** Retrieve all recipes of a specific difficulty level.

**Path Parameters:**
- `difficulty` (String, required): Difficulty level (EASY, MEDIUM, HARD)

**Response (200):**
```json
[
  {
    "id": 2,
    "name": "Chicken Fried Rice",
    "difficulty": "EASY",
    ...
  },
  {
    "id": 3,
    "name": "Tomato Basil Pasta",
    "difficulty": "EASY",
    ...
  }
]
```

**Example:**
```bash
curl http://localhost:8181/api/recipes/difficulty/EASY
```

---

### 6. Get Recipes by Maximum Cook Time

**Endpoint:** `GET /recipes/time/{maxTime}`

**Description:** Retrieve recipes that can be prepared within a maximum cook time.

**Path Parameters:**
- `maxTime` (Integer, required): Maximum cook time in minutes

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Spaghetti Carbonara",
    "cookTimeMinutes": 20,
    ...
  },
  {
    "id": 3,
    "name": "Tomato Basil Pasta",
    "cookTimeMinutes": 30,
    ...
  }
]
```

**Example:**
```bash
curl http://localhost:8181/api/recipes/time/30
```

---

## Error Response

Errors use a consistent response format:

```json
{
  "timestamp": "2026-08-21T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Recipe not found with id: 999",
  "path": "/api/recipes/999"
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request - Invalid parameters |
| 404 | Not Found - Recipe doesn't exist |
| 500 | Internal Server Error |

---

## Data Models

### Recipe Object
```json
{
  "id": 1,
  "name": "Recipe Name",
  "description": "Brief description",
  "cookTimeMinutes": 20,
  "prepTimeMinutes": 10,
  "difficulty": "EASY",
  "servings": 4,
  "instructions": "Step by step instructions",
  "ingredients": ["Ingredient 1", "Ingredient 2"]
}
```

### Ingredient Object
```json
{
  "id": 1,
  "name": "Chicken",
  "category": "Protein"
}
```

### User Object
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "favoriteRecipes": [1, 2, 3]
}
```

---

## Difficulty Levels

- `EASY`: Quick meals, simple techniques (Under 30 min)
- `MEDIUM`: Standard recipes, some technique required (30-60 min)
- `HARD`: Complex recipes, advanced techniques (60+ min)

---

## Sample Usage Scenarios

### Scenario 1: User has Chicken and Rice
```bash
curl -X POST http://localhost:8181/api/recipes/search \
  -H "Content-Type: application/json" \
  -d '{
    "availableIngredients": ["Chicken", "Rice", "Onion", "Carrot"]
  }'
```

### Scenario 2: Quick and Easy Meals
```bash
curl http://localhost:8181/api/recipes/difficulty/EASY
```

### Scenario 3: Recipes under 30 minutes
```bash
curl http://localhost:8181/api/recipes/time/30
```

### Scenario 4: Search for Pasta recipes
```bash
curl "http://localhost:8181/api/recipes/search/by-name?name=Pasta"
```

---

## Rate Limiting

Currently, no rate limiting is implemented. (Future versions may include rate limiting)

---

## CORS

The API allows cross-origin requests from all origins (`*`). This is suitable for development. For production, update the `@CrossOrigin` annotation in `RecipeController.java`.

---

## Future Endpoints (Coming Soon)

- `POST /users/register` - User registration
- `POST /users/login` - User login
- `POST /users/{userId}/favorites/{recipeId}` - Add favorite recipe
- `DELETE /users/{userId}/favorites/{recipeId}` - Remove favorite recipe
- `GET /users/{userId}/favorites` - Get user's favorite recipes
- `POST /recipes` - Create new recipe (admin only)
- `PUT /recipes/{id}` - Update recipe (admin only)
- `DELETE /recipes/{id}` - Delete recipe (admin only)

---

## Support & Issues

For API issues, check:
1. Server is running on port 8181
2. Database connection is valid
3. PostgreSQL is running
4. Check application logs for detailed error messages
