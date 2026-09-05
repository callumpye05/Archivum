import { afterEach, describe, expect, it, vi } from "vitest";
import * as api from "./index";
import { request } from "./client";

afterEach(() => vi.unstubAllGlobals());
describe("API contract", () => {
  it.each([
    ["world list", () => api.getWorlds(), "/worlds"],
    ["world", () => api.getWorld(7), "/worlds/7"],
    [
      "world characters",
      () => api.getCharactersByWorld(7),
      "/worlds/7/characters",
    ],
    ["character", () => api.getCharacter(3), "/characters/3"],
    [
      "scoped character",
      () => api.getCharacterByWorld(7, 3),
      "/worlds/7/characters/3",
    ],
    [
      "world locations",
      () => api.getLocationsByWorld(7),
      "/worlds/7/locations",
    ],
    ["location", () => api.getLocation(2), "/locations/2"],
    [
      "scoped location",
      () => api.getLocationByWorld(7, 2),
      "/worlds/7/locations/2",
    ],
  ] as const)("reads %s from the existing route", async (_name, call, path) => {
    const fetch = vi.fn().mockResolvedValue(new Response("[]"));
    vi.stubGlobal("fetch", fetch);
    await call();
    expect(fetch).toHaveBeenCalledWith(
      `/api${path}`,
      expect.objectContaining({ method: "GET", body: undefined }),
    );
  });
  it("sends locationDesc rather than the response field locationDescription", async () => {
    const dto = {
      locationName: "Test place",
      locationDesc: "Test description",
      locationType: "CITY" as const,
    };
    const fetch = vi
      .fn()
      .mockImplementation(() => Promise.resolve(new Response("{}")));
    vi.stubGlobal("fetch", fetch);
    await api.createLocation(7, dto);
    await api.updateLocation(2, dto);
    expect(fetch).toHaveBeenNthCalledWith(
      1,
      "/api/worlds/7/locations",
      expect.objectContaining({ method: "POST", body: JSON.stringify(dto) }),
    );
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      "/api/locations/2",
      expect.objectContaining({ method: "PUT", body: JSON.stringify(dto) }),
    );
  });
  it("uses the special world create route and supports partial updates", async () => {
    const fetch = vi
      .fn()
      .mockImplementation(() => Promise.resolve(new Response("{}")));
    vi.stubGlobal("fetch", fetch);
    await api.createWorld({ worldName: "Test", worldDesc: "" });
    await api.updateWorld(7, { worldDesc: "" });
    expect(fetch).toHaveBeenNthCalledWith(
      1,
      "/api/worlds/create",
      expect.objectContaining({
        method: "POST",
        body: '{"worldName":"Test","worldDesc":""}',
      }),
    );
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      "/api/worlds/7",
      expect.objectContaining({ method: "PUT", body: '{"worldDesc":""}' }),
    );
  });
  it.each([200, 204])(
    "accepts empty DELETE responses with status %i",
    async (status) => {
      const fetch = vi
        .fn()
        .mockImplementation(() =>
          Promise.resolve(new Response(null, { status })),
        );
      vi.stubGlobal("fetch", fetch);
      await expect(api.deleteWorld(7)).resolves.toBeUndefined();
      await expect(api.deleteCharacter(3)).resolves.toBeUndefined();
      await expect(api.deleteLocation(2)).resolves.toBeUndefined();
      expect(fetch.mock.calls.map((call) => call[0])).toEqual([
        "/api/worlds/7",
        "/api/characters/3",
        "/api/locations/2",
      ]);
      expect(
        fetch.mock.calls.every((call) => call[1].method === "DELETE"),
      ).toBe(true);
    },
  );
  it("shows Spring plain-text not-found responses", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValue(
          new Response("World was not found", { status: 404 }),
        ),
    );
    await expect(api.getWorld(7)).rejects.toThrow("World was not found");
  });
  it("shows JSON validation errors", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValue(
          new Response('{"message":"Age must be non-negative"}', {
            status: 400,
          }),
        ),
    );
    await expect(request("/characters/3", "PUT", { age: -1 })).rejects.toThrow(
      "Age must be non-negative",
    );
  });
  it("explains connectivity failures", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(new TypeError("Failed to fetch")),
    );
    await expect(api.getWorlds()).rejects.toThrow("Could not reach Archivum");
  });
  it("explains possible world deletion constraints without deleting children", async () => {
    const fetch = vi.fn().mockResolvedValue(new Response("", { status: 500 }));
    vi.stubGlobal("fetch", fetch);
    await expect(api.deleteWorld(7)).rejects.toThrow("entries removed first");
    expect(fetch).toHaveBeenCalledTimes(1);
  });
});
