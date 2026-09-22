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
export interface World {
  worldId: number;
  worldName: string;
  worldDesc: string | null;
  createdAt: string | null;
}
export interface WorldCharacter {
  characterId: number;
  characterName: string;
  characterSpecies: string;
  age: number;
  characterDescription: string | null;
  characterNationality: string | null;
  createdAt: string;
  world: World;
}
export const locationTypes = [
  "CITY",
  "VILLAGE",
  "DISTRICT",
  "LANDMARK",
  "FACILITY",
] as const;
export type LocationType = (typeof locationTypes)[number];
export interface Location {
  id: number;
  locationName: string;
  locationType: LocationType;
  locationDescription: string | null;
  createdAt: string;
  world: World;
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
