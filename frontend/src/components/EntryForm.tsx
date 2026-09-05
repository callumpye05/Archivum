import { useState, type FormEvent } from "react";
import { Modal } from "./Modal";
import { locationTypes } from "../types";
export type Field = {
  name: string;
  label: string;
  maxLength?: number;
  kind?: "textarea" | "age" | "locationType";
};
export const worldFields: Field[] = [
  { name: "worldName", label: "World name", maxLength: 100 },
  { name: "worldDesc", label: "Description", maxLength: 500, kind: "textarea" },
];
export const characterFields: Field[] = [
  { name: "characterName", label: "Character name", maxLength: 100 },
  { name: "characterSpecies", label: "Species", maxLength: 100 },
  { name: "age", label: "Age", kind: "age" },
  { name: "characterNationality", label: "Nationality", maxLength: 100 },
  {
    name: "characterDescription",
    label: "Description",
    maxLength: 500,
    kind: "textarea",
  },
];
export const locationFields: Field[] = [
  { name: "locationName", label: "Location name", maxLength: 100 },
  { name: "locationType", label: "Location type", kind: "locationType" },
  {
    name: "locationDesc",
    label: "Description",
    maxLength: 500,
    kind: "textarea",
  },
];
export function EntryForm({
  title,
  context,
  fields,
  initial = {},
  onSave,
  onClose,
}: {
  title: string;
  context: string;
  fields: Field[];
  initial?: Record<string, string | number | null>;
  onSave: (values: Record<string, string>) => Promise<void>;
  onClose: () => void;
}) {
  const [values, setValues] = useState<Record<string, string>>(() =>
    Object.fromEntries(
      fields.map((field) => [
        field.name,
        String(
          initial[field.name] ?? (field.kind === "locationType" ? "CITY" : ""),
        ),
      ]),
    ),
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function submit(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await onSave(values);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not save this entry.",
      );
      setBusy(false);
    }
  }
  return (
    <Modal title={title} onClose={onClose} busy={busy}>
      <p className="form-note">{context}</p>
      <form onSubmit={submit}>
        <fieldset disabled={busy} style={{ border: 0, padding: 0, margin: 0 }}>
          <div className="form-fields">
            {fields.map((field) => {
              const props = {
                id: field.name,
                name: field.name,
                value: values[field.name],
                onChange: (
                  event: React.ChangeEvent<
                    HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
                  >,
                ) => setValues({ ...values, [field.name]: event.target.value }),
              };
              return (
                <label key={field.name} htmlFor={field.name}>
                  {field.label}
                  {field.kind === "textarea" ? (
                    <textarea {...props} maxLength={field.maxLength} />
                  ) : field.kind === "locationType" ? (
                    <select {...props}>
                      {locationTypes.map((type) => (
                        <option key={type} value={type}>
                          {type[0] + type.slice(1).toLowerCase()}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      {...props}
                      type={field.kind === "age" ? "number" : "text"}
                      required={field.kind === "age"}
                      min={field.kind === "age" ? 0 : undefined}
                      max={field.kind === "age" ? 2147483647 : undefined}
                      step={field.kind === "age" ? 1 : undefined}
                      maxLength={field.maxLength}
                    />
                  )}
                  {field.maxLength && (
                    <small>
                      {values[field.name].length} / {field.maxLength}
                    </small>
                  )}
                </label>
              );
            })}
          </div>
        </fieldset>
        {fields.some((field) => field.kind === "age") && (
          <p className="form-note">Age must be a non-negative whole number.</p>
        )}
        {error && (
          <p role="alert" className="inline-error">
            {error}
          </p>
        )}
        <div className="actions">
          <button type="button" onClick={onClose} disabled={busy}>
            Cancel
          </button>
          <button className="primary" disabled={busy}>
            {busy ? "Saving…" : "Save entry"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
