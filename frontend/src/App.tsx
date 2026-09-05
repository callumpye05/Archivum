import { useEffect, useState } from "react";
import { WorldsPage } from "./pages/WorldsPage";
import { WorldDetailPage } from "./pages/WorldDetailPage";
export default function App() {
  const [hash, setHash] = useState(window.location.hash);
  useEffect(() => {
    const update = () => setHash(window.location.hash);
    window.addEventListener("hashchange", update);
    return () => window.removeEventListener("hashchange", update);
  }, []);
  const match = hash.match(/^#\/worlds\/(\d+)$/);
  return (
    <div className="app">
      <aside className="sidebar">
        <a href="#/" className="brand">
          <span className="brand-icon">A</span>Archivum
          <span className="brand-sub">A PLACE FOR WORLDS</span>
        </a>
        <div className="sidebar-label">WORKSPACE</div>
        <a className="nav-link" href="#/">
          <span aria-hidden="true">◈</span> Worlds <span>↗</span>
        </a>
        <div className="sidebar-note">
          <span>✧</span>
          <p>
            Every world has no limits.
            <br />
            Keep yours here.
          </p>
        </div>
      </aside>
      <div className="workspace">
        <div className="topbar">
          <span>
            Personal archive <span className="slash">/</span>{" "}
            {match ? "World overview" : "Worlds"}
          </span>
          <span className="archive-label">ARCHIVUM</span>
        </div>
        <main>
          {match ? (
            <WorldDetailPage key={match[1]} id={Number(match[1])} />
          ) : (
            <WorldsPage />
          )}
        </main>
        <footer className="app-footer">
          Your imagination, thoughtfully archived.
        </footer>
      </div>
    </div>
  );
}
