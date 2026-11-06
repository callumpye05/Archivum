import { useRef, useState, type FormEvent } from "react";
import { Modal } from "./Modal";
import { isLocationType, locationTypes } from "../types";
export type Field = {
  name: string;
  label: string;
  maxLength?: number;
  required?: boolean;
  kind?: "textarea" | "age" | "locationType";
};
export const worldFields: Field[] = [
  { name: "worldName", label: "World name", maxLength: 100, required: true },
  { name: "worldDesc", label: "Description", maxLength: 500, kind: "textarea" },
];
export const characterFields: Field[] = [
  {
    name: "characterName",
    label: "Character name",
    maxLength: 100,
    required: true,
  },
  {
    name: "characterSpecies",
    label: "Species",
    maxLength: 100,
    required: true,
  },
  { name: "age", label: "Age", kind: "age", required: true },
  { name: "characterNationality", label: "Nationality", maxLength: 100 },
  {
    name: "characterDescription",
    label: "Description",
    maxLength: 500,
    kind: "textarea",
  },
];
export const locationFields: Field[] = [
  {
    name: "locationName",
    label: "Location name",
    maxLength: 100,
    required: true,
  },
  {
    name: "locationType",
    label: "Location type",
    kind: "locationType",
    required: true,
  },
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
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const submitting = useRef(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting.current) return;
    const errors: Record<string, string> = {};
    for (const field of fields) {
      const value = values[field.name];
      if (field.required && !value.trim())
        errors[field.name] = `${field.label} is required.`;
      else if (field.maxLength && value.length > field.maxLength)
        errors[field.name] = `Use at most ${field.maxLength} characters.`;
      else if (
        field.kind === "age" &&
        (!Number.isInteger(Number(value)) ||
          Number(value) < 0 ||
          Number(value) > 2147483647)
      )
        errors[field.name] = "Enter a whole number from 0 to 2147483647.";
      else if (field.kind === "locationType" && !isLocationType(value))
        errors[field.name] = "Choose a location type.";
    }
    setFieldErrors(errors);
    setError("");
    const firstInvalid = fields.find((field) => errors[field.name]);
    if (firstInvalid) {
      setError("Check the highlighted fields before saving.");
      const control = event.currentTarget.elements.namedItem(firstInvalid.name);
      if (control instanceof HTMLElement) control.focus();
      return;
    }
    submitting.current = true;
    setBusy(true);
    setError("");
    try {
      await onSave(values);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not save this entry.",
      );
      setBusy(false);
      submitting.current = false;
    }
  }
  return (
    <Modal title={title} onClose={onClose} busy={busy}>
      <p className="form-note">{context}</p>
      <p className="form-note">Fields marked * are required.</p>
      <form onSubmit={submit} aria-busy={busy} noValidate>
        <fieldset disabled={busy} className="entry-fields">
          <div className="form-fields">
            {fields.map((field) => {
              const props = {
                id: field.name,
                name: field.name,
                value: values[field.name],
                required: field.required,
                "aria-invalid": !!fieldErrors[field.name],
                "aria-describedby":
                  [
                    field.maxLength ? `${field.name}-limit` : "",
                    fieldErrors[field.name] ? `${field.name}-error` : "",
                  ]
                    .filter(Boolean)
                    .join(" ") || undefined,
                onChange: (
                  event: React.ChangeEvent<
                    HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
                  >,
                ) => {
                  setValues({ ...values, [field.name]: event.target.value });
                  setFieldErrors((previous) => ({
                    ...previous,
                    [field.name]: "",
                  }));
                  setError("");
                },
              };
              return (
                <label key={field.name} htmlFor={field.name}>
                  {field.label}
                  {field.required ? " *" : ""}
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
                      min={field.kind === "age" ? 0 : undefined}
                      max={field.kind === "age" ? 2147483647 : undefined}
                      step={field.kind === "age" ? 1 : undefined}
                      maxLength={field.maxLength}
                    />
                  )}
                  {fieldErrors[field.name] && (
                    <small className="inline-error" id={`${field.name}-error`}>
                      {fieldErrors[field.name]}
                    </small>
                  )}
                  {field.maxLength && (
                    <small id={`${field.name}-limit`}>
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
