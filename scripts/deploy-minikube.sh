#!/usr/bin/env bash
# Build JARs, build :local images into Minikube's Docker, apply k8s/minikube/all.yaml, restart apps.
# Run from repo root: ./scripts/deploy-minikube.sh [--compose-down]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="${ROOT}/k8s/minikube/all.yaml"

need() { command -v "$1" >/dev/null 2>&1 || { echo "missing: $1" >&2; exit 1; }; }
need minikube

COMPOSE_DOWN=0
for arg in "$@"; do
  case "$arg" in
    --compose-down) COMPOSE_DOWN=1 ;;
    *)
      echo "unknown option: $arg (use --compose-down before deploy, or omit flags)" >&2
      exit 1
      ;;
  esac
done

if [[ "${COMPOSE_DOWN}" -eq 1 ]]; then
  echo ">>> docker compose down (local Postgres stacks)"
  for svc in user-manager auth-manager message-manager; do
    if [[ -f "${ROOT}/${svc}/compose.yml" ]]; then
      (cd "${ROOT}/${svc}" && docker compose down 2>/dev/null) || true
    fi
  done
fi

if ! minikube status >/dev/null 2>&1; then
  echo "Starting minikube..."
  minikube start
fi

echo "Using Minikube Docker daemon for builds..."
eval "$(minikube docker-env)"

package_service() {
  local svc="$1"
  echo ">>> mvn package: ${svc}"
  (
    cd "${ROOT}/${svc}" || exit 1
    if [ -x ./mvnw ]; then
      ./mvnw -q clean package -DskipTests
    else
      mvn -q clean package -DskipTests
    fi
  )
}

for svc in user-manager auth-manager message-manager bff; do
  package_service "${svc}"
done

for svc in user-manager auth-manager message-manager bff; do
  echo ">>> docker build: ${svc}"
  docker build -t "${svc}:local" "${ROOT}/${svc}"
done

echo ">>> kubectl apply"
minikube kubectl -- apply -f "${MANIFEST}"

echo ">>> rollout restart (apps)"
minikube kubectl -- rollout restart deployment/user-manager deployment/auth-manager deployment/message-manager deployment/bff

for d in kafka kafka-ui postgres-user postgres-message postgres-auth user-manager auth-manager message-manager bff; do
  echo ">>> wait: ${d}"
  minikube kubectl -- rollout status "deployment/${d}" --timeout=240s
done

echo
echo "Done. Images are in Minikube Docker; host Docker unchanged until you run:"
echo "  ./scripts/teardown-minikube.sh"
echo
echo "Port-forward BFF (client entrypoint):"
echo "  minikube kubectl -- port-forward svc/bff 8080:8080"
echo
echo "Port-forward subgraphs for local Apollo Router (Sandbox at http://localhost:4000/):"
echo "  ./graph-layer/scripts/port-forward-subgraphs.sh"
echo "  cd graph-layer && ./scripts/run-router.sh"
echo
echo "Port-forward Kafka UI (topic browser, e.g. message-published):"
echo "  minikube kubectl -- port-forward svc/kafka-ui 8089:8080"
echo "  open http://localhost:8089"
