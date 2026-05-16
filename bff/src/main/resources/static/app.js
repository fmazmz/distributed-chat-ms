/* Minimal client: WebAuthn via BFF, chat via WebSocket + REST history */

const TOKEN_KEY = 'chat_access_token';

const $ = (id) => document.getElementById(id);

function setStatus(el, text, kind) {
  el.textContent = text || '';
  el.className = 'status' + (kind ? ' ' + kind : '');
}

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

function bufferToBase64url(buffer) {
  const bytes = new Uint8Array(buffer);
  let str = '';
  for (const b of bytes) str += String.fromCharCode(b);
  return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function base64urlToBuffer(base64url) {
  const pad = '='.repeat((4 - (base64url.length % 4)) % 4);
  const b64 = (base64url + pad).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(b64);
  const buffer = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) buffer[i] = raw.charCodeAt(i);
  return buffer.buffer;
}

function unwrapPublicKey(options) {
  return options.publicKey ? options.publicKey : options;
}

function decodeCreationOptions(json) {
  let options = typeof json === 'string' ? JSON.parse(json) : json;
  options = unwrapPublicKey(options);
  options.challenge = base64urlToBuffer(options.challenge);
  options.user.id = base64urlToBuffer(options.user.id);
  if (options.excludeCredentials) {
    options.excludeCredentials = options.excludeCredentials.map((c) => ({
      ...c,
      id: base64urlToBuffer(c.id),
    }));
  }
  return options;
}

function decodeRequestOptions(json) {
  let options = typeof json === 'string' ? JSON.parse(json) : json;
  options = unwrapPublicKey(options);
  options.challenge = base64urlToBuffer(options.challenge);
  if (options.allowCredentials) {
    options.allowCredentials = options.allowCredentials.map((c) => ({
      ...c,
      id: base64urlToBuffer(c.id),
    }));
  }
  return options;
}

function clientExtensionResults(credential) {
  if (typeof credential.getClientExtensionResults !== 'function') {
    return {};
  }
  const results = credential.getClientExtensionResults();
  if (!results) return {};
  return JSON.parse(JSON.stringify(results));
}

function credentialToJson(credential) {
  const response = credential.response;
  const json = {
    id: credential.id,
    rawId: bufferToBase64url(credential.rawId),
    type: credential.type,
    clientExtensionResults: clientExtensionResults(credential),
    response: {
      clientDataJSON: bufferToBase64url(response.clientDataJSON),
    },
  };
  if (credential.authenticatorAttachment) {
    json.authenticatorAttachment = credential.authenticatorAttachment;
  }
  if (response.attestationObject) {
    json.response.attestationObject = bufferToBase64url(response.attestationObject);
  }
  if (response.authenticatorData) {
    json.response.authenticatorData = bufferToBase64url(response.authenticatorData);
  }
  if (response.signature) {
    json.response.signature = bufferToBase64url(response.signature);
  }
  if (response.userHandle && response.userHandle.byteLength > 0) {
    json.response.userHandle = bufferToBase64url(response.userHandle);
  }
  return JSON.stringify(json);
}

async function api(path, options = {}) {
  const { anonymous, ...fetchOptions } = options;
  const headers = { 'Content-Type': 'application/json', ...(fetchOptions.headers || {}) };
  if (!anonymous) {
    const token = getToken();
    if (token) headers.Authorization = 'Bearer ' + token;
  }
  const res = await fetch(path, { ...fetchOptions, headers });
  const text = await res.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }
  if (!res.ok) {
    const msg =
      body?.detail ||
      body?.message ||
      body?.error ||
      (typeof body === 'string' ? body : res.statusText);
    throw new Error(msg || 'Request failed');
  }
  return body;
}

async function register(userName, email) {
  setToken(null);
  const start = await api('/api/v1/auth/register/start', {
    method: 'POST',
    anonymous: true,
    body: JSON.stringify({ userName, email }),
  });
  if (!window.PublicKeyCredential) {
    throw new Error('WebAuthn is not supported in this browser');
  }
  const publicKey = decodeCreationOptions(start.publicKeyCredentialCreationOptionsJson);
  const credential = await navigator.credentials.create({ publicKey });
  const finish = await api('/api/v1/auth/register/finish', {
    method: 'POST',
    anonymous: true,
    body: JSON.stringify({
      ceremonyId: start.ceremonyId,
      credentialJson: credentialToJson(credential),
      userName,
      email,
    }),
  });
  setToken(finish.accessToken);
  return finish;
}

