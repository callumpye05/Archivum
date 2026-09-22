// @vitest-environment jsdom
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { beforeEach, afterEach, expect, it, vi } from "vitest";
import App from "./App";
import { clearAuthorization, request, signIn } from "./api/client";

let root: Root;
let host: HTMLDivElement;
const world = {
  worldId: 7,
  worldName: "Test world",
  worldDesc: "An isolated test fixture",
  createdAt: "2026-01-01T00:00:00Z",
};
const character = {
  characterId: 3,
  characterName: "Test person",
  characterSpecies: "Human",
  age: 0,
  characterNationality: "",
  characterDescription: "A description",
  createdAt: "2026-01-01T00:00:00Z",
  world,
};
const location = {
  id: 2,
  locationName: "Test place",
  locationType: "CITY",
  locationDescription: "A place description",
  createdAt: "2026-01-01T00:00:00Z",
  world,
};
const calls: {
  path: string;
  method: string;
  body: Record<string, unknown> | undefined;
}[] = [];
let failure = false;
let empty = false;
let authenticated = true;
let acceptLogin = false;

beforeEach(() => {
  clearAuthorization();
  vi.stubGlobal("IS_REACT_ACT_ENVIRONMENT", true);
  Object.defineProperty(HTMLDialogElement.prototype, "showModal", {
    configurable: true,
    value() {
      this.open = true;
    },
  });
  Object.defineProperty(HTMLDialogElement.prototype, "close", {
    configurable: true,
    value() {
      this.open = false;
    },
  });
  window.location.hash = "/";
  failure = false;
  empty = false;
  authenticated = true;
  acceptLogin = false;
  calls.length = 0;
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path: string, options: RequestInit) => {
      const method = options.method || "GET";
      calls.push({
        path,
        method,
        body: options.body ? JSON.parse(String(options.body)) : undefined,
      });
      if (path === "/api/auth/register")
        return new Response(
          JSON.stringify({ userName: "Archivist", email: "a@example.com" }),
        );
      if (
        acceptLogin &&
        new Headers(options.headers).get("Authorization") ===
          `Basic ${btoa("Archivist:password")}`
      )
        authenticated = true;
      if (!authenticated) return new Response("Unauthorized", { status: 401 });
      if (failure) return new Response("Save rejected", { status: 400 });
      if (method === "DELETE") return new Response(null, { status: 204 });
      let value: unknown = world;
      if (path === "/api/worlds") value = empty ? [] : [world];
      else if (path.endsWith("/characters"))
        value = method === "GET" ? [character] : character;
      else if (path.endsWith("/locations"))
        value = method === "GET" ? [location] : location;
      else if (path.endsWith("/characters/3")) value = character;
      else if (path.endsWith("/locations/2")) value = location;
      return new Response(JSON.stringify(value));
    }),
  );
  host = document.createElement("div");
  document.body.append(host);
  root = createRoot(host);
});
afterEach(async () => {
  await act(async () => root.unmount());
  host.remove();
  clearAuthorization();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});
async function render(hash = "/") {
  window.location.hash = hash;
  await act(async () => root.render(<App />));
}
async function click(label: string, scope: ParentNode = host) {
  const button = Array.from(scope.querySelectorAll("button")).find(
    (button) => button.textContent?.trim() === label,
  );
  expect(button, `button ${label}`).toBeTruthy();
  await act(async () => button!.click());
}
async function input(id: string, value: string) {
  const field = host.querySelector<
    HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
  >(`#${id}`)!;
  const prototype =
    field instanceof HTMLTextAreaElement
      ? HTMLTextAreaElement.prototype
      : field instanceof HTMLSelectElement
        ? HTMLSelectElement.prototype
        : HTMLInputElement.prototype;
  await act(async () => {
    Object.getOwnPropertyDescriptor(prototype, "value")!.set!.call(
      field,
      value,
    );
    field.dispatchEvent(new Event("input", { bubbles: true }));
    field.dispatchEvent(new Event("change", { bubbles: true }));
  });
}
async function submit() {
  await act(async () =>
    host
      .querySelector("form")!
      .dispatchEvent(new Event("submit", { bubbles: true, cancelable: true })),
  );
}

