import { useRef, useState, type FormEvent } from "react";
import { registerUser } from "../api";
import { registrationError } from "../api/registrationError";
import "./RegisterPage.css";

export function RegisterPage() {
  const passwordRef = useRef<HTMLInputElement>(null);
  const submitting = useRef(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [registered, setRegistered] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting.current) return;
    const form = event.currentTarget;
    const username = form.elements.namedItem("userName") as HTMLInputElement;
    const email = form.elements.namedItem("email") as HTMLInputElement;
    username.value = username.value.trim();
    email.value = email.value.trim();
    if (!form.reportValidity()) return;

    submitting.current = true;
    setBusy(true);
    setError("");
    try {
      const pending = registerUser({
        userName: username.value,
        email: email.value,
        password: passwordRef.current!.value,
      });
      // The password lives only in the input until the request starts.
      passwordRef.current!.value = "";
      await pending;
      form.reset();
      setRegistered(true);
    } catch (error) {
      setError(registrationError(error));
    } finally {
      if (passwordRef.current) passwordRef.current.value = "";
      submitting.current = false;
      setBusy(false);
    }
  }

  return (
    <section
      className="entry-card registration-card"
      aria-labelledby="register-title"
    >
      <p className="eyebrow">YOUR ARCHIVUM</p>
      <h2 id="register-title">
        {registered ? "Welcome to the archive" : "Create an account"}
      </h2>
      {registered ? (
        <>
          <p className="notice" role="status">
            Account created successfully.
          </p>
          <button
            type="button"
            className="button-link primary"
            onClick={() => {
              window.location.hash = "/login";
            }}
          >
            Continue to login →
          </button>
        </>
      ) : (
        <>
          <p>Begin a home for your imagination.</p>
          <form onSubmit={submit} aria-busy={busy}>
            <fieldset
              className="registration-fields form-fields"
              disabled={busy}
            >
              <label htmlFor="userName">
                Username
                <input
                  id="userName"
                  name="userName"
                  autoComplete="username"
                  required
                />
              </label>
              <label htmlFor="email">
                Email
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                />
              </label>
              <label htmlFor="password">
                Password
                <input
                  ref={passwordRef}
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="new-password"
                  required
                  aria-describedby="password-note"
                />
              </label>
            </fieldset>
            <p className="form-note" id="password-note">
              Your password is cleared when you submit. If registration fails,
              enter it again to retry.
            </p>
            {error && (
              <p className="inline-error" role="alert">
                {error}
              </p>
            )}
            <button className="primary registration-submit" disabled={busy}>
              {busy ? "Creating account…" : "Register"}
            </button>
            {busy && (
              <p className="form-note" role="status">
                Creating your account…
              </p>
            )}
          </form>
        </>
      )}
    </section>
  );
}
