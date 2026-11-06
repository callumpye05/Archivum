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
export class UnconfirmedWriteError extends Error {
  constructor() {
    super(
      "Could not confirm whether your changes were saved. Check your connection and refresh the collection before retrying to avoid creating a duplicate.",
    );
    this.name = "UnconfirmedWriteError";
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

export async function updateCurrentUser(
  data: UpdateUserDto,
): Promise<RegisteredUser> {
  return request<RegisteredUser>("/users/me", "PUT", data);
}

interface CsrfToken {
  headerName: string;
  token: string;
}

export async function request<T>(
  path: string,
  method = "GET",
  body?: unknown,
  parse?: (value: unknown) => T,
): Promise<T> {
  const revision = authRevision;
  const isWrite = !["GET", "HEAD", "OPTIONS"].includes(method);
  const headers: Record<string, string> = { Accept: "application/json" };
  // Fetch before every write. This also picks up tokens replaced by login,
  // logout, an expired session, or another tab, without a token cache.
  if (isWrite) {
    const csrf = await request<CsrfToken>("/auth/csrf");
    if (
      !csrf ||
      typeof csrf.headerName !== "string" ||
      !csrf.headerName.trim() ||
      typeof csrf.token !== "string" ||
      !csrf.token.trim()
    )
      throw new Error("Could not prepare a secure request. Please try again.");
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
    if (revision !== authRevision && !path.startsWith("/auth/"))
      throw new Error("Your session changed. Please try again.");
    if (isWrite) throw new UnconfirmedWriteError();
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
    const fallback =
      response.status === 401
        ? "Your sign-in is no longer valid. Log in again."
        : response.status === 403
          ? "This request was not allowed. Your security token may have expired. Try again, or sign in again if the problem continues."
          : response.status === 404
            ? "This entry is no longer available or you do not have access to it."
            : `Request failed (${response.status}). Please try again.`;
    let message = fallback;
    let code: string | undefined;
    try {
      const error = JSON.parse(text);
      if (typeof error?.code === "string") code = error.code;
      message =
        [error?.errorMessage, error?.detail, error?.message, error?.error].find(
          (value) => typeof value === "string" && value.trim(),
        ) || fallback;
    } catch {
      // Preserve legacy plain text, but never show a proxy's HTML error page.
      if (text.trim() && !text.includes("<")) message = text;
    }
    if (response.status === 403) message = fallback;
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
  if (revision !== authRevision && !path.startsWith("/auth/"))
    throw new Error("Your session changed. Please try again.");
  try {
    if (parse) return parse(text ? JSON.parse(text) : undefined);
    if (!text) return undefined as T;
    return JSON.parse(text) as T;
  } catch {
    if (isWrite) throw new UnconfirmedWriteError();
    throw new Error(
      "Archivum returned an unexpected response. Please try again.",
    );
  }
}
