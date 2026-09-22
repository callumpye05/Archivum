import greenReach from "../assets/panoramas/green-reach.svg";
import amberCoast from "../assets/panoramas/amber-coast.svg";
import quietPeaks from "../assets/panoramas/quiet-peaks.svg";
import lanternIsles from "../assets/panoramas/lantern-isles.svg";
import aelValley from "../assets/panoramas/ael-valley.svg";
import orrenPlain from "../assets/panoramas/orren-plain.svg";

export interface PanoramaEntry {
  id: string | number;
  title: string;
  description: string;
  image: string;
  category: string;
  worldId?: number;
  locationCount?: number;
  characterCount?: number;
  isDemo?: boolean;
}

// Frontend-only studies, with no invented world IDs or record counts.
// Replace this array with mapped records when world imagery is available.
export const demoPanoramas: PanoramaEntry[] = [
  {
    id: "green-reach",
    title: "The Green Reach",
    category: "WOODLAND",
    description:
      "Beyond the last road, rivers thread through ancient woodland. Somewhere beneath the canopy, a story is waiting to be found.",
    image: greenReach,
    isDemo: true,
  },
  {
    id: "amber-coast",
    title: "The Amber Coast",
    category: "SHORELINE",
    description:
      "A long strand of pale sand follows the headlands. Beyond the sheltered bay, the sea opens into silence.",
    image: amberCoast,
    isDemo: true,
  },
  {
    id: "quiet-peaks",
    title: "The Quiet Peaks",
    category: "HIGHLANDS",
    description:
      "Old paths wind between mist and stone. Above the cloud line, the mountains keep their own account of the world.",
    image: quietPeaks,
    isDemo: true,
  },
  {
    id: "lantern-isles",
    title: "The Lantern Isles",
    category: "ARCHIPELAGO",
    description:
      "Five islands rest in a wide expanse of water, their low ridges fading into the sea air.",
    image: lanternIsles,
    isDemo: true,
  },
  {
    id: "ael-valley",
    title: "The Ael Valley",
    category: "RIVER VALLEY",
    description:
      "A silver-green river turns between open slopes, carrying the light toward the distant hills.",
    image: aelValley,
    isDemo: true,
  },
  {
    id: "orren-plain",
    title: "The Orren Plain",
    category: "OPEN FRONTIER",
    description:
      "Low horizons, long grass, and a faint trail. There is room here for a world still taking shape.",
    image: orrenPlain,
    isDemo: true,
  },
];
