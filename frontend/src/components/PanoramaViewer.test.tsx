// @vitest-environment jsdom
import { act, StrictMode } from "react";
import { readFileSync } from "node:fs";
import { createRoot, type Root } from "react-dom/client";
import { beforeEach, afterEach, expect, it, vi } from "vitest";
import { PanoramaViewer } from "./PanoramaViewer";
import { AuthLanding } from "../pages/AuthLanding";
import { demoPanoramas } from "../data/panoramas";
let host: HTMLDivElement;
let root: Root;
let reduced = false;
let motion: MediaQueryList;
beforeEach(() => {
  vi.useFakeTimers();
  vi.stubGlobal("IS_REACT_ACT_ENVIRONMENT", true);
  reduced = false;
  motion = Object.assign(new EventTarget(), {
    media: "(prefers-reduced-motion: reduce)",
  }) as MediaQueryList;
  Object.defineProperty(motion, "matches", { get: () => reduced });
  vi.stubGlobal("matchMedia", () => motion);
  vi.spyOn(document, "hidden", "get").mockReturnValue(false);
  host = document.createElement("div");
  document.body.append(host);
  root = createRoot(host);
});
afterEach(async () => {
  await act(async () => root.unmount());
  host.remove();
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
it("advances decorative whole images every 12 seconds and wraps", async () => {
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
  await advance(11999);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
  for (let i = 2; i <= demoPanoramas.length * 2; i++) {
    await advance(12000);
    expect(active()).toBe(demoPanoramas[i % demoPanoramas.length].image);
  }
});
it.each([false, true])(
  "keeps the rotation schedule across Login/Register switches (reduced motion: %s)",
  async (reduceMotion) => {
    reduced = reduceMotion;
    const login = vi.fn();
    await act(async () =>
      root.render(<AuthLanding register={false} login={login} />),
    );
    await loaded();
    const panorama = host.querySelector(".panorama");
    const images = Array.from(host.querySelectorAll(".panorama img"));
    await advance(6000);
    await act(async () => root.render(<AuthLanding register login={login} />));
    expect(active()).toBe(demoPanoramas[0].image);
    await advance(6000);
    expect(active()).toBe(demoPanoramas[1].image);
    await advance(1500);
    await act(async () =>
      root.render(<AuthLanding register={false} login={login} />),
    );
    expect(active()).toBe(demoPanoramas[1].image);
    expect(host.querySelector(".panorama")).toBe(panorama);
    expect(Array.from(host.querySelectorAll(".panorama img"))).toEqual(images);
    expect(host.querySelector(".is-outgoing img")).toBe(images[0]);
    await advance(10499);
    expect(active()).toBe(demoPanoramas[1].image);
    await advance(1);
    expect(active()).toBe(demoPanoramas[2].image);
  },
);
it("pauses in a hidden tab and waits a full interval on return", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  await advance(6000);
  vi.spyOn(document, "hidden", "get").mockReturnValue(true);
  await act(async () => document.dispatchEvent(new Event("visibilitychange")));
  expect(vi.getTimerCount()).toBe(0);
  await advance(60000);
  expect(active()).toBe(demoPanoramas[0].image);
  vi.spyOn(document, "hidden", "get").mockReturnValue(false);
  await act(async () => document.dispatchEvent(new Event("visibilitychange")));
  await advance(11999);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
});
it("rotates for reduced motion without resetting the timer on preference changes", async () => {
  reduced = true;
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  await advance(11999);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
  await advance(6000);
  reduced = false;
  await act(async () => motion.dispatchEvent(new Event("change")));
  await advance(5999);
  expect(active()).toBe(demoPanoramas[1].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[2].image);
  await advance(6000);
  reduced = true;
  await act(async () => motion.dispatchEvent(new Event("change")));
  await advance(6000);
  expect(active()).toBe(demoPanoramas[3].image);
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
  const stylesheet = document.createElement("style");
  stylesheet.textContent = readFileSync(
    "src/components/PanoramaViewer.css",
    "utf8",
  );
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  await loaded();
  // jsdom cannot animate pixels; verify the CSS contract as well as node reuse.
  document.head.append(stylesheet);
  try {
    expect(getComputedStyle(host.querySelector(".panorama")!).position).toBe(
      "fixed",
    );
    expect(
      getComputedStyle(host.querySelector(".panorama")!).pointerEvents,
    ).toBe("none");
    expect(
      getComputedStyle(host.querySelector(".panorama-scene")!).transition,
    ).toContain("opacity 3000ms");
    const nodes = Array.from(host.querySelectorAll("img"));
    expect(nodes).toHaveLength(demoPanoramas.length);
    await advance(12000);
    expect(host.querySelector(".is-outgoing img")).toBe(nodes[0]);
    expect(host.querySelector(".is-active img")).toBe(nodes[1]);
    await advance(1500);
    expect(Array.from(host.querySelectorAll("img"))).toEqual(nodes);
    await advance(1500);
    expect(host.querySelector(".is-active img")).toBe(nodes[1]);
    expect(getComputedStyle(nodes[1].parentElement!).opacity).toBe("1");
    expect(getComputedStyle(nodes[0].parentElement!).opacity).toBe("0");
  } finally {
    stylesheet.remove();
  }
});

it("keeps typed input and focus intact during rotation", async () => {
  await act(async () =>
    root.render(<AuthLanding register={false} login={vi.fn()} />),
  );
  await loaded();
  const field = host.querySelector<HTMLInputElement>("#login-username")!;
  field.value = "An unfinished name";
  field.focus();
  await advance(12000);
  expect(active()).toBe(demoPanoramas[1].image);
  expect(document.activeElement).toBe(field);
  expect(field.value).toBe("An unfinished name");
});

it("keeps one timer through StrictMode replay and releases resources on unmount", async () => {
  await act(async () =>
    root.render(
      <StrictMode>
        <PanoramaViewer entries={demoPanoramas} />
      </StrictMode>,
    ),
  );
  expect(vi.getTimerCount()).toBe(1);
  await loaded();
  await advance(12000);
  expect(active()).toBe(demoPanoramas[1].image);
  await act(async () => root.render(null));
  expect(vi.getTimerCount()).toBe(0);
  // A leaked visibility handler would recreate a timer after unmount.
  await act(async () => document.dispatchEvent(new Event("visibilitychange")));
  await advance(60000);
  expect(host.querySelector(".panorama")).toBeNull();
  expect(vi.getTimerCount()).toBe(0);
  await act(async () =>
    root.render(
      <StrictMode>
        <PanoramaViewer entries={demoPanoramas} />
      </StrictMode>,
    ),
  );
  await loaded();
  expect(vi.getTimerCount()).toBe(1);
  await advance(12000);
  expect(active()).toBe(demoPanoramas[1].image);
});
it("holds the current image until another loads, then advances on the existing schedule", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  const images = host.querySelectorAll("img");
  expect(images[0].style.visibility).toBe("hidden");
  await act(async () => images[0].dispatchEvent(new Event("load")));
  expect(images[0].style.visibility).toBe("visible");
  await advance(12000);
  expect(active()).toBe(demoPanoramas[0].image);
  expect(images[1].style.visibility).toBe("hidden");
  await advance(6000);
  await act(async () => images[1].dispatchEvent(new Event("load")));
  // A successful load does not reset the timer or change the active scene.
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(5999);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(1);
  expect(active()).toBe(demoPanoramas[1].image);
  expect(images[1].style.visibility).toBe("visible");
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
  await advance(12000);
  expect(active()).toBe(demoPanoramas[2].image);
});