async function login() {
  await input("login-username", "Archivist");
  await input("login-password", "password");
  await submit();
}
async function renderSignedIn(hash = "/") {
  await render(hash);
  await login();
}
async function openAccount() {
  await act(async () =>
    host.querySelector<HTMLButtonElement>(".account-trigger")!.click(),
  );
  await click("Account", host.querySelector("#account-actions")!);
}
it.each(["/", "/worlds/7", "/register"])(
  "starts guest at %s without probing even if browser access would succeed",
  async (hash) => {
    await render(hash);
    expect(host.querySelector(".auth-landing")).toBeTruthy();
    expect(host.querySelector(".sidebar")).toBeNull();
    expect(host.querySelectorAll(".auth-navigation button")).toHaveLength(2);
    expect(host.querySelector(".auth-navigation a")).toBeNull();
    expect(calls).toEqual([]);
  },
);
it.each([false, true])(
  "requires explicit login after remount, with sign out = %s",
  async (signOut) => {
    const storage = vi.spyOn(Storage.prototype, "setItem");
    await renderSignedIn();
    expect(host.querySelector(".account-trigger")?.textContent).toContain(
      "Archivist",
    );
    if (signOut) {
      await openAccount();
      await click("Sign out", host.querySelector("dialog")!);
      expect(
        host.querySelector(".sidebar, .account-control, dialog"),
      ).toBeNull();
      expect(host.querySelector(".auth-landing")).toBeTruthy();
      expect(window.location.hash).toBe("#/login");
    }
    const count = calls.length;
    await act(async () => root.unmount());
    root = createRoot(host);
    await render("/worlds/7");
    expect(host.querySelector(".auth-landing")).toBeTruthy();
    expect(host.querySelector(".sidebar")).toBeNull();
    expect(calls).toHaveLength(count);
    await request("/worlds");
    expect(
      new Headers(vi.mocked(fetch).mock.calls.at(-1)![1]?.headers).has(
        "Authorization",
      ),
    ).toBe(false);
    expect(storage).not.toHaveBeenCalled();
  },
);
it("renders change-oriented settings and restores focus after closing", async () => {
  await renderSignedIn();
  const trigger = host.querySelector<HTMLButtonElement>(".account-trigger")!;
  await openAccount();
  expect(host.querySelector("dialog")?.textContent).toContain(
    "Account settings",
  );
  expect(host.querySelector<HTMLInputElement>("#account-username")!.value).toBe(
    "Archivist",
  );
  expect(host.querySelector<HTMLInputElement>("#account-email")!.value).toBe(
    "",
  );
  expect(host.querySelector<HTMLInputElement>("#account-password")!.type).toBe(
    "password",
  );
  expect(host.querySelector<HTMLInputElement>("#account-password")!.value).toBe(
    "",
  );
  expect(calls.some((call) => call.path.includes("/me"))).toBe(false);
  await act(async () =>
    host
      .querySelector<HTMLButtonElement>('[aria-label="Close dialog"]')!
      .click(),
  );
  expect(document.activeElement).toBe(trigger);
  await act(async () => trigger.click());
  await act(async () =>
    trigger.dispatchEvent(
      new KeyboardEvent("keydown", { key: "Escape", bubbles: true }),
    ),
  );
  expect(host.querySelector("#account-actions")).toBeNull();
  await act(async () => trigger.click());
  await act(async () =>
    document.body.dispatchEvent(new Event("pointerdown", { bubbles: true })),
  );
  expect(host.querySelector("#account-actions")).toBeNull();
});
it.each([false, true])(
  "updates identity with the exact DTO and coherent Basic credentials; password change = %s",
  async (changePassword) => {
    await renderSignedIn();
    await openAccount();
    await input("account-username", "Renamed");
    if (changePassword) {
      await input("account-email", "new@example.com");
      await input("account-password", "new-password");
      await input("account-confirmation", "new-password");
    }
    await submit();
    expect(calls).toContainEqual({
      path: "/api/users/me",
      method: "PUT",
      body: changePassword
        ? {
            userName: "Renamed",
            email: "new@example.com",
            password: "new-password",
          }
        : { userName: "Renamed" },
    });
    expect(host.querySelector(".account-trigger")?.textContent).toContain(
      "Renamed",
    );
    expect(host.querySelector("dialog")?.textContent).toContain(
      "Account updated",
    );
    expect(
      host.querySelector<HTMLInputElement>("#account-password")!.value,
    ).toBe("");
    expect(
      host.querySelector<HTMLInputElement>("#account-confirmation")!.value,
    ).toBe("");
    await request("/worlds");
    expect(
      new Headers(vi.mocked(fetch).mock.calls.at(-1)![1]?.headers).get(
        "Authorization",
      ),
    ).toBe(
      "Basic " +
        btoa("Renamed:" + (changePassword ? "new-password" : "password")),
    );
  },
);
it("validates password confirmation before sending an update", async () => {
  await renderSignedIn();
  await openAccount();
  await input("account-password", "new-password");
  await input("account-confirmation", "different-password");
  await submit();
  expect(host.querySelector('[role="alert"]')?.textContent).toContain(
    "do not match",
  );
  expect(calls.some((call) => call.method === "PUT")).toBe(false);
});
it("preserves identity after a rejected update and clears password fields", async () => {
  await renderSignedIn();
  await openAccount();
  await input("account-username", "Rejected");
  await input("account-password", "new-password");
  await input("account-confirmation", "new-password");
  failure = true;
  await submit();
  expect(host.querySelector('[role="alert"]')?.textContent).toContain(
    "Unable to save",
  );
  expect(host.querySelector(".account-trigger")?.textContent).toContain(
    "Archivist",
  );
  expect(host.querySelector<HTMLInputElement>("#account-password")!.value).toBe(
    "",
  );
  failure = false;
  await request("/worlds");
  expect(
    new Headers(vi.mocked(fetch).mock.calls.at(-1)![1]?.headers).get(
      "Authorization",
    ),
  ).toBe("Basic " + btoa("Archivist:password"));
});