async function login(userName) {
  setToken(null);
  const start = await api('/api/v1/auth/login/start', {
    method: 'POST',
    anonymous: true,
    body: JSON.stringify({ userName }),
  });
  const publicKey = decodeRequestOptions(start.publicKeyCredentialRequestOptionsJson);
  const credential = await navigator.credentials.get({ publicKey });
  const finish = await api('/api/v1/auth/login/finish', {
    method: 'POST',
    anonymous: true,
    body: JSON.stringify({
      ceremonyId: start.ceremonyId,
      credentialJson: credentialToJson(credential),
    }),
  });
  setToken(finish.accessToken);
  return finish;
}

/* --- Chat state --- */
let ws = null;
let me = null;
let activeSessionId = null;
let wsReconnectTimer = null;

function showAuth() {
  $('auth-panel').classList.remove('hidden');
  $('chat-panel').classList.add('hidden');
  if (wsReconnectTimer) {
    clearTimeout(wsReconnectTimer);
    wsReconnectTimer = null;
  }
  if (ws) {
    ws.onclose = null;
    ws.close(1000);
    ws = null;
  }
}

function showChat() {
  $('auth-panel').classList.add('hidden');
  $('chat-panel').classList.remove('hidden');
}

async function loadMe() {
  me = await api('/api/v1/users/me');
  me.id = String(me.id);
  $('me-name').textContent = me.userName;
  $('me-id').textContent = me.id;
  return me;
}

function wsUrl() {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const path = (me && me.chatWebSocketUrl) || '/ws/chat';
  const base = path.startsWith('ws')
    ? path.replace(/\/$/, '')
    : `${proto}//${window.location.host}${path.startsWith('/') ? path : '/' + path}`.replace(/\/$/, '');
  return `${base}?token=${encodeURIComponent(getToken())}`;
}

function appendMessage(text, mine) {
  const div = document.createElement('div');
  div.className = 'msg ' + (mine ? 'mine' : 'theirs');
  div.textContent = text;
  $('messages').appendChild(div);
  $('messages').scrollTop = $('messages').scrollHeight;
}

async function loadHistory(sessionId) {
  const list = await api(`/api/v1/sessions/${sessionId}/messages`);
  $('messages').innerHTML = '';
  for (const m of list) {
    appendMessage(m.content, m.senderId === me.id);
  }
}

function activateSession(sessionId) {
  activeSessionId = sessionId;
  $('session-id').textContent = sessionId;
  $('active-chat').classList.remove('hidden');
  loadHistory(sessionId).catch((e) => setStatus($('ws-status'), e.message, 'err'));
}

const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function isUuid(value) {
  return UUID_RE.test(String(value).trim());
}

function handleWsMessage(ev) {
  let msg;
  try {
    msg = JSON.parse(ev.data);
  } catch {
    if (activeSessionId) appendMessage(ev.data, false);
    return;
  }
  console.log('WS ←', msg);
  switch (msg.type) {
    case 'CHAT_REQUEST':
      addInvite(String(msg.sessionId), String(msg.fromUserId));
      break;
    case 'CHAT_INVITE_SENT':
      setStatus($('ws-status'), 'Invite sent — waiting for them to accept', 'ok');
      break;
    case 'CHAT_PEER_OFFLINE':
      setStatus($('ws-status'), msg.message || 'That user is not online', 'err');
      break;
    case 'CHAT_ACTIVE':
      activateSession(String(msg.sessionId));
      hideInviteToast();
      setStatus($('ws-status'), 'Chat active', 'ok');
      break;
    case 'ERROR':
      setStatus($('ws-status'), msg.message, 'err');
      break;
    default:
      break;
  }
}

function scheduleWsReconnect() {
  if (wsReconnectTimer || $('chat-panel').classList.contains('hidden') || !getToken()) {
    return;
  }
  wsReconnectTimer = setTimeout(() => {
    wsReconnectTimer = null;
    connectWebSocket().catch(() => setStatus($('ws-status'), 'Reconnect failed — refresh the page', 'err'));
  }, 2000);
}

