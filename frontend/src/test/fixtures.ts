import type { World, WorldCharacter, Location } from "../types";

// These fixtures intentionally contain only the backend response DTO fields.
export const world = {
  worldId: 7,
  worldName: "Test world",
  worldDesc: "An isolated test fixture",
} satisfies World;

export const character = {
  characterId: 3,
  characterName: "Test person",
  characterSpecies: "Human",
  age: 0,
  characterNationality: "",
  characterDescription: "A description",
  worldId: world.worldId,
} satisfies WorldCharacter;

export const location = {
  locationId: 2,
  locationName: "Test place",
  locationType: "CITY",
  locationDesc: "A place description",
  worldId: world.worldId,
} satisfies Location;
