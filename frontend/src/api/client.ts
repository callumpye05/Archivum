import type { RegisteredUser, UpdateUserDto } from "../types";

const baseUrl = (import.meta.env.VITE_API_BASE_URL || "/api").replace(
  /\/$/,
  "",
);
export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}
// Cookies belong to the browser. This counter only ignores responses from an
// earlier session; no password or session identifier is kept in JavaScript.
let authRevision = 0;
const pendingRequests = new Set<AbortController>();
function cancelSessionRequests() {
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
  await request<void>(
    "/auth/login",
    "POST",
    new URLSearchParams({ username, password }),
  );
  cancelSessionRequests();
  return getCurrentUser();
}
export const getCurrentUser = () => request<RegisteredUser>("/users/me");

export async function signOut() {
  await request<void>("/auth/logout", "POST");
  cancelSessionRequests();
}

export async function updateCurrentUser(data: UpdateUserDto): Promise<string> {
  const user = await request<RegisteredUser>("/users/me", "PUT", data);
  return user.userName;
}

interface CsrfToken {
  headerName: string;
  token: string;
}

export async function request<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  const revision = authRevision;
  const headers: Record<string, string> = { Accept: "application/json" };
  // Fetch before every write. This also picks up tokens replaced by login,
  // logout, an expired session, or another tab, without a token cache.
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    const csrf = await request<CsrfToken>("/auth/csrf");
    headers[csrf.headerName] = csrf.token;
  }
  if (revision !== authRevision && !path.startsWith("/auth/")) {
    throw new Error("Your session changed. Please try again.");
  }
  if (body !== undefined) {
    headers["Content-Type"] =
      body instanceof URLSearchParams
        ? "application/x-www-form-urlencoded"
        : "application/json";
  }
  const controller = new AbortController();
  // A startup 401 must not cancel public registration or verification requests.
  if (!path.startsWith("/auth/")) pendingRequests.add(controller);
  let response: Response;
  let text: string;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method,
      credentials: "include",
      headers,
      body:
        body === undefined
          ? undefined
          : body instanceof URLSearchParams
            ? body.toString()
            : JSON.stringify(body),
      signal: AbortSignal.any([controller.signal, AbortSignal.timeout(15000)]),
    });
    text = await response.text();
  } catch {
    throw new Error(
      "Could not reach Archivum. Check your connection and try again.",
    );
  } finally {
    pendingRequests.delete(controller);
  }
  if (!response.ok) {
    if (
      response.status === 401 &&
      revision === authRevision &&
      !path.startsWith("/auth/")
    ) {
      cancelSessionRequests();
      unauthorizedListeners.forEach((listener) => listener());
    }
    let message = text;
    let code: string | undefined;
    try {
      const error = JSON.parse(text);
      if (typeof error.code === "string") code = error.code;
      message =
        error.detail || error.message || error.error || response.statusText;
    } catch {
      /* Spring also returns plain text errors. */
    }
    if (response.status >= 500)
      message =
        method === "DELETE"
          ? "Unable to delete this entry. Please try again."
          : "Archivum could not complete the request. Please try again.";
    throw new ApiError(
      typeof message === "string" && message
        ? message
        : `Request failed (${response.status}).`,
      response.status,
      code,
    );
  }
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
