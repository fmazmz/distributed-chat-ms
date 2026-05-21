#!/usr/bin/env zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "Scanning microservices in: $ROOT"
echo

for dir in "$ROOT"/*; do
  [[ -d "$dir" ]] || continue

  svc="$(basename "$dir")"

  echo "======================================="
  echo "Service: $svc"
  echo "======================================="

  #
  # Start docker compose if present
  #
  if [[ -f "$dir/compose.yml" || -f "$dir/docker-compose.yml" ]]; then
    echo ">>> Starting docker compose for $svc"

    (
      cd "$dir"

      if [[ -f "compose.yml" ]]; then
        docker compose -f compose.yml up -d
      else
        docker compose -f docker-compose.yml up -d
      fi
    )
  fi

  #
  # Run Spring Boot if detected
  #
  if [[ -f "$dir/pom.xml" ]]; then
    echo ">>> Starting Spring Boot (Maven) for $svc"

    (
      cd "$dir"
      ./mvnw spring-boot:run -Dspring.profiles.active=dev&
    )

  else
    echo ">>> No Spring Boot project detected"
  fi

  echo
done

echo "All services started successfully."
wait