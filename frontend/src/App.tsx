import { useEffect, useState } from "react";
import { WorldsPage } from "./pages/WorldsPage";
import { WorldDetailPage } from "./pages/WorldDetailPage";
import { AuthLanding } from "./pages/AuthLanding";
import { useAuth } from "./hooks/useAuth";
import { AccountMenu } from "./components/AccountMenu";
export default function App() {
  const auth = useAuth();
  const [hash, setHash] = useState(window.location.hash);
  useEffect(() => {
    const update = () => setHash(window.location.hash);
    window.addEventListener("hashchange", update);
    return () => window.removeEventListener("hashchange", update);
  }, []);
  useEffect(() => {
    if (
      hash === "#/explore" ||
      hash === "#/panorama" ||
      (auth.status === "authenticated" &&
        (hash === "#/register" || hash === "#/login"))
    ) {
      window.history.replaceState(null, "", "#/");
      setHash("#/");
    }
  }, [hash, auth.status]);
  const match = hash.match(/^#\/worlds\/(\d+)$/);
  const register = hash === "#/register";
  const pageTitle = match ? "World overview" : "Worlds";
  if (auth.status === "guest") {
    return (
      <AuthLanding
        register={register}
        login={auth.login}
        signedOut={auth.signedOut}
      />
    );
  }
  return (
    <div className="app">
      <aside className="sidebar">
        <a href="#/" className="brand">
          <span className="brand-icon">A</span>Archivum
          <span className="brand-sub">A PLACE FOR WORLDS</span>
        </a>
        <div className="sidebar-label">WORKSPACE</div>
        <nav className="workspace-nav" aria-label="Workspace">
          <a className="nav-link" href="#/" aria-current="page">
            <span aria-hidden="true">◈</span> Worlds <span>↗</span>
          </a>
        </nav>
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
            Personal archive <span className="slash">/</span> {pageTitle}
          </span>
          <AccountMenu
            username={auth.username!}
            onSave={auth.saveAccount}
            onSignOut={() => {
              auth.logout();
              window.history.replaceState(null, "", "#/login");
              setHash("#/login");
            }}
          />
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
