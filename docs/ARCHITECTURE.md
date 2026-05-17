# Distributed chat — architecture

Single path for registration, auth, profiles, and messages. Aligned with the lab (BFF, JWT, gRPC, Kafka) with WebAuthn as the twist.

## Components

| Service | Role | Database |
|---------|------|----------|
| **bff** | Only public REST entry; validates JWT; orchestrates register | None |
| **auth-manager** | WebAuthn + JWT; owns identity (username, email, passkeys) | AuthDB |
| **user-manager** | User profiles (same UUID as auth); gRPC `GetUser` | UserDB |
| **message-manager** | Messages, WebSocket chat, Kafka `message-published` | MessageDB |

Auth and User are **independent**: no gRPC between them. The BFF links them after registration.

## Registration flow (auth first)

1. Client → **BFF** `POST /api/v1/auth/register/start` `{ userName, email }`
2. BFF → **auth-manager** → creates `auth_accounts` row (`PENDING`) + WebAuthn ceremony → returns `ceremonyId` + options
3. Browser runs WebAuthn (`navigator.credentials.create`)
4. Client → **BFF** `POST /api/v1/auth/register/finish` `{ ceremonyId, credentialJson, userName, email }`
5. BFF → **auth-manager** finish → stores passkey, sets account `ACTIVE`, returns JWT (`sub` = account UUID)
6. BFF → **user-manager** `POST /api/v1/users` `{ id, userName, email }` with `id` = JWT `sub`

## Login flow

1. `POST /api/v1/auth/login/start` `{ userName }` → auth-manager (active account + passkey)
2. WebAuthn assert in browser
3. `POST /api/v1/auth/login/finish` → JWT

## Messages (lab REST + real-time twist)

- **REST (via BFF, JWT):** `POST/GET /api/v1/sessions/{sessionId}/messages`
- **message-manager** checks sender exists via **gRPC → user-manager** before save, then publishes **`message-published`** to Kafka
- **WebSocket** via BFF proxy `ws://<bff-host>/ws/chat` → message-manager `/chat` (JWT in handshake headers only; see below)

## Client rules

- Talk to **BFF only** for HTTP (port 8080 in k8s / local default)
- Use `Authorization: Bearer <accessToken>` on protected routes
- `userId` everywhere = UUID string from JWT `sub` (same as auth account id and user profile id)

## Local ports (suggested)

| Service | HTTP | gRPC |
|---------|------|------|
| bff | 8080 | — |
| auth-manager | 8081 | — |
| user-manager | 8082 | 9090 |
| message-manager | 8083 | — |
| kafka-ui | 8089 | — (compose or `port-forward svc/kafka-ui 8089:8080` in minikube) |

Set `APP_AUTH_MANAGER_URL`, `APP_USER_MANAGER_URL`, `APP_MESSAGE_MANAGER_URL`, `APP_JWK_SET_URI` on BFF when ports differ.

**Kafka UI:** [provectuslabs/kafka-ui](https://github.com/provectus/kafka-ui) — browse cluster topics (main app topic: `message-published`). Minikube: port-forward as above. Compose: `docker compose up` in `message-manager/` then open http://localhost:8089.

## Web UI (SPA)

Static HTML/JS is served by **BFF** at `http://localhost:8080/` (`bff/src/main/resources/static/`).

1. Start Postgres + auth (8081), user (8082), message (8083), then BFF (8080).
2. Open two browser profiles (or normal + incognito), register two users, copy each other’s user ID.
3. Both stay on the chat screen (WebSocket connected), one sends an invite by peer UUID, the other accepts, then message.

WebAuthn `rp-id` is `localhost`; use `http://localhost:8080` (not `127.0.0.1`) if the browser is picky. Chat WebSocket connects to the **same host as the SPA** at `/ws/chat` (BFF proxies to message-manager).

## WebSocket authentication

Browsers cannot set `Authorization` on `new WebSocket()`, so:

1. Login/register sets an **HttpOnly** cookie `chat_access_token` (same JWT as REST) plus returns the token in JSON for `Authorization: Bearer` on REST.
2. Browser opens `ws://<bff>/ws/chat` with **no token in the URL**; the cookie is sent on the upgrade request.
3. BFF `ChatWebSocketAuthHandshakeInterceptor` validates the JWT (cookie or `Authorization` header) and rejects invalid handshakes.
4. BFF opens message-manager with `Authorization: Bearer` only (internal hop; no query string).
5. message-manager `ChatJwtHandshakeInterceptor` requires `Authorization` and `ChatHandler` validates the JWT again.

Unauthenticated WebSocket upgrades are rejected at the BFF handshake (and at message-manager if reached without a bearer).
