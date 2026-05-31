#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
FEDERATED_GRAPH="$ROOT/federated-graph"

cd "$FEDERATED_GRAPH"

echo "Composing federated supergraph from subgraph SDL..."
npx --yes @apollo/rover@0.26.2 supergraph compose \
  --config supergraph.yaml \
  --output supergraph.graphql

echo "Wrote $FEDERATED_GRAPH/supergraph.graphql"
