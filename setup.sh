#!/bin/bash

# Recipe Finder - Setup Script
# This script automates the initial setup of the Recipe Finder application

echo "🍳 Recipe Finder - Setup Script"
echo "================================"
echo ""

# Check Java version
echo "✅ Checking Java version..."
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "   Java version: $JAVA_VERSION"
echo ""

# Check Maven
echo "✅ Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven."
    exit 1
fi

MVN_VERSION=$(mvn -v | grep "Apache Maven" | awk '{print $3}')
echo "   Maven version: $MVN_VERSION"
echo ""

# Check PostgreSQL
echo "✅ Checking PostgreSQL..."
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL not found. Please install PostgreSQL."
    exit 1
fi

PG_VERSION=$(psql --version | awk '{print $3}')
echo "   PostgreSQL version: $PG_VERSION"
echo ""

# Database setup
echo "🗄️  Setting up database..."
HAS_POSTGRES_ROLE=$(psql -d postgres -Atqc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres';" 2>/dev/null || true)
if [ "$HAS_POSTGRES_ROLE" = "1" ]; then
    DEFAULT_PG_USER="postgres"
else
    DEFAULT_PG_USER=$(id -un)
fi

read -p "Enter PostgreSQL username (default: $DEFAULT_PG_USER): " PG_USER
PG_USER=${PG_USER:-$DEFAULT_PG_USER}

read -sp "Enter PostgreSQL password: " PG_PASSWORD
echo ""

run_psql() {
    if [ -n "$PG_PASSWORD" ]; then
        PGPASSWORD="$PG_PASSWORD" psql "$@"
    else
        psql "$@"
    fi
}

if ! run_psql -U "$PG_USER" -h localhost -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ Failed to authenticate with PostgreSQL using user '$PG_USER' on localhost:5432."
    echo "   Please verify your username/password and that PostgreSQL is configured for password authentication."
    exit 1
fi

echo "Creating database..."
DB_EXISTS=$(run_psql -U "$PG_USER" -h localhost -d postgres -Atqc "SELECT 1 FROM pg_database WHERE datname = 'recipe_finder_db';")
if [ "$DB_EXISTS" = "1" ]; then
    echo "⚠️  Database already exists, continuing..."
else
    if run_psql -U "$PG_USER" -h localhost -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE recipe_finder_db;" > /dev/null; then
        echo "✅ Database created successfully"
    else
        echo "❌ Failed to create database 'recipe_finder_db'."
        exit 1
    fi
fi

echo "Initializing schema..."
if run_psql -U "$PG_USER" -h localhost -d recipe_finder_db -v ON_ERROR_STOP=1 -f src/main/resources/schema.sql > /dev/null; then
    echo "✅ Schema initialized with sample data"
else
    echo "❌ Failed to initialize schema"
    exit 1
fi

echo "Importing JSON data..."
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required to import JSON data."
    exit 1
fi

set -- scripts/*.json
if [ "$1" = "scripts/*.json" ]; then
    echo "❌ No JSON files found in project root for import."
    exit 1
fi

IMPORT_SQL_FILE=$(mktemp)
if ! python3 scripts/generate_json_import_sql.py "$@" > "$IMPORT_SQL_FILE"; then
    echo "❌ Failed to generate SQL from JSON data."
    rm -f "$IMPORT_SQL_FILE"
    exit 1
fi

if run_psql -U "$PG_USER" -h localhost -d recipe_finder_db -v ON_ERROR_STOP=1 -f "$IMPORT_SQL_FILE" > /dev/null; then
    echo "✅ JSON data imported successfully"
else
    echo "❌ Failed to import JSON data into database."
    rm -f "$IMPORT_SQL_FILE"
    exit 1
fi
rm -f "$IMPORT_SQL_FILE"

# Update application properties
echo ""
echo "⚙️  Updating application configuration..."
cat > src/main/resources/application.properties << EOF
spring.application.name=recipe-finder
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_finder_db
spring.datasource.username=$PG_USER
spring.datasource.password=$PG_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# Server
server.port=8181
server.servlet.context-path=/api

# Logging
logging.level.root=INFO
logging.level.com.recipefinder=DEBUG
EOF

echo "✅ Configuration updated"
echo ""

# Build project
echo "🔨 Building project..."
mvn clean package -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✅ Project built successfully"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "================================"
echo "✅ Setup Complete!"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Run the application: mvn spring-boot:run"
echo "2. Open browser: http://localhost:8181/api/recipes"
echo "3. Try the API:"
echo "   POST http://localhost:8181/api/recipes/search"
echo "   With body: {\"availableIngredients\": [\"Chicken\", \"Rice\"]}"
echo ""
