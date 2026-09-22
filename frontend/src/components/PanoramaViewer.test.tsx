// @vitest-environment jsdom
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { beforeEach, afterEach, expect, it, vi } from "vitest";
import { PanoramaViewer } from "./PanoramaViewer";
import { AuthLanding } from "../pages/AuthLanding";
import { demoPanoramas } from "../data/panoramas";
let host: HTMLDivElement;
let root: Root;
let reduced = false;
let motionChange: () => void;
beforeEach(() => {
  vi.useFakeTimers();
  vi.stubGlobal("IS_REACT_ACT_ENVIRONMENT", true);
  reduced = false;
  vi.stubGlobal("matchMedia", () => ({
    get matches() {
      return reduced;
    },
    addEventListener: (_event: string, callback: () => void) => {
      motionChange = callback;
    },
    removeEventListener: vi.fn(),
  }));
  vi.spyOn(document, "hidden", "get").mockReturnValue(false);
  host = document.createElement("div");
  root = createRoot(host);
});
afterEach(async () => {
  await act(async () => root.unmount());
  vi.useRealTimers();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
const active = () => host.querySelector(".is-active img")?.getAttribute("src");
async function loaded() {
  await act(async () => {
    host
      .querySelectorAll("img")
      .forEach((image) => image.dispatchEvent(new Event("load")));
  });
}
const advance = async (ms: number) => {
  await act(async () => vi.advanceTimersByTime(ms));
};
it("advances decorative whole images every 14.8 seconds and wraps", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  expect(host.querySelector(".panorama")?.getAttribute("aria-hidden")).toBe(
    "true",
  );
  expect(
    host.querySelector("button, a, [tabindex], .panorama-door"),
  ).toBeNull();
  expect(host.querySelectorAll(".is-active img")).toHaveLength(1);
  expect(active()).toBe(demoPanoramas[0].image);
  await loaded();
  await advance(14799);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
  for (let i = 2; i <= demoPanoramas.length; i++) {
    await advance(14800);
    expect(active()).toBe(demoPanoramas[i % demoPanoramas.length].image);
  }
});
it("does not advance or reset the timer when Login/Register switches", async () => {
  const login = vi.fn();
  await act(async () =>
    root.render(<AuthLanding register={false} login={login} />),
  );
  await loaded();
  await advance(6000);
  await act(async () => root.render(<AuthLanding register login={login} />));
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(8800);
  expect(active()).toBe(demoPanoramas[1].image);
  await act(async () =>
    root.render(<AuthLanding register={false} login={login} />),
  );
  expect(active()).toBe(demoPanoramas[1].image);
});
it("pauses in a hidden tab and waits a full interval on return", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  await advance(6000);
  vi.spyOn(document, "hidden", "get").mockReturnValue(true);
  await act(async () => document.dispatchEvent(new Event("visibilitychange")));
  await advance(60000);
  expect(active()).toBe(demoPanoramas[0].image);
  vi.spyOn(document, "hidden", "get").mockReturnValue(false);
  await act(async () => document.dispatchEvent(new Event("visibilitychange")));
  await advance(14799);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
});
it("stays static for reduced motion and responds to preference changes", async () => {
  reduced = true;
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  await advance(60000);
  expect(active()).toBe(demoPanoramas[0].image);
  reduced = false;
  await act(async () => motionChange());
  await advance(14800);
  expect(active()).toBe(demoPanoramas[1].image);
  reduced = true;
  await act(async () => motionChange());
  await advance(60000);
  expect(active()).toBe(demoPanoramas[1].image);
});
it("cleans up its timer and supports empty or single-image lists", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  expect(vi.getTimerCount()).toBe(1);
  await act(async () => root.render(<PanoramaViewer entries={[]} />));
  expect(host.querySelector("img")).toBeNull();
  expect(vi.getTimerCount()).toBe(0);
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas.slice(0, 1)} />),
  );
  await loaded();
  await advance(60000);
  expect(active()).toBe(demoPanoramas[0].image);
  expect(vi.getTimerCount()).toBe(0);
});

it("keeps outgoing and incoming image nodes mounted throughout each transition", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  const nodes = Array.from(host.querySelectorAll("img"));
  expect(nodes).toHaveLength(demoPanoramas.length);
  await advance(14800);
  expect(host.querySelector(".is-outgoing img")).toBe(nodes[0]);
  expect(host.querySelector(".is-active img")).toBe(nodes[1]);
  await advance(1400);
  expect(Array.from(host.querySelectorAll("img"))).toEqual(nodes);
  await advance(1400);
  expect(host.querySelector(".is-active img")).toBe(nodes[1]);
});
it("holds the current map until the next asset finishes decoding", async () => {
  let finish!: () => void;
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  const next = host.querySelectorAll("img")[1];
  Object.defineProperty(next, "decode", {
    value: () =>
      new Promise<void>((resolve) => {
        finish = resolve;
      }),
  });
  await act(async () => next.dispatchEvent(new Event("load")));
  await advance(14800);
  expect(active()).toBe(demoPanoramas[0].image);
  await act(async () => finish());
  // Decode completion does not itself force a transition: the timer owns it.
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(14800);
  expect(active()).toBe(demoPanoramas[1].image);
});
it("skips a broken image without replacing the current scene with a blank", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  const images = host.querySelectorAll("img");
  await act(async () => {
    images[1].dispatchEvent(new Event("error"));
    images[2].dispatchEvent(new Event("load"));
  });
  await advance(14800);
  expect(active()).toBe(demoPanoramas[2].image);
});
