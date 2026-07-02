/*
   api.js — Shared API client + auth infra for all authenticated pages.
   Depends on config.js (API_BASE_URL). Load config.js BEFORE this file.

   Public surface:
     getToken()              -> raw JWT string or null
     getTokenClaims()        -> decoded payload object or null
     isAuthenticated()       -> boolean (token present AND not expired)
     requireAuth()           -> redirects to login if not authenticated
     fetchWithAuth(path, opts)-> parsed JSON; auto Bearer header + refresh + 401 handling
     logout()                -> blacklists token server-side, clears storage, redirects
*/

const TOKEN_KEY = "token";
const LOGIN_URL = "../auth/login.html";
// Backend REFRESH_THRESHOLD_MILLIS = 5 min before expiry.
const REFRESH_THRESHOLD_MS = 5 * 60 * 1000;

/* ---------- token helpers ---------- */

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

// Decode JWT payload (base64url) without verifying signature — display/expiry only.
function getTokenClaims() {
  const token = getToken();
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

// exp is seconds since epoch -> ms. Returns null if unknown.
function getTokenExpiryMs() {
  const claims = getTokenClaims();
  if (!claims || !claims.exp) return null;
  return claims.exp * 1000;
}

function isTokenExpired() {
  const expMs = getTokenExpiryMs();
  if (expMs === null) return false; // can't tell -> let server decide
  return Date.now() >= expMs;
}

function isAuthenticated() {
  return !!getToken() && !isTokenExpired();
}

/* ---------- auth guard ---------- */

function redirectToLogin() {
  clearToken();
  window.location.href = LOGIN_URL;
}

// Call at the top of every protected page script.
function requireAuth() {
  if (!isAuthenticated()) {
    redirectToLogin();
    return false;
  }
  return true;
}

/* ---------- silent token refresh ---------- */

let refreshInFlight = null;

// If the token is within REFRESH_THRESHOLD of expiry (but not yet expired),
// swap it for a fresh one via /auth/refresh. Coalesces concurrent calls.
async function maybeRefresh() {
  const expMs = getTokenExpiryMs();
  if (expMs === null) return;

  const msLeft = expMs - Date.now();
  if (msLeft <= 0 || msLeft > REFRESH_THRESHOLD_MS) return;

  if (refreshInFlight) return refreshInFlight;

  refreshInFlight = (async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: "POST",
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      const data = await res.json().catch(() => null);
      const newToken = data && data.data && data.data.token;
      if (res.ok && newToken) setToken(newToken);
    } catch (e) {
      // Non-fatal: the next fetchWithAuth 401 handler will catch a dead token.
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

/* ---------- core fetch wrapper ---------- */

/*
   fetchWithAuth("/transaction/all", { method: "GET" })
   fetchWithAuth("/transaction/add", { method: "POST", body: {...} })

   - Prepends API_BASE_URL.
   - Adds Authorization + Content-Type.
   - Accepts `body` as a plain object (auto JSON.stringify) or a string.
   - On 401: clears token, redirects to login, throws.
   - Returns parsed JSON (or null for empty bodies).
*/
async function fetchWithAuth(path, options = {}) {
  await maybeRefresh();

  const token = getToken();
  if (!token) {
    redirectToLogin();
    throw new Error("Not authenticated");
  }

  const headers = {
    Authorization: `Bearer ${token}`,
    ...(options.headers || {}),
  };

  let body = options.body;
  if (body !== undefined && body !== null && typeof body !== "string") {
    body = JSON.stringify(body);
    if (!headers["Content-Type"]) headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body,
  });

  if (res.status === 401) {
    redirectToLogin();
    throw new Error("Session expired");
  }

  // Some endpoints return plain text (batch ops). Try JSON, fall back to text.
  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch (e) {
      data = { success: res.ok, message: text };
    }
  }

  return { ok: res.ok, status: res.status, data };
}

/* ---------- logout ---------- */

async function logout() {
  try {
    await fetch(`${API_BASE_URL}/logout`, {
      method: "POST",
      headers: { Authorization: `Bearer ${getToken()}` },
    });
  } catch (e) {
    // Ignore network errors — still clear locally.
  } finally {
    clearToken();
    window.location.href = LOGIN_URL;
  }
}

/* ---------- small shared formatters ---------- */

function formatCurrency(n) {
  const num = Number(n) || 0;
  return num.toLocaleString(undefined, {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  });
}

function escapeHtml(str) {
  return String(str == null ? "" : str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