it("cancels pending requests and prevents a late sign-in from restoring cleared credentials", async () => {
  let finish!: (response: Response) => void;
  const fetch = vi
    .fn()
    .mockImplementationOnce(
      () =>
        new Promise<Response>((resolve) => {
          finish = resolve;
        }),
    )
    .mockResolvedValue(new Response("[]"));
  vi.stubGlobal("fetch", fetch);
  const pending = signIn("Archivist", "password");
  const signal = fetch.mock.calls[0][1].signal as AbortSignal;
  clearAuthorization();
  expect(signal.aborted).toBe(true);
  finish(new Response("[]"));
  await expect(pending).rejects.toThrow("Sign-in was cancelled");
  await request("/worlds");
  expect(new Headers(fetch.mock.calls[1][1].headers).has("Authorization")).toBe(
    false,
  );
});

it("switches forms without advancing the panorama and redirects legacy routes", async () => {
  authenticated = false;
  await render("/register");
  const landing = host.querySelector(".auth-landing");
  expect(
    host.querySelector(".auth-register-panel")?.hasAttribute("inert"),
  ).toBe(false);
  expect(host.querySelector(".auth-login-panel")?.hasAttribute("inert")).toBe(
    true,
  );
  expect(host.querySelector(".sidebar")).toBeNull();
  const image = host.querySelector(".is-active img")?.getAttribute("src");
  await act(async () => {
    window.location.hash = "/explore";
    window.dispatchEvent(new HashChangeEvent("hashchange"));
  });
  expect(window.location.hash).toBe("#/");
  expect(host.querySelector(".auth-landing")).toBe(landing);
  expect(host.querySelector(".auth-login-panel")?.hasAttribute("inert")).toBe(
    false,
  );
  expect(
    host.querySelector(".auth-register-panel")?.hasAttribute("inert"),
  ).toBe(true);
  expect(host.querySelector(".is-active img")?.getAttribute("src")).toBe(image);
  expect(host.querySelector('[aria-roledescription="carousel"]')).toBeNull();
  expect(calls).toEqual([]);
});

