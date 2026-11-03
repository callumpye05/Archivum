import { useEffect, useRef, useState } from "react";
import { verifyEmail } from "../api";
import { ApiError } from "../api/client";
import "./RegisterPage.css";

export function VerifyEmailPage({ token }: { token: string | null }) {
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [message, setMessage] = useState("");
  const [attempt, setAttempt] = useState(0);
  const [canRetry, setCanRetry] = useState(false);
  const verification = useRef<{
    token: string;
    attempt: number;
    request: Promise<void>;
  } | null>(null);

  useEffect(() => {
    setCanRetry(false);
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
    setStatus("loading");
    setMessage("");
    // Reuse the request when StrictMode runs this effect twice. Tokens are single use.
    if (
      verification.current?.token !== token ||
      verification.current.attempt !== attempt
    )
      verification.current = { token, attempt, request: verifyEmail(token) };
    verification.current.request
      .then(() => {
        if (active) setStatus("success");
      })
      .catch((error: unknown) => {
        if (!active) return;
        setStatus("error");
        setCanRetry(
          !(error instanceof ApiError && [400, 404].includes(error.status)),
        );
        setMessage(
          error instanceof ApiError && error.status === 400
            ? "This verification link has expired. Links are valid for one hour."
            : error instanceof ApiError && error.status === 404
              ? "This verification link is invalid or has already been used. If you already verified your email, continue to login."
              : "We could not confirm your email verification. Check your connection and try again. If it already succeeded, you can continue to login.",
        );
      });
    return () => {
      active = false;
    };
  }, [token, attempt]);

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
      {status === "error" && canRetry && (
        <button type="button" onClick={() => setAttempt((value) => value + 1)}>
          Try again
        </button>
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
