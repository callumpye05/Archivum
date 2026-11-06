import { useEffect, useState } from "react";
import type { PanoramaEntry } from "../data/panoramas";
import "./PanoramaViewer.css";

export function PanoramaViewer({ entries }: { entries: PanoramaEntry[] }) {
  const [scene, setScene] = useState({
    index: 0,
    previous: -1,
    images: {} as Record<string, "loaded" | "failed">,
  });
  const count = entries.length;

  useEffect(() => {
    if (count < 2) return;

    let timer: number | undefined;
    function schedule() {
      window.clearInterval(timer);
      if (document.hidden) return;
      timer = window.setInterval(() => {
        if (document.hidden) return;
        setScene((current) => {
          const active = current.index % count;
          for (let offset = 1; offset < count; offset++) {
            const next = (active + offset) % count;
            if (current.images[entries[next].image] === "loaded") {
              return { ...current, index: next, previous: active };
            }
          }
          return current;
        });
      }, 12000);
    }
    schedule();
    document.addEventListener("visibilitychange", schedule);
    return () => {
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", schedule);
    };
  }, [entries, count]);

  const active = count > 0 ? scene.index % count : 0;

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
            style={{
              visibility:
                scene.images[entry.image] === "loaded" ? "visible" : "hidden",
            }}
            onLoad={() =>
              setScene((current) => ({
                ...current,
                images: { ...current.images, [entry.image]: "loaded" },
              }))
            }
            onError={() =>
              setScene((current) => ({
                ...current,
                images: { ...current.images, [entry.image]: "failed" },
              }))
            }
          />
        </div>
      ))}
      <div className="panorama-shade" />
    </div>
  );
}
