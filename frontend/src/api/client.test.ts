import { afterEach, expect, it, vi } from "vitest";
import {
  getCurrentUser,
  onUnauthorized,
  request,
  signIn,
  signOut,
} from "./client";
import { registerUser, updateUser, verifyEmail } from "./index";

afterEach(() => vi.unstubAllGlobals());

it("uses cookies, form login, the server's CSRF header and new tokens after login/logout", async () => {
  let token = "guest-token";
  let loggedIn = false;
  const fetch = vi.fn(async (path: string, options: RequestInit) => {
    expect(options.credentials).toBe("include");
    const headers = new Headers(options.headers);
    expect(headers.has("Authorization")).toBe(false);
    if (path === "/api/auth/csrf")
      return Response.json({ headerName: "X-CSRF-TOKEN", token });
    if (options.method !== "GET")
      expect(headers.get("X-CSRF-TOKEN")).toBe(token);
    if (path === "/api/auth/login") {
      expect(headers.get("Content-Type")).toBe(
        "application/x-www-form-urlencoded",
      );
      expect(
        Object.fromEntries(new URLSearchParams(String(options.body))),
      ).toEqual({ username: "a+b@example.com", password: "p&= +:word" });
      loggedIn = true;
      token = "signed-in-token";
      return new Response(null);
    }
    if (path === "/api/auth/logout") {
      loggedIn = false;
      token = "signed-out-token";
      return new Response(null, { status: 204 });
    }
    if (path === "/api/users/me")
      return loggedIn
        ? Response.json({ userName: "Archivist", email: "a+b@example.com" })
        : new Response(null, { status: 401 });
    return new Response(null);
  });
  vi.stubGlobal("fetch", fetch);
  await expect(signIn("a+b@example.com", "p&= +:word")).resolves.toMatchObject({
    userName: "Archivist",
  });
  await request("/worlds/create", "POST", { worldName: "Test" });
  await signOut();
  await registerUser({
    userName: "New",
    email: "new@example.com",
    password: "new-password",
  });
  await verifyEmail("a".repeat(64));
  await expect(getCurrentUser()).rejects.toMatchObject({ status: 401 });
  expect(fetch.mock.calls.map(([path]) => path)).toEqual([
    "/api/auth/csrf",
    "/api/auth/login",
    "/api/users/me",
    "/api/auth/csrf",
    "/api/worlds/create",
    "/api/auth/csrf",
    "/api/auth/logout",
    "/api/auth/csrf",
    "/api/auth/register",
    "/api/auth/csrf",
    "/api/auth/verify-email",
    "/api/users/me",
  ]);
});

it("notifies about protected 401s but keeps rejected login in the form", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path: string) =>
      path === "/api/auth/csrf"
        ? Response.json({ headerName: "X-CSRF-TOKEN", token: "csrf" })
        : new Response(null, { status: 401 }),
    ),
  );
  const listener = vi.fn();
  const unsubscribe = onUnauthorized(listener);
  try {
    await expect(signIn("name", "wrong")).rejects.toMatchObject({
      status: 401,
    });
    expect(listener).not.toHaveBeenCalled();
    await expect(getCurrentUser()).rejects.toMatchObject({ status: 401 });
    expect(listener).toHaveBeenCalledTimes(1);
  } finally {
    unsubscribe();
  }
});

it.each(["INVALID_CREDENTIALS", "EMAIL_NOT_VERIFIED"])(
  "preserves the login error code %s without requesting the current user",
  async (code) => {
    const fetch = vi.fn(async (path: string) =>
      path === "/api/auth/csrf"
        ? Response.json({ headerName: "X-CSRF-TOKEN", token: "csrf" })
        : Response.json({ code }, { status: 401 }),
    );
    vi.stubGlobal("fetch", fetch);
    await expect(signIn("Archivist", "password")).rejects.toMatchObject({
      status: 401,
      code,
    });
    expect(fetch.mock.calls.map(([path]) => path)).toEqual([
      "/api/auth/csrf",
      "/api/auth/login",
    ]);
  },
);

it("gets another CSRF token on a manual retry without automatically replaying a write", async () => {
  let token = "old";
  const fetch = vi.fn(async (path: string) => {
    if (path === "/api/auth/csrf")
      return Response.json({ headerName: "X-CSRF-TOKEN", token });
    return token === "old"
      ? new Response(null, { status: 403 })
      : new Response(null);
  });
  vi.stubGlobal("fetch", fetch);
  await expect(verifyEmail("a".repeat(64))).rejects.toMatchObject({
    status: 403,
  });
  expect(fetch).toHaveBeenCalledTimes(2);
  token = "new";
  await verifyEmail("a".repeat(64));
  expect(fetch).toHaveBeenCalledTimes(4);
});

it("uses the returned user after account changes without retaining credentials", async () => {
  const fetch = vi.fn(async (path: string) =>
    Response.json(
      path === "/api/auth/csrf"
        ? { headerName: "X-CSRF-TOKEN", token: "csrf" }
        : { userName: "Renamed", email: "new@example.com" },
    ),
  );
  vi.stubGlobal("fetch", fetch);
  await expect(updateUser({ userName: "Renamed" })).resolves.toBe("Renamed");
  expect(fetch).toHaveBeenCalledWith(
    "/api/users/me",
    expect.objectContaining({
      method: "PUT",
      body: '{"userName":"Renamed"}',
      credentials: "include",
    }),
  );
});

it("ignores an old request's late 401 after a new login", async () => {
  let finish!: (response: Response) => void;
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path: string) => {
      if (path === "/api/worlds")
        return new Promise<Response>((resolve) => {
          finish = resolve;
        });
      if (path === "/api/auth/csrf")
        return Response.json({ headerName: "X-CSRF-TOKEN", token: "csrf" });
      if (path === "/api/users/me")
        return Response.json({ userName: "Archivist", email: "a@example.com" });
      return new Response(null);
    }),
  );
  const listener = vi.fn();
  const unsubscribe = onUnauthorized(listener);
  try {
    const oldRequest = request("/worlds");
    await signIn("Archivist", "password");
    finish(new Response(null, { status: 401 }));
    await expect(oldRequest).rejects.toMatchObject({ status: 401 });
    expect(listener).not.toHaveBeenCalled();
  } finally {
    unsubscribe();
  }
});

it("does not send a pending write if logout finishes while its CSRF token is loading", async () => {
  let finish!: (response: Response) => void;
  const fetch = vi
    .fn()
    .mockImplementationOnce(
      () =>
        new Promise<Response>((resolve) => {
          finish = resolve;
        }),
    )
    .mockImplementation(async (path: string) =>
      path === "/api/auth/csrf"
        ? Response.json({ headerName: "X-CSRF-TOKEN", token: "csrf" })
        : new Response(null),
    );
  vi.stubGlobal("fetch", fetch);
  const write = request("/worlds/7", "DELETE");
  await signOut();
  finish(Response.json({ headerName: "X-CSRF-TOKEN", token: "old-csrf" }));
  await expect(write).rejects.toThrow("Your session changed");
  expect(fetch.mock.calls.some(([path]) => path === "/api/worlds/7")).toBe(
    false,
  );
});

it("does not send a write when fetching CSRF fails", async () => {
  const fetch = vi.fn(async () => new Response(null, { status: 503 }));
  vi.stubGlobal("fetch", fetch);
  await expect(signOut()).rejects.toMatchObject({ status: 503 });
  expect(fetch).toHaveBeenCalledExactlyOnceWith(
    "/api/auth/csrf",
    expect.objectContaining({ credentials: "include" }),
  );
});
