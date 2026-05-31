#!/usr/bin/env bash
# Forward Minikube subgraph services to the local ports expected by graph-layer/supergraph.yaml.
# Run from repo root in a dedicated terminal (keeps running until Ctrl+C):
#   ./graph-layer/scripts/port-forward-subgraphs.sh
set -euo pipefail

need() { command -v "$1" >/dev/null 2>&1 || { echo "missing: $1" >&2; exit 1; }; }
need minikube
need kubectl

if ! minikube status >/dev/null 2>&1; then
  echo "minikube is not running. Start it first: minikube start" >&2
  exit 1
fi

# Use kubectl directly to avoid repeated "minikube downloading dependencies" noise.
KUBECTL=(kubectl --context=minikube)

pids=()
cleanup() {
  echo
  echo "Stopping port-forwards..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
}
trap cleanup EXIT INT TERM

forward() {
  local local_port="$1"
  local svc="$2"
  local remote_port="${3:-8080}"
  echo ">>> ${local_port} -> svc/${svc}:${remote_port}"
  "${KUBECTL[@]}" port-forward "svc/${svc}" "${local_port}:${remote_port}" &
  pids+=("$!")
}

echo "Forwarding subgraphs for local Apollo Router (127.0.0.1):"
echo "  8081 auth-manager   (JWKS for local subgraph GraphiQL)"
echo "  8082 user-manager"
echo "  8083 message-manager"
echo "  8084 analytics-svc"
echo

forward 8081 auth-manager
forward 8082 user-manager
forward 8083 message-manager

if "${KUBECTL[@]}" get svc analytics-svc >/dev/null 2>&1; then
  forward 8084 analytics-svc
else
  echo ">>> analytics-svc not in cluster — start it locally on :8084 or deploy to Minikube"
fi

echo
echo "Port-forwards active. In another terminal:"
echo "  cd graph-layer && ./scripts/run-router.sh"
echo "  open http://localhost:4000/"
echo
echo "In Sandbox, set connection headers:"
echo '  { "Authorization": "Bearer <token from BFF login>" }'
echo
wait