it("keeps the current image when alternatives fail and accepts a later successful load", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  const images = host.querySelectorAll("img");
  // A later error must invalidate an earlier successful load.
  await loaded();
  await act(async () =>
    Array.from(images)
      .slice(1)
      .forEach((image) => image.dispatchEvent(new Event("error"))),
  );
  await advance(12000);
  expect(active()).toBe(demoPanoramas[0].image);
  await advance(12000);
  expect(active()).toBe(demoPanoramas[0].image);
  await act(async () => images[1].dispatchEvent(new Event("load")));
  await advance(12000);
  expect(active()).toBe(demoPanoramas[1].image);
});

it("skips an unloaded image when a later image is ready", async () => {
  await act(async () =>
    root.render(<PanoramaViewer entries={demoPanoramas} />),
  );
  const images = host.querySelectorAll("img");
  await act(async () => {
    images[0].dispatchEvent(new Event("load"));
    images[2].dispatchEvent(new Event("load"));
  });
  await advance(12000);
  expect(active()).toBe(demoPanoramas[2].image);
  await advance(12000);
  expect(active()).toBe(demoPanoramas[0].image);
});

it.each([false, true])(
  "continues rotating while staying on the same auth form (register: %s)",
  async (register) => {
    await act(async () =>
      root.render(<AuthLanding register={register} login={vi.fn()} />),
    );
    await loaded();
    await advance(12000);
    expect(active()).toBe(demoPanoramas[1].image);
    await advance(12000);
    expect(active()).toBe(demoPanoramas[2].image);
  },
);
