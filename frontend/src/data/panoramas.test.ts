// @vitest-environment jsdom
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { expect, it } from "vitest";
import { demoPanoramas } from "./panoramas";

// These are asset-integrity checks, not judgments about visual quality.
it.each(demoPanoramas)("$title is a valid, self-contained SVG", (entry) => {
  const source = readFileSync(
    resolve("src/assets/panoramas", `${entry.id}.svg`),
    "utf8",
  );
  const svg = new DOMParser().parseFromString(source, "image/svg+xml");
  expect(svg.querySelector("parsererror")).toBeNull();
  expect(svg.documentElement.getAttribute("viewBox")).toBe("0 0 1800 1100");
  expect(svg.querySelector("title")?.textContent).toBeTruthy();
  expect(svg.querySelector("script, foreignObject, image")).toBeNull();
  expect(source).not.toMatch(/(?:href=["']https?:|url\(["']?https?:)/);
});
