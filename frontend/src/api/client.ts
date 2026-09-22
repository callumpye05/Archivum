import type { UpdateUserDto } from "../types";

const baseUrl = (import.meta.env.VITE_API_BASE_URL || "/api").replace(
  /\/$/,
  "",
);
export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}
// HTTP Basic is the backend's existing authentication mechanism. Keep the
// credential in memory only; never persist it in browser storage.
let credentials: { username: string; password: string } | undefined;
function basicAuthorization(value = credentials) {
  if (!value) return undefined;
  const bytes = new TextEncoder().encode(`${value.username}:${value.password}`);
  return `Basic ${btoa(Array.from(bytes, (byte) => String.fromCharCode(byte)).join(""))}`;
}
let authRevision = 0;
const pendingRequests = new Set<AbortController>();
export function clearAuthorization() {
  credentials = undefined;
  authRevision += 1;
  pendingRequests.forEach((controller) => controller.abort());
  pendingRequests.clear();
}
const unauthorizedListeners = new Set<() => void>();
export function onUnauthorized(listener: () => void) {
  unauthorizedListeners.add(listener);
  return () => {
    unauthorizedListeners.delete(listener);
  };
}
export async function signIn(username: string, password: string) {
  const revision = authRevision;
  const candidate = { username, password };
  await request("/worlds", "GET", undefined, basicAuthorization(candidate));
  if (revision !== authRevision)
    throw new Error("Sign-in was cancelled. Please try again.");
  credentials = candidate;
}
// Commit new Basic credentials only after the existing endpoint accepts the
// change, using the password supplied at explicit login or its new replacement.
export async function updateCurrentUser(data: UpdateUserDto): Promise<string> {
  const current = credentials;
  const revision = authRevision;
  if (!current) throw new Error("Please log in to update your account.");
  await request<void>("/users/me", "PUT", data);
  if (revision !== authRevision || credentials !== current)
    throw new Error("Your sign-in changed. Please log in again.");
  const next = {
    username: data.userName ?? current.username,
    password: data.password ?? current.password,
  };
  // Cancel older requests so an old credential's late 401 cannot undo the update.
  clearAuthorization();
  credentials = next;
  return next.username;
}
export async function request<T>(
  path: string,
  method = "GET",
  body?: unknown,
  credential = basicAuthorization(),
): Promise<T> {
  const revision = authRevision;
  const controller = new AbortController();
  pendingRequests.add(controller);
  let response: Response;
  let text: string;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method,
      // Authenticate only with the explicit in-memory header, never ambient
      // browser cookies or cached HTTP Basic credentials.
      credentials: "omit",
      headers: {
        Accept: "application/json",
        // Let Spring return a 401 to the form without a browser Basic-auth dialog.
        "X-Requested-With": "XMLHttpRequest",
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
        ...(credential ? { Authorization: credential } : {}),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.any([controller.signal, AbortSignal.timeout(15000)]),
    });
    text = await response.text();
  } catch {
    throw new Error(
      "Could not reach Archivum. Check that the backend is running, then try again.",
    );
  } finally {
    pendingRequests.delete(controller);
  }
  if (!response.ok) {
    if (
      response.status === 401 &&
      revision === authRevision &&
      credential &&
      credential === basicAuthorization()
    ) {
      clearAuthorization();
      unauthorizedListeners.forEach((listener) => listener());
    }
    let message = text;
    try {
      const error = JSON.parse(text);
      message =
        error.detail || error.message || error.error || response.statusText;
    } catch {
      /* Spring also returns plain text errors. */
    }
    if (response.status >= 500)
      message =
        method === "DELETE"
          ? "The backend could not delete this entry. A world with characters or locations may need those entries removed first."
          : "The backend could not complete the request. Check the backend connection and logs, then retry.";
    throw new ApiError(
      typeof message === "string" && message
        ? message
        : `Request failed (${response.status}).`,
      response.status,
    );
  }
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
