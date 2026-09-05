const baseUrl = (import.meta.env.VITE_API_BASE_URL || "/api").replace(
  /\/$/,
  "",
);
export async function request<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method,
      headers:
        body === undefined
          ? { Accept: "application/json" }
          : { Accept: "application/json", "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(15000),
    });
  } catch {
    throw new Error(
      "Could not reach Archivum. Check that the backend is running, then try again.",
    );
  }
  const text = await response.text();
  if (!response.ok) {
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
    throw new Error(message || `Request failed (${response.status}).`);
  }
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
