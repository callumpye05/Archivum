// PUT /users/me: omitted fields are left unchanged by UserService.
export interface UpdateUserDto {
  userName?: string;
  email?: string;
  password?: string;
}
export interface RegisterDto {
  userName: string;
  email: string;
  password: string;
}
export interface RegisteredUser {
  userName: string;
  email: string;
}
// Public response DTOs. Related resources are identified by worldId only.
export interface World {
  worldId: number;
  worldName: string;
  worldDesc: string | null;
}
export interface WorldCharacter {
  characterId: number;
  characterName: string;
  characterSpecies: string;
  age: number;
  characterDescription: string | null;
  characterNationality: string | null;
  worldId: number;
}
export const locationTypes = [
  "CITY",
  "VILLAGE",
  "DISTRICT",
  "LANDMARK",
  "FACILITY",
] as const;
export type LocationType = (typeof locationTypes)[number];
export function isLocationType(value: unknown): value is LocationType {
  return locationTypes.some((type) => type === value);
}
export interface Location {
  locationId: number;
  locationName: string;
  locationType: LocationType;
  locationDesc: string | null;
  worldId: number;
}
export interface CreateWorldDto {
  worldName: string;
  worldDesc: string;
}
export interface CreateCharacterDto {
  characterName: string;
  characterSpecies: string;
  age: number;
  characterDescription: string;
  characterNationality: string;
}
export interface CreateLocationDto {
  locationName: string;
  locationType: LocationType;
  locationDesc: string;
}
type Update<T> = { [K in keyof T]?: T[K] | null };
export type UpdateWorldDto = Update<CreateWorldDto>;
export type UpdateCharacterDto = Update<CreateCharacterDto>;
export type UpdateLocationDto = Update<CreateLocationDto>;
