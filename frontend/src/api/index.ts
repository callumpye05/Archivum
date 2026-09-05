import { request } from "./client";
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
} from "../types";
export const getWorlds = () => request<World[]>("/worlds");
export const getWorld = (id: number) => request<World>(`/worlds/${id}`);
export const createWorld = (data: CreateWorldDto) =>
  request<World>("/worlds/create", "POST", data);
export const updateWorld = (id: number, data: UpdateWorldDto) =>
  request<World>(`/worlds/${id}`, "PUT", data);
export const deleteWorld = (id: number) =>
  request<void>(`/worlds/${id}`, "DELETE");
export const getCharactersByWorld = (worldId: number) =>
  request<WorldCharacter[]>(`/worlds/${worldId}/characters`);
export const getCharacter = (id: number) =>
  request<WorldCharacter>(`/characters/${id}`);
export const getCharacterByWorld = (worldId: number, id: number) =>
  request<WorldCharacter>(`/worlds/${worldId}/characters/${id}`);
export const createCharacter = (worldId: number, data: CreateCharacterDto) =>
  request<WorldCharacter>(`/worlds/${worldId}/characters`, "POST", data);
export const updateCharacter = (id: number, data: UpdateCharacterDto) =>
  request<WorldCharacter>(`/characters/${id}`, "PUT", data);
export const deleteCharacter = (id: number) =>
  request<void>(`/characters/${id}`, "DELETE");
export const getLocationsByWorld = (worldId: number) =>
  request<Location[]>(`/worlds/${worldId}/locations`);
export const getLocation = (id: number) =>
  request<Location>(`/locations/${id}`);
export const getLocationByWorld = (worldId: number, id: number) =>
  request<Location>(`/worlds/${worldId}/locations/${id}`);
export const createLocation = (worldId: number, data: CreateLocationDto) =>
  request<Location>(`/worlds/${worldId}/locations`, "POST", data);
export const updateLocation = (id: number, data: UpdateLocationDto) =>
  request<Location>(`/locations/${id}`, "PUT", data);
export const deleteLocation = (id: number) =>
  request<void>(`/locations/${id}`, "DELETE");
