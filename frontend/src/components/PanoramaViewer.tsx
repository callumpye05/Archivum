import { useEffect, useRef, useState } from "react";
import type { PanoramaEntry } from "../data/panoramas";
import "./PanoramaViewer.css";

export function PanoramaViewer({ entries }: { entries: PanoramaEntry[] }) {
  const [scene, setScene] = useState({ index: 0, previous: -1 });
  const ready = useRef(new Set<string>());
  const failed = useRef(new Set<string>());
  useEffect(() => {
    const motion = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    let timer: ReturnType<typeof setInterval> | undefined;
    function schedule() {
      clearInterval(timer);
      if (document.hidden || motion?.matches || entries.length < 2) return;
      // 12 seconds at rest plus a 2.8-second overlapping transition.
      timer = setInterval(() => {
        setScene((current) => {
          for (let offset = 1; offset < entries.length; offset++) {
            const next = (current.index + offset) % entries.length;
            if (failed.current.has(entries[next].image)) continue;
            // Keep the current map if the next image has not decoded yet.
            if (!ready.current.has(entries[next].image)) return current;
            return { index: next, previous: current.index };
          }
          return current;
        });
      }, 14800);
    }
    schedule();
    document.addEventListener("visibilitychange", schedule);
    motion?.addEventListener("change", schedule);
    return () => {
      clearInterval(timer);
      document.removeEventListener("visibilitychange", schedule);
      motion?.removeEventListener("change", schedule);
    };
  }, [entries]);
  const active = entries.length ? scene.index % entries.length : 0;
  return (
    <div className="panorama" aria-hidden="true">
      {entries.map((entry, index) => (
        <div
          key={entry.id}
          className={
            "panorama-scene" +
            (index === active
              ? " is-active"
              : index === scene.previous
                ? " is-outgoing"
                : "")
          }
        >
          <img
            src={entry.image}
            alt=""
            loading="eager"
            decoding="async"
            fetchPriority={index === 0 ? "high" : "low"}
            onLoad={async (event) => {
              const image = event.currentTarget;
              try {
                // Mounted layers preload every local SVG. Decode before use so
                // a completed download cannot still cause a blank first frame.
                await image.decode?.();
                failed.current.delete(entry.image);
                ready.current.add(entry.image);
              } catch {
                failed.current.add(entry.image);
              }
            }}
            onError={() => failed.current.add(entry.image)}
          />
        </div>
      ))}
      <div className="panorama-shade" />
    </div>
  );
}
