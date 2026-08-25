#!/bin/sh

set -eu

BASE_URL="${1:-http://localhost:8181/api}"
OUTPUT_FILE="${2:-openapi.json}"

if ! curl --fail --silent --show-error "$BASE_URL/v3/api-docs" -o "$OUTPUT_FILE"; then
    echo "Failed to download OpenAPI specification from $BASE_URL/v3/api-docs." >&2
    exit 1
fi

echo "OpenAPI specification exported to $OUTPUT_FILE"
