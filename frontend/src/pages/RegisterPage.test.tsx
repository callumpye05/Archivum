// @vitest-environment jsdom
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import { RegisterPage } from "./RegisterPage";

let root: Root;
let host: HTMLDivElement;
beforeEach(async () => {
  vi.stubGlobal("IS_REACT_ACT_ENVIRONMENT", true);
  host = document.createElement("div");
  document.body.append(host);
  root = createRoot(host);
  await act(async () => root.render(<RegisterPage />));
});
afterEach(async () => {
  await act(async () => root.unmount());
  host.remove();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});
function field(name: string) {
  return host.querySelector<HTMLInputElement>(`[name="${name}"]`)!;
}
function fill() {
  field("userName").value = "  Archivist  ";
  field("email").value = "archivist@example.com";
  field("password").value = "test-only-password";
}
async function submit() {
  await act(async () => {
    host
      .querySelector("form")!
      .dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  });
}

it("sends the exact registration contract and stays on a success state with a login action", async () => {
  const fetch = vi.fn().mockResolvedValue(
    new Response(
      JSON.stringify({
        userName: "Archivist",
        email: "archivist@example.com",
      }),
      { status: 201 },
    ),
  );
  vi.stubGlobal("fetch", fetch);
  const store = vi.spyOn(Storage.prototype, "setItem");
  fill();
  const password = field("password");
  await submit();
  expect(fetch).toHaveBeenCalledWith(
    "/api/auth/register",
    expect.objectContaining({
      method: "POST",
      headers: {
        Accept: "application/json",
        "X-Requested-With": "XMLHttpRequest",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        userName: "Archivist",
        email: "archivist@example.com",
        password: "test-only-password",
      }),
    }),
  );
  expect(host.querySelector('[role="status"]')?.textContent).toBe(
    "Account created successfully.",
  );
  expect(host.querySelector("form")).toBeNull();
  expect(host.querySelector('button[type="button"]')?.textContent).toContain(
    "Continue to login",
  );
  expect(host.querySelector("a")).toBeNull();
  await act(async () =>
    host.querySelector<HTMLButtonElement>('button[type="button"]')!.click(),
  );
  expect(window.location.hash).toBe("#/login");
  expect(password.value).toBe("");
  expect(store).not.toHaveBeenCalled();
});

it.each(["empty", "blank username", "invalid email"])(
  "blocks %s input without a request",
  async (scenario) => {
    const fetch = vi.fn();
    vi.stubGlobal("fetch", fetch);
    if (scenario !== "empty") fill();
    if (scenario === "blank username") field("userName").value = "   ";
    if (scenario === "invalid email") field("email").value = "not-an-email";
    await submit();
    expect(fetch).not.toHaveBeenCalled();
  },
);

it("clears the password immediately, disables fields, and prevents duplicate in-flight requests", async () => {
  let finish!: (value: Response) => void;
  const fetch = vi.fn(
    () =>
      new Promise<Response>((resolve) => {
        finish = resolve;
      }),
  );
  vi.stubGlobal("fetch", fetch);
  fill();
  await submit();
  expect(field("password").value).toBe("");
  expect(host.querySelector("fieldset")?.disabled).toBe(true);
  expect(host.querySelector("button")?.disabled).toBe(true);
  expect(host.querySelector('[role="status"]')?.textContent).toContain(
    "Creating",
  );
  await submit();
  expect(fetch).toHaveBeenCalledTimes(1);
  await act(async () => finish(new Response("Conflict", { status: 409 })));
  expect(host.querySelector("button")?.disabled).toBe(false);
});

it.each([
  [409, "Username already exists", "This username is already taken"],
  [
    409,
    '{"message":"Email already exists"}',
    "This email is already registered",
  ],
  [409, "", "This username or email is already registered"],
  [400, "Duplicate email", "This email is already registered"],
  [
    400,
    '{"message":"Invalid password test-only-password"}',
    "Your details were not accepted",
  ],
  [500, "Internal details", "Registration could not be completed"],
])(
  "handles HTTP %s (%s), preserving only non-secret fields and allowing retry",
  async (status, body, message) => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(new Response(body, { status }))
      .mockResolvedValueOnce(
        new Response(
          '{"userName":"Archivist","email":"archivist@example.com"}',
        ),
      );
    vi.stubGlobal("fetch", fetch);
    fill();
    await submit();
    expect(host.querySelector('[role="alert"]')?.textContent).toContain(
      message,
    );
    expect(host.textContent).not.toContain("test-only-password");
    expect(field("userName").value).toBe("Archivist");
    expect(field("email").value).toBe("archivist@example.com");
    expect(field("password").value).toBe("");
    await submit();
    expect(fetch).toHaveBeenCalledTimes(1);
    field("password").value = "retry-password";
    await submit();
    expect(host.textContent).toContain("Account created successfully.");
  },
);

it("shows a recoverable network error without retaining the password", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn().mockRejectedValue(new TypeError("Failed to fetch")),
  );
  fill();
  await submit();
  expect(host.querySelector('[role="alert"]')?.textContent).toContain(
    "Could not reach Archivum",
  );
  expect(field("password").value).toBe("");
  expect(host.querySelector("button")?.disabled).toBe(false);
});
