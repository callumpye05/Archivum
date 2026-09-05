// @vitest-environment jsdom
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { beforeEach, afterEach, expect, it, vi } from "vitest";
import App from "./App";

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

beforeEach(() => {
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
  vi.unstubAllGlobals();
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

it("loads worlds and keeps selection in a reloadable URL", async () => {
  await render();
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
  await render();
  expect(host.textContent).toContain("Every story begins with a world");
  await act(async () => root.unmount());
  root = createRoot(host);
  failure = true;
  await render();
  expect(host.querySelector('[role="alert"]')).toBeTruthy();
  failure = false;
  empty = false;
  await click("Try again");
  expect(host.textContent).toContain("Test world");
});

it("creates a world using exact DTO fields and selects the result", async () => {
  await render();
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
  await render();
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
  await render();
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
  await render("/worlds/7");
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
  await render("/worlds/7");
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
  await render("/worlds/7");
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
