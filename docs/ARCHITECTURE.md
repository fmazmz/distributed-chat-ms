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
- **WebSocket** `/chat` on message-manager for invites and live chat (connect with same JWT)

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

Set `APP_AUTH_MANAGER_URL`, `APP_USER_MANAGER_URL`, `APP_MESSAGE_MANAGER_URL`, `APP_JWK_SET_URI` on BFF when ports differ.

## Web UI (SPA)

Static HTML/JS is served by **BFF** at `http://localhost:8080/` (`bff/src/main/resources/static/`).

1. Start Postgres + auth (8081), user (8082), message (8083), then BFF (8080).
2. Open two browser profiles (or normal + incognito), register two users, copy each other’s user ID.
3. Both stay on the chat screen (WebSocket connected), one sends an invite by peer UUID, the other accepts, then message.

WebAuthn `rp-id` is `localhost`; use `http://localhost:8080` (not `127.0.0.1`) if the browser is picky. WebSocket URL defaults to `ws://127.0.0.1:8083/chat` (override with `APP_MESSAGE_WEBSOCKET_URL`).