it.each(["/", "/login", "/register", "/explore", "/panorama"])(
  "hides all guest controls for an authenticated visitor at %s",
  async (hash) => {
    await render();
    await login();
    await act(async () => {
      window.location.hash = hash;
      window.dispatchEvent(new HashChangeEvent("hashchange"));
    });
    expect(host.textContent).toContain("Test world");
    expect(
      host.querySelector(
        '.auth-landing, a[href="#/register"], a[href="#/login"], a[href="#/explore"]',
      ),
    ).toBeNull();
    expect(window.location.hash).toBe("#/");
  },
);

it("logs in through existing HTTP Basic, handles rejection, and resumes a world deep link", async () => {
  authenticated = false;
  acceptLogin = true;
  const storage = vi.spyOn(Storage.prototype, "setItem");
  await render("/worlds/7");
  const username = host.querySelector<HTMLInputElement>("#login-username")!;
  const password = host.querySelector<HTMLInputElement>("#login-password")!;
  username.value = "Archivist";
  password.value = "wrong";
  await submit();
  expect(host.querySelector('[role="alert"]')?.textContent).toContain(
    "wasn't recognized",
  );
  expect(password.value).toBe("");
  password.value = "password";
  await submit();
  expect(host.textContent).toContain("People of this world");
  expect(window.location.hash).toBe("#/worlds/7");
  expect(host.querySelector(".auth-landing")).toBeNull();
  expect(calls.some((call) => call.path.includes("auth/login"))).toBe(false);
  expect(storage).not.toHaveBeenCalled();
  storage.mockRestore();
  // A later protected request returning 401 removes the workspace immediately.
  authenticated = false;
  acceptLogin = false;
  await act(async () => {
    await request("/worlds").catch(() => {});
  });
  expect(host.querySelector(".auth-landing")).toBeTruthy();
  expect(host.querySelector(".sidebar")).toBeNull();
});

it("keeps a newly registered visitor in the shared guest flow until login", async () => {
  authenticated = false;
  await render("/register");
  const panel = host.querySelector(".auth-register-panel")!;
  (panel.querySelector('[name="userName"]') as HTMLInputElement).value =
    "Archivist";
  (panel.querySelector('[name="email"]') as HTMLInputElement).value =
    "a@example.com";
  (panel.querySelector('[name="password"]') as HTMLInputElement).value =
    "password";
  await act(async () => {
    panel
      .querySelector("form")!
      .dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  });
  expect(panel.textContent).toContain("Account created successfully.");
  expect(panel.querySelector('button[type="button"]')?.textContent).toContain(
    "Continue to login",
  );
  expect(host.querySelector(".sidebar")).toBeNull();
});

it("loads worlds and keeps selection in a reloadable URL", async () => {
  await renderSignedIn();
  expect(host.textContent).toContain("Test world");
  expect(host.querySelector('a[href="#/worlds/7"]')).toBeTruthy();
  await render("/worlds/7");
  await act(async () =>
    window.dispatchEvent(new HashChangeEvent("hashchange")),
  );
  expect(host.textContent).toContain("People of this world");
  expect(calls.map((call) => call.path)).toContain("/api/worlds/7/characters");
  expect(calls.map((call) => call.path)).toContain("/api/worlds/7/locations");
});

