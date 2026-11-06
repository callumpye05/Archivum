import { afterEach, describe, expect, it, vi } from "vitest";
import * as api from "./index";
import { request, UnconfirmedWriteError } from "./client";
import { world, character, location } from "../test/fixtures";

afterEach(() => {
  vi.unstubAllGlobals();
});
function stubFetch(respond: ReturnType<typeof vi.fn>) {
  vi.stubGlobal("fetch", (path: string, options: RequestInit) =>
    path === "/api/auth/csrf"
      ? Promise.resolve(
          Response.json({ headerName: "X-CSRF-TOKEN", token: "test-csrf" }),
        )
      : respond(path, options),
  );
}
describe("API contract", () => {
  it("creates characters in the selected world and updates by characterId", async () => {
    const dto = {
      characterName: "New person",
      characterSpecies: "Human",
      age: 0,
      characterDescription: "",
      characterNationality: "",
    };
    const fetch = vi
      .fn()
      .mockImplementation(() => Promise.resolve(Response.json(character)));
    stubFetch(fetch);
    await expect(api.createCharacter(7, dto)).resolves.toEqual(character);
    await expect(api.updateCharacter(3, { age: 1 })).resolves.toEqual(
      character,
    );
    expect(fetch).toHaveBeenNthCalledWith(
      1,
      "/api/worlds/7/characters",
      expect.objectContaining({ method: "POST", body: JSON.stringify(dto) }),
    );
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      "/api/characters/3",
      expect.objectContaining({ method: "PUT", body: '{"age":1}' }),
    );
  });

  it.each([
    [() => api.getWorld(7), world],
    [() => api.getCharacter(3), character],
    [() => api.getLocation(2), location],
  ] as const)(
    "retains only DTO fields even if a response contains entity properties",
    async (call, dto) => {
      stubFetch(
        vi.fn().mockResolvedValue(
          Response.json({
            ...dto,
            owner: { userHashedPassword: "must-not-be-retained" },
            world: { ...world, owner: { userName: "private" } },
            createdAt: "2026-01-01T00:00:00Z",
          }),
        ),
      );
      await expect(call()).resolves.toEqual(dto);
    },
  );

  it("accepts nullable descriptions and nationality without inventing entity defaults", async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(Response.json({ ...world, worldDesc: null }))
      .mockResolvedValueOnce(
        Response.json({
          ...character,
          characterDescription: null,
          characterNationality: null,
        }),
      )
      .mockResolvedValueOnce(
        Response.json({ ...location, locationDesc: null }),
      );
    stubFetch(fetch);
    await expect(api.getWorld(7)).resolves.toEqual({
      ...world,
      worldDesc: null,
    });
    await expect(api.getCharacter(3)).resolves.toEqual({
      ...character,
      characterDescription: null,
      characterNationality: null,
    });
    await expect(api.getLocation(2)).resolves.toEqual({
      ...location,
      locationDesc: null,
    });
  });

  it.each([
    [() => api.getWorlds(), {}],
    [() => api.getWorlds(), [world, null]],
    [() => api.getWorld(7), { ...world, worldId: "7" }],
    [() => api.getCharacter(3), { ...character, worldId: undefined, world }],
    [
      () => api.getLocation(2),
      {
        id: 2,
        locationName: "Old",
        locationType: "CITY",
        locationDescription: "Old",
        world,
      },
    ],
    [() => api.getLocation(2), { ...location, locationType: "UNKNOWN" }],
    [() => api.getLocation(2), { ...location, locationDesc: undefined }],
  ] as const)(
    "rejects malformed DTOs and obsolete entity shapes",
    async (call, dto) => {
      stubFetch(vi.fn().mockResolvedValue(Response.json(dto)));
      await expect(call()).rejects.toThrow("unexpected response");
    },
  );

  it.each([null, {}, "empty"])(
    "does not replay a create when its response cannot be confirmed: %j",
    async (body) => {
      const fetch = vi
        .fn()
        .mockResolvedValue(
          body === "empty" ? new Response(null) : Response.json(body),
        );
      stubFetch(fetch);
      await expect(
        api.createWorld({ worldName: "New", worldDesc: "" }),
      ).rejects.toBeInstanceOf(UnconfirmedWriteError);
      expect(fetch).toHaveBeenCalledTimes(1);
    },
  );

  it.each([200, 201, 202, 204, 205, 206, 299])(
    "accepts an empty %i world DELETE with session cookies and CSRF and no child requests",
    async (status) => {
      const fetch = vi.fn();
      stubFetch(fetch);
      fetch.mockClear();
      const response = new Response(null, { status });
      const parseJson = vi.spyOn(response, "json");
      fetch.mockResolvedValueOnce(response);
      await expect(api.deleteWorld(7)).resolves.toBeUndefined();
      expect(parseJson).not.toHaveBeenCalled();
      expect(fetch).toHaveBeenCalledExactlyOnceWith(
        "/api/worlds/7",
        expect.objectContaining({
          method: "DELETE",
          body: undefined,
          credentials: "include",
          headers: expect.objectContaining({
            "X-CSRF-TOKEN": "test-csrf",
          }),
        }),
      );
    },
  );
  it.each([400, 401, 403, 404, 409, 500, 503])(
    "rejects a world DELETE with HTTP %i without issuing child requests",
    async (status) => {
      const fetch = vi.fn().mockResolvedValue(new Response(null, { status }));
      stubFetch(fetch);
      await expect(api.deleteWorld(7)).rejects.toMatchObject({
        name: "ApiError",
        status,
      });
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(fetch.mock.calls[0][0]).toBe("/api/worlds/7");
    },
  );
  it.each([
    ["world list", () => api.getWorlds(), "/worlds", [world]],
    ["world", () => api.getWorld(7), "/worlds/7", world],
    [
      "world characters",
      () => api.getCharactersByWorld(7),
      "/worlds/7/characters",
      [character],
    ],
    ["character", () => api.getCharacter(3), "/characters/3", character],
    [
      "scoped character",
      () => api.getCharacterByWorld(7, 3),
      "/worlds/7/characters/3",
      character,
    ],
    [
      "world locations",
      () => api.getLocationsByWorld(7),
      "/worlds/7/locations",
      [location],
    ],
    ["location", () => api.getLocation(2), "/locations/2", location],
    [
      "scoped location",
      () => api.getLocationByWorld(7, 2),
      "/worlds/7/locations/2",
      location,
    ],
  ] as const)(
    "reads %s DTO from the existing route",
    async (_name, call, path, dto) => {
      const fetch = vi.fn().mockResolvedValue(Response.json(dto));
      stubFetch(fetch);
      await expect(call()).resolves.toEqual(dto);
      expect(fetch).toHaveBeenCalledWith(
        `/api${path}`,
        expect.objectContaining({ method: "GET", body: undefined }),
      );
    },
  );
  it("sends and receives locationDesc for location creation and updates", async () => {
    const dto = {
      locationName: "Test place",
      locationDesc: "Test description",
      locationType: "CITY" as const,
    };
    const fetch = vi
      .fn()
      .mockImplementation(() => Promise.resolve(Response.json(location)));
    stubFetch(fetch);
    await expect(api.createLocation(7, dto)).resolves.toEqual(location);
    await expect(api.updateLocation(2, dto)).resolves.toEqual(location);
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
      .mockImplementation(() => Promise.resolve(Response.json(world)));
    stubFetch(fetch);
    await expect(
      api.createWorld({ worldName: "Test", worldDesc: "" }),
    ).resolves.toEqual(world);
    await expect(api.updateWorld(7, { worldDesc: "" })).resolves.toEqual(world);
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
      stubFetch(fetch);
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
    stubFetch(
      vi
        .fn()
        .mockResolvedValue(
          new Response("World was not found", { status: 404 }),
        ),
    );
    await expect(api.getWorld(7)).rejects.toThrow("World was not found");
  });
  it("shows JSON validation errors", async () => {
    stubFetch(
      vi.fn().mockResolvedValue(
        new Response('{"errorMessage":"Age must be non-negative"}', {
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
  it("reports a world deletion failure without deleting children", async () => {
    const fetch = vi.fn().mockResolvedValue(new Response("", { status: 500 }));
    stubFetch(fetch);
    await expect(api.deleteWorld(7)).rejects.toThrow(
      "Unable to delete this entry",
    );
    expect(fetch).toHaveBeenCalledTimes(1);
  });
});
