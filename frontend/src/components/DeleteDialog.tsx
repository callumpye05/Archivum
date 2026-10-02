import { useId, useRef, useState } from "react";
import { ApiError } from "../api/client";
import { Modal } from "./Modal";
export function DeleteDialog({
  name,
  kind,
  onDelete,
  onClose,
}: {
  name: string;
  kind: "world" | "character" | "location";
  onDelete: () => Promise<void>;
  onClose: () => void;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const submitting = useRef(false);
  const descriptionId = useId();
  async function remove() {
    if (submitting.current) return;
    submitting.current = true;
    setBusy(true);
    setError("");
    try {
      await onDelete();
    } catch (error) {
      if (error instanceof ApiError) {
        const reason =
          error.status === 401
            ? "Your sign-in is no longer valid. Log in again."
            : error.status === 403
              ? `You don't have permission to delete this ${kind}.`
              : error.status === 404
                ? `This ${kind} is no longer available. Refresh the collection to check its current state.`
                : error.status >= 500
                  ? `The server could not delete this ${kind}. Please try again later.`
                  : `The server rejected deletion of this ${kind}.`;
        // Preserve the status for diagnosis without exposing server internals or credentials.
        setError(`${reason} (HTTP ${error.status})`);
      } else {
        setError(
          `Could not confirm whether this ${kind} was deleted. Check your connection and refresh the collection before retrying.`,
        );
      }
    } finally {
      submitting.current = false;
      setBusy(false);
    }
  }
  return (
    <Modal
      title={`Delete “${name || `Untitled ${kind}`}”?`}
      onClose={onClose}
      busy={busy}
      describedBy={descriptionId}
    >
      <div id={descriptionId} className="delete-description">
        <p>
          {kind === "world"
            ? "Deleting this world will permanently remove the world and all characters and locations associated with it."
            : `This ${kind} will be permanently removed.`}
        </p>
        <p className="delete-consequence">This action cannot be undone.</p>
      </div>
      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}
      <div className="actions">
        <button
          type="button"
          onClick={onClose}
          disabled={busy}
          data-initial-focus
        >
          Cancel
        </button>
        <button
          type="button"
          className="danger-confirm"
          onClick={remove}
          disabled={busy}
        >
          {busy ? "Deleting…" : `Delete ${kind}`}
        </button>
      </div>
      {busy && (
        <p className="form-note" role="status">
          Deleting {kind}…
        </p>
      )}
    </Modal>
  );
}
