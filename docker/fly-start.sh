#!/bin/sh
set -eu

java ${JAVA_OPTS:-} -jar /app/backend.jar &
backend_pid=$!

cleanup() {
  kill "$backend_pid" 2>/dev/null || true
}

trap cleanup INT TERM EXIT

exec node /app/frontend/.output/server/index.mjs
