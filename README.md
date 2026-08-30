# Recipe Finder - Java Spring Boot Application

A full-stack web application that helps users find recipes based on ingredients they have at home. Built with Java Spring Boot and PostgreSQL.

## 🎯 Problem Statement

Create an application that helps users find the most relevant recipes they can prepare with ingredients available at home.

## 📋 User Stories

### User Story 1: Search Recipes by Available Ingredients
**As a** home cook  
**I want to** enter the ingredients I have available  
**So that** I can discover recipes I can make right now

**Acceptance Criteria:**
- User can select multiple ingredients from the list of available ingredients
- By default, checkbox is checked to search recipes that include all selected ingredients
- System displays recipes that can be prepared with those ingredients
- Results show recipes with all required ingredients available


### User Story 2: User should be able to search recipes including the mentioned ingredients and also see the missing ingredients for each recipe
**As a** user looking for dinner ideas  
**I want to** see detailed recipe information based on my selected ingredients, including any missing ingredients
**So that** I can decide if I want to prepare it

**Acceptance Criteria:**
- User should have a checkbox to which they can uncheck to search recipes including the selected ingredients
- Recipe details include: name, ingredients, cook time
- Step-by-step cooking instructions are clear
- Recipe must include the selected ingredients
- Ingredients are color-coded (have vs. don't have)

### User Story 3: User can filter the recipes search based on the max cooking time
**As a** busy cook with limited time  
**I want to** filter recipes by cooking time and difficulty  
**So that** I can find quick meals I'm capable of making

**Acceptance Criteria:**
- Filter by cook time (e.g., max cook time 30 minutes)
- Sort results by cooking time (fastest first)
- Results update dynamically as filters are applied

### User Story 4: Save Favorite Recipes (For Future Reference)
**As a** frequent user of the app  
**I want to** save recipes I like  
**So that** I can quickly access them later

**Acceptance Criteria:**
- User can mark recipes as favorites
- Favorites persist after logout
- User can view a "My Favorites" list
- User can remove recipes from favorites
- 
### User Story 4: Custom base ingredients (For Future Reference)
**As a** frequent user of the app  
**I want to** add my own base ingredients to the list of available ingredients
**So that** I can customize the search results based on my pantry

**Acceptance Criteria:**
- User can add custom ingredients to their profile
- Custom ingredients are stored in the database
- User can remove custom ingredients from their profile

## 🛠️ Tech Stack

- **Backend Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **ORM**: JPA/Hibernate
- **API**: RESTful API with Spring Web
- **Dependencies**:
  - Spring Data JPA
  - PostgreSQL JDBC Driver
  - Lombok (for reducing boilerplate)
  - Spring Validation

## 📁 Project Structure

```
recipe-finder/
├── src/
│   ├── main/
│   │   ├── java/com/recipefinder/
│   │   │   ├── RecipeFinderApplication.java     # Main app entry point
│   │   │   ├── controller/                       # REST Controllers
│   │   │   │   └── RecipeController.java
│   │   │   ├── service/                          # Business logic
│   │   │   │   └── RecipeService.java
│   │   │   ├── repository/                       # Data access
│   │   │   │   ├── RecipeRepository.java
│   │   │   │   ├── IngredientRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── model/                            # JPA Entities
│   │   │   │   ├── Recipe.java
│   │   │   │   ├── Ingredient.java
│   │   │   │   └── User.java
│   │   │   └── dto/                              # Data Transfer Objects
│   │   │       ├── RecipeDTO.java
│   │   │       └── RecipeSearchRequest.java
│   │   └── resources/
│   │       ├── application.properties             # Spring config
│   │       └── schema.sql                         # Database initialization
│   └── test/
├── pom.xml                                        # Maven configuration
└── README.md                                      # This file
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- Git

### Setup Instructions

#### 1. Clone the Repository
```bash
cd /Users/ritika.sharma/Documents/Personal/recipe-finder
```

#### 2. Create PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE recipe_finder_db;

# Exit psql
\q
```

#### 3. Initialize Database Schema

```bash
# Connect to the new database
psql -U postgres -d recipe_finder_db -f src/main/resources/schema.sql
```

This will:
- Create all tables (recipes, ingredients, users, and junction tables)
- Set up indexes for performance
- Insert sample data (20 ingredients and 5 sample recipes)

#### 4. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Default configuration (update if your PostgreSQL setup differs)
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_finder_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

#### 5. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at `http://localhost:8181/api`

## 🐳 Docker deployment

The Nuxt frontend belongs in `frontend/` beside the Spring Boot backend. Start
the full stack with:

```bash
docker compose up --build
```

The frontend is available at `http://localhost:3000`, and the API at
`http://localhost:8181/api`. The frontend container uses the internal Docker
service name `backend` for server-side API requests.

PostgreSQL stores its data in the named `postgres_data` volume. The schema and
full JSON recipe dataset are imported automatically the first time the volume
is created. To recreate the database and rerun the import:

```bash
docker compose down -v
docker compose up --build
```

## 📘 Exporting the API specification

The shareable OpenAPI specification is available in `openapi.json`. To refresh it
from a running application:

```bash
./scripts/export_openapi.sh
```

You can import `openapi.json` into Swagger Editor, Postman, or other OpenAPI
compatible tools. Use a different application URL or output file when needed:

```bash
./scripts/export_openapi.sh http://localhost:8283/api shared-openapi.json
```

## 📡 API Endpoints

### Search Recipes by Ingredients

**POST** `/api/recipes/search`

Request Body:
```json
{
  "availableIngredients": ["Chicken", "Rice", "Onion"],
  "difficulty": "EASY",
  "maxCookTime": 30,
  "minCookTime": 10
}
```

Response:
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

### Get All Recipes

**GET** `/api/recipes`

Response: Array of all recipes with full details

### Get Recipe by ID

**GET** `/api/recipes/{id}`

### Search by Name

**GET** `/api/recipes/search/by-name?name=Pasta`

### Filter by Difficulty

**GET** `/api/recipes/difficulty/EASY`

### Filter by Cook Time

**GET** `/api/recipes/time/30` (recipes with max 30 minutes cook time)

## 🗄️ Database Schema

### Tables

**users** - User accounts
- id (PK)
- email (UNIQUE)
- username (UNIQUE)
- password
- created_at, updated_at

**ingredients** - Available ingredients
- id (PK)
- name (UNIQUE)
- category
- created_at

**recipes** - Recipe details
- id (PK)
- name
- description
- cook_time_minutes
- prep_time_minutes
- difficulty (EASY, MEDIUM, HARD)
- servings
- instructions
- created_at, updated_at

**recipe_ingredients** - Junction table for recipe-ingredient relationship
- recipe_id (FK)
- ingredient_id (FK)

**user_favorite_recipes** - Junction table for user favorites
- user_id (FK)
- recipe_id (FK)

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn jacoco:report
```

## 🐛 Troubleshooting

### PostgreSQL Connection Error
```
Error: Could not get a connection to the database
```
**Solution:**
- Ensure PostgreSQL is running: `brew services start postgresql` (macOS)
- Check database exists: `psql -l | grep recipe_finder_db`
- Verify connection string in `application.properties`

### Database Already Exists
```
Database "recipe_finder_db" already exists
```
**Solution:**
```bash
# Drop and recreate
psql -U postgres -c "DROP DATABASE IF EXISTS recipe_finder_db;"
psql -U postgres -c "CREATE DATABASE recipe_finder_db;"
psql -U postgres -d recipe_finder_db -f src/main/resources/schema.sql
```

### Build Fails with Java Version Error
```
Solution: Ensure Java 17+ is installed
java -version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
```

## 📦 Dependencies

All dependencies are managed in `pom.xml`. Key dependencies:

```xml
<!-- Spring Boot Web -->
<spring-boot-starter-web>3.1.5</spring-boot-starter-web>

<!-- Spring Data JPA -->
<spring-boot-starter-data-jpa>3.1.5</spring-boot-starter-data-jpa>

<!-- PostgreSQL -->
<postgresql>42.6.0</postgresql>

<!-- Lombok -->
<lombok>1.18.30</lombok>
```

## 🚀 Deployment

### Deploy to Fly.io

The recommended Fly.io deployment uses one application image for the
frontend and backend, with PostgreSQL hosted separately for persistence. The
repository includes `Dockerfile.fly` and `fly.single.toml` for this setup.
Replace the example app name if it is already in use, and choose a nearby
Fly.io region if needed.

```bash
# Install flyctl
curl -L https://fly.io/install.sh | sh

# Login
flyctl auth login

# Create the combined frontend/backend app
flyctl launch --config fly.single.toml --no-deploy

# Configure the hosted PostgreSQL connection
flyctl secrets set \
  SPRING_DATASOURCE_URL="jdbc:postgresql://HOST:5432/recipe_finder_db?sslmode=require" \
  SPRING_DATASOURCE_USERNAME="postgres" \
  SPRING_DATASOURCE_PASSWORD="your-password"
flyctl deploy --config fly.single.toml
```

The frontend and backend run in the same Fly Machine. The frontend's Nuxt
server calls the backend through `http://127.0.0.1:8181/api`, while Fly
exposes only the frontend on port `3000`.

Create or provision PostgreSQL separately using Fly Managed Postgres or
another hosted PostgreSQL provider. If using a Fly PostgreSQL app, use its
private hostname as `HOST` and ensure it is in the same Fly organization.
The local Docker Compose database is not deployed by this configuration.

### Environment Variables

Create `.env` file or set in deployment platform:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/recipe_finder_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-password
```

## 📝 Future Enhancements

- User authentication and authorization
- Recipe ratings and reviews
- Dietary restrictions filtering (vegetarian, vegan, gluten-free)
- Nutritional information per recipe
- Shopping list generator
- Recipe recommendations based on user history
- Image upload for recipes
- Mobile app version
- Integration with grocery delivery services

## 👥 Contributing

1. Create a feature branch: `git checkout -b feature/feature-name`
2. Commit changes: `git commit -m "Add feature"`
3. Push to branch: `git push origin feature/feature-name`
4. Submit a pull request

## 📄 License

MIT License - See LICENSE file for details

## 📧 Support

For issues, questions, or suggestions, please open an issue on GitHub.
