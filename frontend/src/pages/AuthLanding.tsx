import { useEffect, useRef, useState, type FormEvent } from "react";
import { ApiError } from "../api/client";
import { PanoramaViewer } from "../components/PanoramaViewer";
import { demoPanoramas } from "../data/panoramas";
import { RegisterPage } from "./RegisterPage";
import "./AuthLanding.css";

export function AuthLanding({
  register,
  login,
  signedOut = false,
}: {
  register: boolean;
  login: (username: string, password: string) => Promise<void>;
  signedOut?: boolean;
}) {
  const previousMode = useRef(register);
  const loginPanel = useRef<HTMLDivElement>(null);
  const registerPanel = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (previousMode.current === register) return;
    previousMode.current = register;
    (register ? registerPanel : loginPanel).current?.focus({
      preventScroll: true,
    });
  }, [register]);

  return (
    <div className={`auth-landing${register ? " is-register" : ""}`}>
      <PanoramaViewer entries={demoPanoramas} />
      <header className="auth-brand">
        <button
          type="button"
          onClick={() => {
            window.location.hash = "/login";
          }}
          className="brand"
        >
          <span className="brand-icon">A</span>Archivum
          <span className="brand-sub">A PLACE FOR WORLDS</span>
        </button>
      </header>
      <main className="auth-main">
        <header className="auth-intro">
          <p className="eyebrow">YOUR IMAGINATION, THOUGHTFULLY ARCHIVED</p>
          <h1>Every world begins with you.</h1>
          <p>
            A home for the people, places, and stories only you can imagine.
          </p>
        </header>
        <div className="auth-frame">
          {signedOut && (
            <p className="auth-signout-note" role="status">
              Signed out. Log in to open your archive again.
            </p>
          )}
          <nav className="auth-navigation tabs" aria-label="Account">
            <button
              type="button"
              onClick={() => {
                window.location.hash = "/login";
              }}
              aria-current={!register ? "page" : undefined}
            >
              Login
            </button>
            <button
              type="button"
              onClick={() => {
                window.location.hash = "/register";
              }}
              aria-current={register ? "page" : undefined}
            >
              Create account
            </button>
          </nav>
          <div className="auth-panels">
            <div
              ref={loginPanel}
              tabIndex={-1}
              className="auth-panel auth-login-panel"
              inert={register}
              aria-hidden={register}
            >
              <LoginForm login={login} />
            </div>
            <div
              ref={registerPanel}
              tabIndex={-1}
              className="auth-panel auth-register-panel"
              inert={!register}
              aria-hidden={!register}
            >
              <RegisterPage />
            </div>
          </div>
        </div>
      </main>
      <footer className="auth-footer">
        Every world has no limits. Keep yours here.
      </footer>
    </div>
  );
}

function LoginForm({
  login,
}: {
  login: (username: string, password: string) => Promise<void>;
}) {
  const submitting = useRef(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting.current) return;
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const username = form.elements.namedItem("username") as HTMLInputElement;
    const password = form.elements.namedItem(
      "login-password",
    ) as HTMLInputElement;
    submitting.current = true;
    setBusy(true);
    setError("");
    try {
      const pending = login(username.value, password.value);
      password.value = "";
      await pending;
    } catch (error) {
      setError(
        error instanceof ApiError && error.status === 401
          ? "The username or password wasn't recognized. Please try again."
          : error instanceof Error
            ? error.message
            : "Unable to log in. Please try again.",
      );
    } finally {
      password.value = "";
      submitting.current = false;
      setBusy(false);
    }
  }
  return (
    <section
      className="entry-card registration-card"
      aria-labelledby="login-title"
    >
      <p className="eyebrow">YOUR ARCHIVUM</p>
      <h2 id="login-title">Welcome back.</h2>
      <p>Your worlds are waiting.</p>
      <form onSubmit={submit} aria-busy={busy}>
        <fieldset className="registration-fields form-fields" disabled={busy}>
          <label htmlFor="login-username">
            Username
            <input
              id="login-username"
              name="username"
              autoComplete="username"
              required
              pattern="[^:]+"
              title="Enter your username without a colon."
            />
          </label>
          <label htmlFor="login-password">
            Password
            <input
              id="login-password"
              name="login-password"
              type="password"
              autoComplete="current-password"
              required
            />
          </label>
        </fieldset>
        {error && (
          <p className="inline-error" role="alert">
            {error}
          </p>
        )}
        <button className="primary registration-submit" disabled={busy}>
          {busy ? "Opening your archive…" : "Login"}
        </button>
      </form>
    </section>
  );
}
