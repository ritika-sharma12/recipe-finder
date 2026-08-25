-- Recipe Finder Database Schema
-- PostgreSQL

-- Drop existing tables if they exist (for fresh setup)
DROP TABLE IF EXISTS user_favorite_recipes CASCADE;
DROP TABLE IF EXISTS recipe_ingredients CASCADE;
DROP TABLE IF EXISTS recipes CASCADE;
DROP TABLE IF EXISTS ingredients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ingredients table
CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_ingredient VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recipes table
CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cook_time_minutes INTEGER,
    prep_time_minutes INTEGER,
    difficulty VARCHAR(50),
    servings INTEGER,
    instructions TEXT,
    author VARCHAR(255),
    category VARCHAR(100),
    cuisine VARCHAR(100),
    ratings DECIMAL(3,2),
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recipe Ingredients Junction Table (with quantity information)
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_with_unit VARCHAR(255),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE,
    UNIQUE(recipe_id, ingredient_id)
);

-- User Favorite Recipes Junction Table
CREATE TABLE user_favorite_recipes (
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, recipe_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- Create indexes for better performance
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

-- Insert sample data from recipes-en.json
-- Sample Ingredients
INSERT INTO ingredients (name, base_ingredient, category) VALUES
('1 cup all-purpose flour', 'all-purpose flour', 'Grains'),
('1 cup yellow cornmeal', 'yellow cornmeal', 'Grains'),
('⅔ cup white sugar', 'white sugar', 'Sweetener'),
('1 teaspoon salt', 'salt', 'Seasoning'),
('3 ½ teaspoons baking powder', 'baking powder', 'Leavening Agent'),
('1 egg', 'egg', 'Protein'),
('1 cup milk', 'milk', 'Dairy'),
('⅓ cup vegetable oil', 'vegetable oil', 'Oil'),
('3 (12 ounce) packages refrigerated biscuit dough', 'biscuit dough', 'Grains'),
('2 teaspoons ground cinnamon', 'ground cinnamon', 'Spice'),
('½ cup margarine', 'margarine', 'Fat'),
('1 cup packed brown sugar', 'brown sugar', 'Sweetener'),
('½ cup chopped walnuts', 'walnuts', 'Nuts'),
('½ cup raisins', 'raisins', 'Dried Fruit'),
('1 ½ cups whole wheat flour', 'whole wheat flour', 'Grains'),
('⅓ cup packed brown sugar', 'brown sugar', 'Sweetener'),
('1 (12 fluid ounce) can or bottle beer', 'beer', 'Beverage'),
('1 ½ cups cubed winter squash', 'winter squash', 'Vegetable'),
('1 cup scalded milk', 'milk', 'Dairy'),
('2 (.25 ounce) packages active dry yeast', 'active dry yeast', 'Leavening Agent'),
('½ cup warm water (110 degrees F/45 degrees C)', 'water', 'Liquid'),
('½ cup white sugar', 'white sugar', 'Sweetener'),
('2 teaspoons salt', 'salt', 'Seasoning'),
('½ cup shortening', 'shortening', 'Fat'),
('4 cups all-purpose flour', 'all-purpose flour', 'Grains'),
('2 teaspoons baking powder', 'baking powder', 'Leavening Agent'),
('2 tablespoons lard', 'lard', 'Fat'),
('1 ½ cups water', 'water', 'Liquid');

-- Sample Recipes
INSERT INTO recipes (title, cook_time_minutes, prep_time_minutes, author, category, cuisine, ratings, image_url) VALUES
('Golden Sweet Cornbread', 25, 10, 'bluegirl', 'Cornbread', '', 4.74, 
 'https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fstatic.onecms.io%2Fwp-content%2Fuploads%2Fsites%2F43%2F2021%2F10%2F26%2Fcornbread-1.jpg'),

('Monkey Bread I', 35, 15, 'deleteduser', 'Monkey Bread', '', 4.74,
 'https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fstatic.onecms.io%2Fwp-content%2Fuploads%2Fsites%2F43%2F2018%2F11%2F546316.jpg'),

('Whole Wheat Beer Bread', 50, 10, 'Betty Latvala', 'Quick Bread', '', 4.52,
 'https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fimages.media-allrecipes.com%2Fuserphotos%2F9443508.jpg'),

('Winter Squash Rolls', 45, 30, 'TRACEY_MITCHELL', 'Roll and Bun Recipes', '', 4.71,
 'https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fimages.media-allrecipes.com%2Fuserphotos%2F784483.jpg'),

('Homemade Flour Tortillas', 45, 15, 'LaDonna', 'Tortillas', '', 4.54,
 'https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fstatic.onecms.io%2Fwp-content%2Fuploads%2Fsites%2F43%2F-0001%2F11%2F30%2F663757.jpg');

-- Link ingredients to recipes with quantities
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity_with_unit) VALUES
-- Golden Sweet Cornbread
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '1 cup all-purpose flour'), '1 cup all-purpose flour'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '1 cup yellow cornmeal'), '1 cup yellow cornmeal'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '⅔ cup white sugar'), '⅔ cup white sugar'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '1 teaspoon salt'), '1 teaspoon salt'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '3 ½ teaspoons baking powder'), '3 ½ teaspoons baking powder'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '1 egg'), '1 egg'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '1 cup milk'), '1 cup milk'),
((SELECT id FROM recipes WHERE title = 'Golden Sweet Cornbread'), (SELECT id FROM ingredients WHERE name = '⅓ cup vegetable oil'), '⅓ cup vegetable oil'),

