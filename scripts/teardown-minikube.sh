#!/usr/bin/env bash
# Remove Minikube workloads from this repo's manifest, point Docker CLI back at the host, optionally stop Minikube / compose.
# Run from repo root: ./scripts/teardown-minikube.sh [--stop-cluster] [--compose-down]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="${ROOT}/k8s/minikube/all.yaml"

STOP_CLUSTER=0
COMPOSE_DOWN=0
for arg in "$@"; do
  case "$arg" in
    --stop-cluster) STOP_CLUSTER=1 ;;
    --compose-down) COMPOSE_DOWN=1 ;;
    *) echo "unknown option: $arg (use --stop-cluster and/or --compose-down)" >&2; exit 1 ;;
  esac
done

if command -v minikube >/dev/null 2>&1; then
  if minikube status >/dev/null 2>&1; then
    echo ">>> kubectl delete -f manifest (ignore if already gone)"
    minikube kubectl -- delete -f "${MANIFEST}" --ignore-not-found=true || true
  else
    echo "minikube not running; skipping kubectl delete"
  fi
fi

if command -v minikube >/dev/null 2>&1; then
  echo ">>> point Docker CLI back at host (unset minikube docker-env)"
  eval $(minikube docker-env -u 2>/dev/null) || true
fi

if [[ "${COMPOSE_DOWN}" -eq 1 ]]; then
  echo ">>> docker compose down (all three Postgres stacks)"
  for svc in user-manager auth-manager message-manager; do
    if [[ -f "${ROOT}/${svc}/compose.yml" ]]; then
      (cd "${ROOT}/${svc}" && docker compose down 2>/dev/null) || true
    fi
  done
fi

if [[ "${STOP_CLUSTER}" -eq 1 ]]; then
  if command -v minikube >/dev/null 2>&1; then
    echo ">>> minikube stop"
    minikube stop || true
  fi
fi

echo
echo "Docker on this shell now uses your normal daemon (not Minikube)."
echo "To deploy again: ./scripts/deploy-minikube.sh"