function connectWebSocket() {
  return new Promise((resolve, reject) => {
    if (wsReconnectTimer) {
      clearTimeout(wsReconnectTimer);
      wsReconnectTimer = null;
    }
    if (ws) {
      ws.onclose = null;
      ws.onerror = null;
      try {
        ws.close(1000);
      } catch (_) {
        /* ignore */
      }
    }
    ws = new WebSocket(wsUrl());
    ws.onopen = () => {
      setStatus($('ws-status'), 'Connected to chat — keep this tab open (do not refresh)', 'ok');
      resolve();
    };
    ws.onerror = () => {
      setStatus($('ws-status'), 'WebSocket failed (check BFF proxy /ws/chat)', 'err');
      reject(new Error('WebSocket connection failed'));
    };
    ws.onclose = (ev) => {
      if (ev.code === 1000) return;
      setStatus($('ws-status'), `Disconnected (${ev.code}) — reconnecting…`, 'err');
      scheduleWsReconnect();
    };
    ws.onmessage = handleWsMessage;
  });
}

function addInvite(sessionId, fromUserId) {
  const box = $('pending-invites');
  const id = 'invite-' + sessionId;
  if (document.getElementById(id)) return;

  showInviteToast('New chat invite — accept below to start talking.');
  if (Notification.permission === 'granted') {
    new Notification('Chat invite', { body: `User ${fromUserId} wants to chat` });
  }

  const el = document.createElement('div');
  el.className = 'invite';
  el.id = id;
  el.innerHTML = `<span>Invite from <code>${fromUserId}</code></span>`;
  const btn = document.createElement('button');
  btn.textContent = 'Accept';
  btn.type = 'button';
  btn.onclick = () => {
    sendWs('ACCEPT_CHAT', { sessionId: String(sessionId) });
    activateSession(sessionId);
    el.remove();
    hideInviteToast();
  };
  el.appendChild(btn);
  box.prepend(el);
  el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function showInviteToast(text) {
  const toast = $('invite-toast');
  toast.textContent = text;
  toast.classList.remove('hidden');
}

function hideInviteToast() {
  $('invite-toast').classList.add('hidden');
}

function sendWs(type, data) {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    throw new Error('Not connected');
  }
  ws.send(JSON.stringify({ type, data }));
}

async function enterApp() {
  await loadMe();
  showChat();
  if (Notification.permission === 'default') {
    Notification.requestPermission().catch(() => {});
  }
  await connectWebSocket();
}

/* --- UI wiring --- */
document.querySelectorAll('.tab').forEach((tab) => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach((t) => t.classList.remove('active'));
    tab.classList.add('active');
    const name = tab.dataset.tab;
    $('login-form').classList.toggle('hidden', name !== 'login');
    $('register-form').classList.toggle('hidden', name !== 'register');
    setStatus($('auth-status'), '');
  });
});

$('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const userName = fd.get('userName');
  setStatus($('auth-status'), 'Waiting for passkey…');
  try {
    await login(userName);
    setStatus($('auth-status'), '');
    await enterApp();
  } catch (err) {
    setStatus($('auth-status'), err.message, 'err');
  }
});

$('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const userName = fd.get('userName');
  const email = fd.get('email');
  setStatus($('auth-status'), 'Create your passkey in the browser prompt…');
  try {
    await register(userName, email);
    setStatus($('auth-status'), '');
    await enterApp();
  } catch (err) {
    setStatus($('auth-status'), err.message, 'err');
  }
});

$('logout-btn').addEventListener('click', () => {
  setToken(null);
  me = null;
  activeSessionId = null;
  showAuth();
});

$('invite-btn').addEventListener('click', () => {
  const peerId = $('peer-id').value.trim();
  if (!peerId) {
    setStatus($('ws-status'), 'Enter the other user’s UUID', 'err');
    return;
  }
  if (!isUuid(peerId)) {
    setStatus($('ws-status'), 'User ID must be a full UUID', 'err');
    return;
  }
  if (peerId === String(me.id)) {
    setStatus($('ws-status'), 'You cannot invite yourself', 'err');
    return;
  }
  try {
    sendWs('REQUEST_CHAT', { toUserId: peerId.toLowerCase() });
    setStatus($('ws-status'), 'Sending invite…', '');
  } catch (err) {
    setStatus($('ws-status'), err.message, 'err');
  }
});

$('send-form').addEventListener('submit', (e) => {
  e.preventDefault();
  const content = $('message-input').value.trim();
  if (!content || !activeSessionId) return;
  try {
    sendWs('MESSAGE', { sessionId: activeSessionId, content });
    appendMessage(content, true);
    $('message-input').value = '';
  } catch (err) {
    setStatus($('ws-status'), err.message, 'err');
  }
});

if (getToken()) {
  enterApp().catch(() => {
    setToken(null);
    showAuth();
  });
} else {
  showAuth();
}
