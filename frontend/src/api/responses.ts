import {
  isLocationType,
  type World,
  type WorldCharacter,
  type Location,
} from "../types";

// Validate the DTO boundary and retain only public response fields in state.
// Missing fields are contract errors, not a reason to accept old entity shapes.
function record(value: unknown): Record<string, unknown> {
  if (typeof value !== "object" || value === null || Array.isArray(value))
    throw new Error("Expected a response object");
  return Object.fromEntries(Object.entries(value));
}

function string(value: unknown): string {
  if (typeof value !== "string") throw new Error("Expected a string");
  return value;
}

function nullableString(value: unknown): string | null {
  return value === null ? null : string(value);
}

function integer(value: unknown): number {
  if (typeof value !== "number" || !Number.isSafeInteger(value))
    throw new Error("Expected an integer");
  return value;
}

export function worldResponse(value: unknown): World {
  const dto = record(value);
  return {
    worldId: integer(dto.worldId),
    worldName: string(dto.worldName),
    worldDesc: nullableString(dto.worldDesc),
  };
}

export function characterResponse(value: unknown): WorldCharacter {
  const dto = record(value);
  return {
    characterId: integer(dto.characterId),
    characterName: string(dto.characterName),
    characterSpecies: string(dto.characterSpecies),
    age: integer(dto.age),
    characterDescription: nullableString(dto.characterDescription),
    characterNationality: nullableString(dto.characterNationality),
    worldId: integer(dto.worldId),
  };
}

export function locationResponse(value: unknown): Location {
  const dto = record(value);
  if (!isLocationType(dto.locationType))
    throw new Error("Unknown location type");
  return {
    locationId: integer(dto.locationId),
    locationName: string(dto.locationName),
    locationType: dto.locationType,
    locationDesc: nullableString(dto.locationDesc),
    worldId: integer(dto.worldId),
  };
}

export function listResponse<T>(parse: (value: unknown) => T) {
  return (value: unknown): T[] => {
    if (!Array.isArray(value)) throw new Error("Expected a response list");
    return value.map(parse);
  };
}