it("shows empty and recoverable error states", async () => {
  empty = true;
  await renderSignedIn();
  expect(host.textContent).toContain("Every story begins with a world");
  failure = true;
  await act(async () => {
    window.location.hash = "/worlds/7";
    window.dispatchEvent(new HashChangeEvent("hashchange"));
  });
  expect(host.querySelector('[role="alert"]')).toBeTruthy();
  failure = false;
  await click("Try again");
  expect(host.textContent).toContain("People of this world");
});

it("creates a world using exact DTO fields and selects the result", async () => {
  await renderSignedIn();
  await click("＋ Create world");
  await input("worldName", "New test world");
  await input("worldDesc", "New description");
  await submit();
  expect(calls).toContainEqual({
    path: "/api/worlds/create",
    method: "POST",
    body: { worldName: "New test world", worldDesc: "New description" },
  });
  expect(window.location.hash).toBe("#/worlds/7");
});

it("retains form data after failure and permits a retry", async () => {
  await renderSignedIn();
  await click("Edit");
  await input("worldName", "Changed name");
  failure = true;
  await submit();
  expect(host.textContent).toContain("Save rejected");
  expect(host.querySelector<HTMLInputElement>("#worldName")!.value).toBe(
    "Changed name",
  );
  failure = false;
  await submit();
  expect(host.querySelector("dialog")).toBeNull();
  expect(calls).toContainEqual({
    path: "/api/worlds/7",
    method: "PUT",
    body: { worldName: "Changed name", worldDesc: world.worldDesc },
  });
});

it("requires explicit confirmation and allows cancellation of world deletion", async () => {
  await renderSignedIn();
  await click("Delete");
  await click("Keep entry");
  expect(calls.some((call) => call.method === "DELETE")).toBe(false);
  await click("Delete");
  await click("Delete permanently");
  expect(calls).toContainEqual({
    path: "/api/worlds/7",
    method: "DELETE",
    body: undefined,
  });
});

it("creates a character with numeric age and validates input bounds", async () => {
  await renderSignedIn("/worlds/7");
  await click("＋ Create character");
  const age = host.querySelector<HTMLInputElement>("#age")!;
  expect(age.checkValidity()).toBe(false);
  await input("age", "-1");
  expect(age.checkValidity()).toBe(false);
  await input("age", "1.5");
  expect(age.checkValidity()).toBe(false);
  await input("age", "2147483648");
  expect(age.checkValidity()).toBe(false);
  await input("age", "0");
  expect(age.checkValidity()).toBe(true);
  expect(
    host.querySelector<HTMLInputElement>("#characterName")!.maxLength,
  ).toBe(100);
  expect(
    host.querySelector<HTMLTextAreaElement>("#characterDescription")!.maxLength,
  ).toBe(500);
  await input("characterName", "New person");
  await input("characterSpecies", "Human");
  await submit();
  expect(calls).toContainEqual({
    path: "/api/worlds/7/characters",
    method: "POST",
    body: {
      characterName: "New person",
      characterSpecies: "Human",
      age: 0,
      characterNationality: "",
      characterDescription: "",
    },
  });
});

it("views, edits and deletes a character within its world", async () => {
  await renderSignedIn("/worlds/7");
  await click("View");
  expect(calls.map((call) => call.path)).toContain(
    "/api/worlds/7/characters/3",
  );
  expect(host.querySelector("dialog")!.textContent).toContain("Test world");
  await click("×");
  await click("Edit");
  await input("characterDescription", "Updated");
  await submit();
  expect(calls).toContainEqual({
    path: "/api/characters/3",
    method: "PUT",
    body: {
      characterName: character.characterName,
      characterSpecies: "Human",
      age: 0,
      characterNationality: "",
      characterDescription: "Updated",
    },
  });
  await click("Delete");
  await click("Delete permanently");
  expect(calls).toContainEqual({
    path: "/api/characters/3",
    method: "DELETE",
    body: undefined,
  });
});

