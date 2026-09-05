import { useState } from "react";
import { Modal } from "./Modal";
export function DeleteDialog({
  name,
  isWorld,
  onDelete,
  onClose,
}: {
  name: string;
  isWorld?: boolean;
  onDelete: () => Promise<void>;
  onClose: () => void;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function remove() {
    setBusy(true);
    setError("");
    try {
      await onDelete();
    } catch (error) {
      setError(error instanceof Error ? error.message : "Deletion failed.");
      setBusy(false);
    }
  }
  return (
    <Modal title="Delete this entry?" onClose={onClose} busy={busy}>
      <p className="description">
        “{name || "Untitled entry"}” will be permanently deleted. This cannot be
        undone.
      </p>
      {isWorld && (
        <p className="form-note">
          Worlds with characters or locations cannot be deleted until their
          entries have been removed.
        </p>
      )}
      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}
      <div className="actions">
        <button onClick={onClose} disabled={busy} autoFocus>
          Keep entry
        </button>
        <button className="danger" onClick={remove} disabled={busy}>
          {busy ? "Deleting…" : "Delete permanently"}
        </button>
      </div>
    </Modal>
  );
}
