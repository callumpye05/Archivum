import { useEffect, useRef, useState } from "react";
import { AccountSettings } from "./AccountSettings";
import type { UpdateUserDto } from "../types";
import "./AccountMenu.css";

export function AccountMenu({
  username,
  onSave,
  onSignOut,
}: {
  username: string;
  onSave: (data: UpdateUserDto) => Promise<void>;
  onSignOut: () => void;
}) {
  const [open, setOpen] = useState(false);
  const [account, setAccount] = useState(false);
  const container = useRef<HTMLDivElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const wasAccountOpen = useRef(false);

  useEffect(() => {
    // Restore focus after the native dialog has unmounted and released its trap.
    if (wasAccountOpen.current && !account) trigger.current?.focus();
    wasAccountOpen.current = account;
  }, [account]);

  useEffect(() => {
    if (!open) return;
    const outside = (event: PointerEvent) => {
      if (!container.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("pointerdown", outside);
    return () => document.removeEventListener("pointerdown", outside);
  }, [open]);

  function closeAccount() {
    setAccount(false);
  }

  return (
    <div
      ref={container}
      className="account-control"
      onKeyDown={(event) => {
        if (event.key === "Escape" && open) {
          event.preventDefault();
          setOpen(false);
          trigger.current?.focus();
        }
      }}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) setOpen(false);
      }}
    >
      <button
        ref={trigger}
        type="button"
        className="account-trigger"
        aria-expanded={open}
        aria-controls={open ? "account-actions" : undefined}
        onClick={() => setOpen((value) => !value)}
      >
        <span className="account-name">{username}</span>
        <span>Account</span>
        <span aria-hidden="true">▾</span>
      </button>
      {open && (
        <div
          id="account-actions"
          className="account-actions"
          role="group"
          aria-label="Account actions"
        >
          <p>{`Signed in as ${username}`}</p>
          <button
            type="button"
            onClick={() => {
              setOpen(false);
              setAccount(true);
            }}
          >
            Account
          </button>
          <button type="button" onClick={onSignOut}>
            Sign out
          </button>
        </div>
      )}
      {account && (
        <AccountSettings
          username={username}
          onSave={onSave}
          onClose={closeAccount}
          onSignOut={onSignOut}
        />
      )}
    </div>
  );
}
