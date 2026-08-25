#!/usr/bin/env python3
"""
Generate SQL to import recipe JSON data into PostgreSQL.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Iterable


UNICODE_FRACTIONS = "¼½¾⅐⅑⅒⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞"
LEADING_NUMBER_RE = re.compile(
    rf"^\s*(?:"
    rf"(?:\d+(?:\.\d+)?|\.\d+)%?(?:\s*(?:\d+\s*/\s*\d+|/\s*\d+|[{UNICODE_FRACTIONS}]))?"
    rf"|[{UNICODE_FRACTIONS}])\s*",
    re.UNICODE,
)
PARENS_RE = re.compile(r"\([^)]*\)")
UNIT_RE = re.compile(
    r"^(?:about\s+)?(?:an?\s+)?(?:"
    r"cups?|cupfuls?|pints?|quarts?|tablespoons?|tbsp|teaspoons?|tsp|ounces?|oz|pounds?|lbs?|grams?|kg|g|"
    r"milliliters?|ml|liters?|l|pinches?|dashes?|cloves?|slices?|cans?|jars?|bottles?|"
    r"packages?|pkgs?|bags?|containers?|sticks?|bunches?|heads?|pieces?|sprigs?|inches?|inch|"
    r"cut|chopped|diced|sliced|cubed|minced|shredded|grated|trimmed|fluid ounces?|fl oz"
    r")\b\.?\s*",
    re.IGNORECASE,
)
LEADING_FILLER_RE = re.compile(r"^(?:of|and)\s+", re.IGNORECASE)


def sql_literal(value: str | None) -> str:
    if value is None:
        return "NULL"
    escaped = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"


def sql_number(value: int | float | None) -> str:
    return "NULL" if value is None else str(value)


def as_int(value: object) -> int | None:
    if value is None:
        return None
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def as_float(value: object) -> float | None:
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def normalize_base_ingredient(ingredient: str) -> str:
    text = ingredient.strip().lower()
    text = text.split(",", 1)[0]
    text = PARENS_RE.sub(" ", text)
    text = LEADING_NUMBER_RE.sub("", text)
    text = re.sub(r"^\s*[-–]\s*", "", text)

    while True:
        next_text = LEADING_NUMBER_RE.sub("", text)
        next_text = re.sub(r"^\s*[-–]\s*", "", next_text)
        next_text = UNIT_RE.sub("", next_text)
        next_text = LEADING_FILLER_RE.sub("", next_text).strip()
        if next_text == text:
            break
        text = next_text

    text = re.sub(r"\s+", " ", text).strip(" -")
    return text


def infer_category(base_ingredient: str) -> str:
    keyword_map: list[tuple[str, str]] = [
        ("flour", "Grains"),
        ("rice", "Grains"),
        ("cornmeal", "Grains"),
        ("oats", "Grains"),
        ("yeast", "Leavening Agent"),
        ("baking powder", "Leavening Agent"),
        ("baking soda", "Leavening Agent"),
        ("egg", "Protein"),
        ("chicken", "Protein"),
        ("beef", "Protein"),
        ("turkey", "Protein"),
        ("milk", "Dairy"),
        ("butter", "Dairy"),
        ("cheddar", "Dairy"),
        ("yogurt", "Dairy"),
        ("oil", "Oil"),
        ("olive", "Oil"),
        ("sugar", "Sweetener"),
        ("honey", "Sweetener"),
        ("molasses", "Sweetener"),
        ("salt", "Seasoning"),
        ("pepper", "Spice"),
        ("cinnamon", "Spice"),
        ("vanilla", "Flavoring"),
        ("lemon", "Flavoring"),
        ("water", "Liquid"),
        ("beer", "Beverage"),
    ]

    for keyword, category in keyword_map:
        if keyword in base_ingredient:
            return category
    return "Other"


def chunked(items: list[str], size: int) -> Iterable[list[str]]:
    for i in range(0, len(items), size):
        yield items[i : i + size]


def load_recipes(paths: list[Path]) -> list[dict]:
    recipes_by_title: dict[str, dict] = {}

    for path in paths:
        try:
            parsed = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            raise ValueError(f"Invalid JSON in {path}: {exc}") from exc

        if not isinstance(parsed, list):
            continue

        for item in parsed:
            if not isinstance(item, dict):
                continue
            title = str(item.get("title", "")).strip()
            ingredients = item.get("ingredients")
            if not title or not isinstance(ingredients, list):
                continue
            if title not in recipes_by_title:
                recipes_by_title[title] = item

    return list(recipes_by_title.values())


def main() -> int:
    if len(sys.argv) < 2:
        print("Usage: generate_json_import_sql.py <json-file> [<json-file>...]", file=sys.stderr)
        return 1

    json_paths = [Path(arg) for arg in sys.argv[1:]]
    missing = [str(path) for path in json_paths if not path.exists()]
    if missing:
        print(f"Missing JSON files: {', '.join(missing)}", file=sys.stderr)
        return 1

    recipes = load_recipes(json_paths)
    if not recipes:
        print("No recipe records found in provided JSON files.", file=sys.stderr)
        return 1

    ingredient_rows: dict[str, tuple[str, str]] = {}
    recipe_rows: list[str] = []
    recipe_ingredient_rows: list[str] = []
    seen_recipe_ingredient_pairs: set[tuple[str, str]] = set()

    for recipe in recipes:
        title = str(recipe.get("title", "")).strip()
        if not title:
            continue

        cook_time = as_int(recipe.get("cook_time_minutes", recipe.get("cook_time")))
        prep_time = as_int(recipe.get("prep_time_minutes", recipe.get("prep_time")))
        servings = as_int(recipe.get("servings"))
        ratings = as_float(recipe.get("ratings"))
        description = recipe.get("description")
        instructions = recipe.get("instructions")
        difficulty = recipe.get("difficulty")
        author = recipe.get("author")
        category = recipe.get("category")
        cuisine = recipe.get("cuisine")
        image_url = recipe.get("image_url", recipe.get("image"))

        recipe_rows.append(
            "("
            + ", ".join(
                [
                    sql_literal(title),
                    sql_literal(str(description).strip() if description else None),
                    sql_number(cook_time),
                    sql_number(prep_time),
                    sql_literal(str(difficulty).strip() if difficulty else None),
                    sql_number(servings),
                    sql_literal(str(instructions).strip() if instructions else None),
                    sql_literal(str(author).strip() if author else None),
                    sql_literal(str(category).strip() if category else None),
                    sql_literal(str(cuisine).strip() if cuisine else None),
                    sql_number(ratings),
                    sql_literal(str(image_url).strip() if image_url else None),
                ]
            )
            + ")"
        )

        raw_ingredients = recipe.get("ingredients", [])
        if not isinstance(raw_ingredients, list):
            continue

        for raw_ingredient in raw_ingredients:
            ingredient_name = str(raw_ingredient).strip()
            if not ingredient_name:
                continue
            if ingredient_name not in ingredient_rows:
                base = normalize_base_ingredient(ingredient_name)
                if not base:
                    continue
                ingredient_rows[ingredient_name] = (base, infer_category(base))

            recipe_ingredient_key = (title, ingredient_name)
            if recipe_ingredient_key in seen_recipe_ingredient_pairs:
                continue
            seen_recipe_ingredient_pairs.add(recipe_ingredient_key)

            recipe_ingredient_rows.append(
                "("
                + ", ".join(
                    [
                        sql_literal(title),
                        sql_literal(ingredient_name),
                        sql_literal(ingredient_name),
                    ]
                )
                + ")"
            )

    if not recipe_rows:
        print("No valid recipe records after filtering.", file=sys.stderr)
        return 1

    ingredient_values = [
        "(" + ", ".join([sql_literal(name), sql_literal(base), sql_literal(category)]) + ")"
        for name, (base, category) in ingredient_rows.items()
    ]

    print("BEGIN;")
    print("TRUNCATE TABLE recipe_ingredients, recipes, ingredients RESTART IDENTITY CASCADE;")

    for batch in chunked(ingredient_values, 500):
        print("INSERT INTO ingredients (name, base_ingredient, category) VALUES")
        print(",\n".join(batch) + ";")

    for batch in chunked(recipe_rows, 200):
        print(
            "INSERT INTO recipes (title, description, cook_time_minutes, prep_time_minutes, "
            "difficulty, servings, instructions, author, category, cuisine, ratings, image_url) VALUES"
        )
        print(",\n".join(batch) + ";")

    for batch in chunked(recipe_ingredient_rows, 500):
        print(
            "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity_with_unit)\n"
            "SELECT r.id, i.id, links.quantity_with_unit\n"
            "FROM (VALUES"
        )
        print(",\n".join(batch))
        print(") AS links(recipe_title, ingredient_name, quantity_with_unit)")
        print("JOIN recipes r ON r.title = links.recipe_title")
        print("JOIN ingredients i ON i.name = links.ingredient_name;")

    print("COMMIT;")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
