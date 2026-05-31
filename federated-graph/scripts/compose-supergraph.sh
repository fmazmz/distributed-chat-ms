#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
GRAPH_LAYER="$ROOT/graph-layer"

cd "$GRAPH_LAYER"

echo "Composing federated supergraph from subgraph SDL..."
npx --yes @apollo/rover@0.26.2 supergraph compose \
  --config supergraph.yaml \
  --output supergraph.graphql

echo "Wrote $GRAPH_LAYER/supergraph.graphql"
