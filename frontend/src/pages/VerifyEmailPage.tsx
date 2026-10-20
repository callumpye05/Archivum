import { useEffect, useRef, useState } from "react";
import { verifyEmail } from "../api";
import { ApiError } from "../api/client";
import "./RegisterPage.css";

export function VerifyEmailPage({ token }: { token: string | null }) {
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [message, setMessage] = useState("");
  const verification = useRef<Promise<void> | null>(null);

  useEffect(() => {
    if (!token || !/^[a-f0-9]{64}$/.test(token)) {
      setStatus("error");
      setMessage(
        token
          ? "This verification link is invalid. Open the full link from your email."
          : "This verification link is missing its token. Open the full link from your email.",
      );
      return;
    }
    let active = true;
    // Reuse the request when StrictMode runs this effect twice. Tokens are single use.
    verification.current ??= verifyEmail(token);
    verification.current
      .then(() => {
        if (active) setStatus("success");
      })
      .catch((error: unknown) => {
        if (!active) return;
        setStatus("error");
        setMessage(
          error instanceof ApiError && error.status === 400
            ? "This verification link has expired. Links are valid for one hour."
            : error instanceof ApiError && error.status === 404
              ? "This verification link is invalid or has already been used. If you already verified your email, continue to login."
              : "We could not confirm your email verification. Check your connection and reopen the link to try again. If it already succeeded, you can continue to login.",
        );
      });
    return () => {
      active = false;
    };
  }, [token]);

  return (
    <section
      className="entry-card registration-card"
      aria-labelledby="verify-title"
    >
      <p className="eyebrow">YOUR ARCHIVUM</p>
      <h2 id="verify-title">Verify your email</h2>
      {status === "loading" && <p role="status">Verifying your email…</p>}
      {status === "success" && (
        <p role="status">Your email has been verified. You can now log in.</p>
      )}
      {status === "error" && (
        <p className="inline-error" role="alert">
          {message}
        </p>
      )}
      {status !== "loading" && (
        <button
          type="button"
          className="button-link primary"
          onClick={() => window.location.assign("/#/login")}
        >
          Continue to login →
        </button>
      )}
    </section>
  );
}
