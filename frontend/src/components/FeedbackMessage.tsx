import { useEffect, useState } from "react";

export function FeedbackMessage({
  message,
  onDismiss,
}: {
  message: string;
  onDismiss: (message: string) => void;
}) {
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);
  useEffect(() => {
    if (!message || hovered || focused) return;
    const timer = window.setTimeout(() => onDismiss(""), 6000);
    return () => window.clearTimeout(timer);
  }, [message, onDismiss, hovered, focused]);

  return (
    <div
      className={`feedback${message ? " is-visible" : ""}`}
      role="status"
      aria-atomic="true"
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={() => setFocused(false)}
    >
      {message && (
        <>
          <span>{message}</span>
          <button
            type="button"
            className="ghost"
            aria-label="Dismiss notification"
            onClick={() => {
              setFocused(false);
              setHovered(false);
              onDismiss("");
            }}
          >
            ×
          </button>
        </>
      )}
    </div>
  );
}