it("creates, views, edits and deletes locations with the request/response description mapping", async () => {
  await renderSignedIn("/worlds/7");
  const locationsTab = Array.from(host.querySelectorAll("button")).find(
    (button) => button.textContent?.startsWith("Locations"),
  )!;
  await act(async () => locationsTab.click());
  await click("＋ Create location");
  expect(
    Array.from(host.querySelectorAll("option")).map((option) => option.value),
  ).toEqual(["CITY", "VILLAGE", "DISTRICT", "LANDMARK", "FACILITY"]);
  await input("locationName", "New place");
  await input("locationType", "LANDMARK");
  await input("locationDesc", "New description");
  await submit();
  expect(calls).toContainEqual({
    path: "/api/worlds/7/locations",
    method: "POST",
    body: {
      locationName: "New place",
      locationType: "LANDMARK",
      locationDesc: "New description",
    },
  });
  await click("View");
  expect(calls.map((call) => call.path)).toContain("/api/worlds/7/locations/2");
  expect(host.querySelector("dialog")!.textContent).toContain(
    "A place description",
  );
  await click("×");
  await click("Edit");
  expect(host.querySelector<HTMLTextAreaElement>("#locationDesc")!.value).toBe(
    location.locationDescription,
  );
  await input("locationDesc", "Edited place");
  await submit();
  expect(calls).toContainEqual({
    path: "/api/locations/2",
    method: "PUT",
    body: {
      locationName: "Test place",
      locationType: "CITY",
      locationDesc: "Edited place",
    },
  });
  await click("Delete");
  await click("Delete permanently");
  expect(calls).toContainEqual({
    path: "/api/locations/2",
    method: "DELETE",
    body: undefined,
  });
});

it("does not send unchanged account values and enforces password length", async () => {
  await renderSignedIn();
  await openAccount();
  await submit();
  expect(host.querySelector('dialog [role="status"]')?.textContent).toContain(
    "No changes",
  );
  await input("account-password", "short");
  await input("account-confirmation", "short");
  await submit();
  expect(host.querySelector('[role="alert"]')?.textContent).toContain(
    "8 and 100",
  );
  expect(calls.some((call) => call.method === "PUT")).toBe(false);
});

it("auth action buttons retain hash navigation, back/forward, and independent panorama state", async () => {
  await render("/login");
  const navigation = host.querySelector(".auth-navigation")!;
  const image = host.querySelector(".is-active img");
  expect(host.querySelector(".auth-landing a")).toBeNull();
  expect(
    Array.from(navigation.querySelectorAll("button")).every(
      (button) => button.type === "button",
    ),
  ).toBe(true);
  async function navigate(action: () => void) {
    await act(async () => {
      const changed = new Promise((resolve) =>
        window.addEventListener("hashchange", resolve, { once: true }),
      );
      action();
      await changed;
    });
  }
  await navigate(() =>
    (navigation.querySelectorAll("button")[1] as HTMLButtonElement).click(),
  );
  expect(window.location.hash).toBe("#/register");
  expect(
    host.querySelector(".auth-landing")?.classList.contains("is-register"),
  ).toBe(true);
  expect(document.activeElement).toBe(
    host.querySelector(".auth-register-panel"),
  );
  expect(host.querySelector(".is-active img")).toBe(image);
  await navigate(() => window.history.back());
  expect(window.location.hash).toBe("#/login");
  expect(
    host.querySelector(".auth-landing")?.classList.contains("is-register"),
  ).toBe(false);
  await navigate(() => window.history.forward());
  expect(window.location.hash).toBe("#/register");
  await navigate(() =>
    (navigation.querySelectorAll("button")[0] as HTMLButtonElement).click(),
  );
  expect(window.location.hash).toBe("#/login");
  expect(host.querySelector(".is-active img")).toBe(image);
});
