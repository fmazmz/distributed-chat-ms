#!/usr/bin/env bash
set -euo pipefail

GRAPH_LAYER="$(cd "$(dirname "$0")/.." && pwd)"
ROUTER_BIN="$GRAPH_LAYER/router"

if [[ ! -x "$ROUTER_BIN" ]]; then
  echo "Installing Apollo Router into $GRAPH_LAYER ..."
  (
    cd "$GRAPH_LAYER"
    curl -sSL https://router.apollo.dev/download/nix/latest | sh
  )
fi

"$GRAPH_LAYER/scripts/compose-supergraph.sh"

check_port() {
  local port="$1"
  local name="$2"
  if ! (echo >/dev/tcp/127.0.0.1/"$port") 2>/dev/null; then
    echo "WARNING: nothing listening on 127.0.0.1:${port} (${name})"
    echo "  Minikube: run ./graph-layer/scripts/port-forward-subgraphs.sh in another terminal"
    echo "  Local dev: start ${name} with SERVER_PORT=${port}"
    missing_ports=1
  fi
}

missing_ports=0
check_port 8082 "user-manager"
check_port 8083 "message-manager"
check_port 8084 "analytics-svc"
if [[ "$missing_ports" -eq 1 ]]; then
  echo
  echo "Fix the missing ports above, then restart the router."
fi

echo "Starting Apollo Router:"
echo "  Sandbox UI:  http://localhost:4000/"
echo "  GraphQL API: http://localhost:4000/graphql"
echo "  (Set Authorization: Bearer <token> in Sandbox connection headers)"
exec "$ROUTER_BIN" \
  --config "$GRAPH_LAYER/router.yaml" \
  --supergraph "$GRAPH_LAYER/supergraph.graphql"