-- Monkey Bread I
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '3 (12 ounce) packages refrigerated biscuit dough'), '3 (12 ounce) packages refrigerated biscuit dough'),
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '1 cup packed brown sugar'), '1 cup packed brown sugar'),
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '2 teaspoons ground cinnamon'), '2 teaspoons ground cinnamon'),
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '½ cup margarine'), '½ cup margarine'),
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '½ cup chopped walnuts'), '½ cup chopped walnuts'),
((SELECT id FROM recipes WHERE title = 'Monkey Bread I'), (SELECT id FROM ingredients WHERE name = '½ cup raisins'), '½ cup raisins'),

-- Whole Wheat Beer Bread
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '1 cup all-purpose flour'), '1 cup all-purpose flour'),
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '1 ½ cups whole wheat flour'), '1 ½ cups whole wheat flour'),
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '3 ½ teaspoons baking powder'), '4 ½ teaspoons baking powder'),
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '2 teaspoons salt'), '1 ½ teaspoons salt'),
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '⅓ cup packed brown sugar'), '⅓ cup packed brown sugar'),
((SELECT id FROM recipes WHERE title = 'Whole Wheat Beer Bread'), (SELECT id FROM ingredients WHERE name = '1 (12 fluid ounce) can or bottle beer'), '1 (12 fluid ounce) can or bottle beer'),

-- Winter Squash Rolls
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '1 ½ cups cubed winter squash'), '1 ½ cups cubed winter squash'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '1 cup scalded milk'), '1 cup scalded milk'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '2 (.25 ounce) packages active dry yeast'), '2 (.25 ounce) packages active dry yeast'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '½ cup warm water (110 degrees F/45 degrees C)'), '½ cup warm water (110 degrees F/45 degrees C)'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '4 cups all-purpose flour'), '6 cups all-purpose flour'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '½ cup white sugar'), '½ cup white sugar'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '2 teaspoons salt'), '2 teaspoons salt'),
((SELECT id FROM recipes WHERE title = 'Winter Squash Rolls'), (SELECT id FROM ingredients WHERE name = '½ cup shortening'), '½ cup shortening'),

-- Homemade Flour Tortillas
((SELECT id FROM recipes WHERE title = 'Homemade Flour Tortillas'), (SELECT id FROM ingredients WHERE name = '4 cups all-purpose flour'), '4 cups all-purpose flour'),
((SELECT id FROM recipes WHERE title = 'Homemade Flour Tortillas'), (SELECT id FROM ingredients WHERE name = '1 teaspoon salt'), '1 teaspoon salt'),
((SELECT id FROM recipes WHERE title = 'Homemade Flour Tortillas'), (SELECT id FROM ingredients WHERE name = '2 teaspoons baking powder'), '2 teaspoons baking powder'),
((SELECT id FROM recipes WHERE title = 'Homemade Flour Tortillas'), (SELECT id FROM ingredients WHERE name = '2 tablespoons lard'), '2 tablespoons lard'),
((SELECT id FROM recipes WHERE title = 'Homemade Flour Tortillas'), (SELECT id FROM ingredients WHERE name = '1 ½ cups water'), '1 ½ cups water');
