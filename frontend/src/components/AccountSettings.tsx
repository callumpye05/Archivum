import { useRef, useState, type FormEvent } from "react";
import { ApiError } from "../api/client";
import type { UpdateUserDto } from "../types";
import { Modal } from "./Modal";

export function AccountSettings({
  username,
  onSave,
  onClose,
  onSignOut,
}: {
  username: string;
  onSave: (data: UpdateUserDto) => Promise<void>;
  onClose: () => void;
  onSignOut: () => void;
}) {
  const submitting = useRef(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    if (submitting.current || !form.reportValidity()) return;
    const field = (name: string) =>
      form.elements.namedItem(name) as HTMLInputElement;
    const name = field("userName").value.trim();
    const email = field("email").value.trim();
    const password = field("password");
    const confirmation = field("confirmation");
    setError("");
    setMessage("");
    if (!name) {
      setError("Enter a username.");
      return;
    }
    if (
      password.value &&
      (password.value.length < 8 || password.value.length > 100)
    ) {
      setError("Use between 8 and 100 characters for your new password.");
      return;
    }
    if (password.value !== confirmation.value) {
      setError("The new passwords do not match.");
      return;
    }
    const data: UpdateUserDto = {
      ...(name === username ? {} : { userName: name }),
      ...(email ? { email } : {}),
      ...(password.value ? { password: password.value } : {}),
    };
    if (!Object.keys(data).length) {
      setMessage("No changes to save.");
      return;
    }
    submitting.current = true;
    setBusy(true);
    try {
      const pending = onSave(data);
      password.value = "";
      confirmation.value = "";
      await pending;
      field("email").value = "";
      field("userName").value = name;
      setMessage("Account updated. Your sign-in details are up to date.");
    } catch (error) {
      // Do not render raw server validation text that could echo a password.
      setError(
        error instanceof ApiError && error.status === 409
          ? "That username or email is already in use. Try another."
          : "Unable to save changes. Check your details and connection, then retry. If your sign-in changed, log in again.",
      );
    } finally {
      password.value = "";
      confirmation.value = "";
      submitting.current = false;
      setBusy(false);
    }
  }

  return (
    <Modal title="Account settings" onClose={onClose} busy={busy}>
      <p className="account-identity">
        Signed in as <strong>{username}</strong>
      </p>
      <form onSubmit={submit} aria-busy={busy}>
        <fieldset className="form-fields registration-fields" disabled={busy}>
          <label htmlFor="account-username">
            Username
            <input
              id="account-username"
              name="userName"
              defaultValue={username}
              required
              maxLength={100}
              pattern="[^:]+"
              title="Enter your username without a colon."
              autoComplete="username"
            />
          </label>
          <label htmlFor="account-email">
            New email
            <input
              id="account-email"
              name="email"
              type="email"
              maxLength={254}
              autoComplete="off"
            />
          </label>
          <label htmlFor="account-password">
            New password
            <input
              id="account-password"
              name="password"
              type="password"
              minLength={8}
              maxLength={100}
              autoComplete="new-password"
            />
          </label>
          <label htmlFor="account-confirmation">
            Confirm new password
            <input
              id="account-confirmation"
              name="confirmation"
              type="password"
              minLength={8}
              maxLength={100}
              autoComplete="new-password"
            />
          </label>
        </fieldset>
        <p className="form-note">
          Leave new email and password blank to keep them unchanged. Your
          current email is not available here.
        </p>
        {error && (
          <p className="inline-error" role="alert">
            {error}
          </p>
        )}
        {message && <p role="status">{message}</p>}
        <div className="actions">
          <button className="primary" disabled={busy}>
            {busy ? "Saving changes…" : "Save changes"}
          </button>
        </div>
      </form>
      <div className="account-signout">
        <button type="button" onClick={onSignOut}>
          Sign out
        </button>
      </div>
    </Modal>
  );
}
