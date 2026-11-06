export { updateCurrentUser as updateUser } from "./client";
import { request } from "./client";
import {
  worldResponse,
  characterResponse,
  locationResponse,
  listResponse,
} from "./responses";
import type {
  World,
  WorldCharacter,
  Location,
  CreateWorldDto,
  UpdateWorldDto,
  CreateCharacterDto,
  UpdateCharacterDto,
  CreateLocationDto,
  UpdateLocationDto,
  RegisterDto,
  RegisteredUser,
} from "../types";
export const registerUser = (data: RegisterDto) =>
  request<RegisteredUser>("/auth/register", "POST", data);
export const verifyEmail = (token: string) =>
  request<void>("/auth/verify-email", "POST", { token });
export const getWorlds = () =>
  request<World[]>("/worlds", "GET", undefined, listResponse(worldResponse));
export const getWorld = (id: number) =>
  request<World>(`/worlds/${id}`, "GET", undefined, worldResponse);
export const createWorld = (data: CreateWorldDto) =>
  request<World>("/worlds/create", "POST", data, worldResponse);
export const updateWorld = (id: number, data: UpdateWorldDto) =>
  request<World>(`/worlds/${id}`, "PUT", data, worldResponse);
export const deleteWorld = (id: number) =>
  request<void>(`/worlds/${id}`, "DELETE");
export const getCharactersByWorld = (worldId: number) =>
  request<WorldCharacter[]>(
    `/worlds/${worldId}/characters`,
    "GET",
    undefined,
    listResponse(characterResponse),
  );
export const getCharacter = (id: number) =>
  request<WorldCharacter>(
    `/characters/${id}`,
    "GET",
    undefined,
    characterResponse,
  );
export const getCharacterByWorld = (worldId: number, id: number) =>
  request<WorldCharacter>(
    `/worlds/${worldId}/characters/${id}`,
    "GET",
    undefined,
    characterResponse,
  );
export const createCharacter = (worldId: number, data: CreateCharacterDto) =>
  request<WorldCharacter>(
    `/worlds/${worldId}/characters`,
    "POST",
    data,
    characterResponse,
  );
export const updateCharacter = (id: number, data: UpdateCharacterDto) =>
  request<WorldCharacter>(`/characters/${id}`, "PUT", data, characterResponse);
export const deleteCharacter = (id: number) =>
  request<void>(`/characters/${id}`, "DELETE");
export const getLocationsByWorld = (worldId: number) =>
  request<Location[]>(
    `/worlds/${worldId}/locations`,
    "GET",
    undefined,
    listResponse(locationResponse),
  );
export const getLocation = (id: number) =>
  request<Location>(`/locations/${id}`, "GET", undefined, locationResponse);
export const getLocationByWorld = (worldId: number, id: number) =>
  request<Location>(
    `/worlds/${worldId}/locations/${id}`,
    "GET",
    undefined,
    locationResponse,
  );
export const createLocation = (worldId: number, data: CreateLocationDto) =>
  request<Location>(
    `/worlds/${worldId}/locations`,
    "POST",
    data,
    locationResponse,
  );
export const updateLocation = (id: number, data: UpdateLocationDto) =>
  request<Location>(`/locations/${id}`, "PUT", data, locationResponse);
export const deleteLocation = (id: number) =>
  request<void>(`/locations/${id}`, "DELETE");
