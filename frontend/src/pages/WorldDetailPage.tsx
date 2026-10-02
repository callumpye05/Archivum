import { useCallback, useState } from "react";
import * as api from "../api";
import type { WorldCharacter, Location, LocationType } from "../types";
import { useResource } from "../hooks/useResource";
import { Empty, ErrorState, Loading } from "../components/States";
import { FeedbackMessage } from "../components/FeedbackMessage";
import {
  EntryForm,
  worldFields,
  characterFields,
  locationFields,
} from "../components/EntryForm";
import { DeleteDialog } from "../components/DeleteDialog";
import { EntryDetail } from "../components/EntryDetail";

type EditTarget =
  | { kind: "world" }
  | { kind: "characters"; entry?: WorldCharacter }
  | { kind: "locations"; entry?: Location };
type DeleteTarget = {
  kind: "world" | "characters" | "locations";
  id: number;
  name: string;
};

export function WorldDetailPage({ id }: { id: number }) {
  const world = useResource(useCallback(() => api.getWorld(id), [id]));
  const characters = useResource(
    useCallback(() => api.getCharactersByWorld(id), [id]),
  );
  const locations = useResource(
    useCallback(() => api.getLocationsByWorld(id), [id]),
  );
  const [tab, setTab] = useState<"characters" | "locations">("characters");
  const [editing, setEditing] = useState<EditTarget | null>(null);
  const [deleting, setDeleting] = useState<DeleteTarget | null>(null);
  const [viewing, setViewing] = useState<{
    kind: "characters" | "locations";
    id: number;
  } | null>(null);
  const [notice, setNotice] = useState("");
  if (world.loading) return <Loading />;
  if (world.error || !world.data)
    return <ErrorState message={world.error} retry={world.reload} />;
  const selectedWorld = world.data;
  const resource = tab === "characters" ? characters : locations;
  const initial: Record<string, string | number | null> =
    editing?.kind === "world"
      ? {
          worldName: selectedWorld.worldName,
          worldDesc: selectedWorld.worldDesc,
        }
      : editing?.kind === "characters" && editing.entry
        ? {
            characterName: editing.entry.characterName,
            characterSpecies: editing.entry.characterSpecies,
            age: editing.entry.age,
            characterNationality: editing.entry.characterNationality,
            characterDescription: editing.entry.characterDescription,
          }
        : editing?.kind === "locations" && editing.entry
          ? {
              locationName: editing.entry.locationName,
              locationType: editing.entry.locationType,
              locationDesc: editing.entry.locationDescription,
            }
          : {};

  async function save(values: Record<string, string>) {
    if (!editing) return;
    if (editing.kind === "world") {
      await api.updateWorld(id, {
        worldName: values.worldName,
        worldDesc: values.worldDesc,
      });
      world.reload();
    } else if (editing.kind === "characters") {
      const dto = {
        characterName: values.characterName,
        characterSpecies: values.characterSpecies,
        age: Number(values.age),
        characterNationality: values.characterNationality,
        characterDescription: values.characterDescription,
      };
      if (editing.entry)
        await api.updateCharacter(editing.entry.characterId, dto);
      else await api.createCharacter(id, dto);
      characters.reload();
    } else {
      const dto = {
        locationName: values.locationName,
        locationType: values.locationType as LocationType,
        locationDesc: values.locationDesc,
      };
      if (editing.entry) await api.updateLocation(editing.entry.id, dto);
      else await api.createLocation(id, dto);
      locations.reload();
    }
    setEditing(null);
    setNotice(
      editing.kind === "world"
        ? "World updated."
        : `${editing.kind === "characters" ? "Character" : "Location"} ${editing.entry ? "updated" : "created"}.`,
    );
  }
  async function remove() {
    if (!deleting) return;
    if (deleting.kind === "world") {
      await api.deleteWorld(id);
      window.location.hash = "/";
    } else if (deleting.kind === "characters") {
      await api.deleteCharacter(deleting.id);
      characters.reload();
    } else {
      await api.deleteLocation(deleting.id);
      locations.reload();
    }
    setDeleting(null);
    if (deleting.kind !== "world")
      setNotice(
        `${deleting.kind === "characters" ? "Character" : "Location"} deleted.`,
      );
  }
  return (
    <>
      <a className="back" href="#/">
        ← All worlds
      </a>
      <header className="page-heading world-heading">
        <div>
          <p className="eyebrow">
            WORLD ARCHIVE / {String(id).padStart(2, "0")}
          </p>
          <h1>{selectedWorld.worldName || "Untitled world"}</h1>
          <p className="description">
            {selectedWorld.worldDesc || "No description yet."}
          </p>
        </div>
        <div className="actions">
          <button type="button" onClick={() => setEditing({ kind: "world" })}>
            Edit world
          </button>
          <button
            type="button"
            className="danger"
            onClick={() =>
              setDeleting({ kind: "world", id, name: selectedWorld.worldName })
            }
          >
            Delete world
          </button>
        </div>
      </header>
      <FeedbackMessage message={notice} onDismiss={setNotice} />
      <nav className="tabs" aria-label="World sections">
        <button
          type="button"
          aria-current={tab === "characters" ? "page" : undefined}
          onClick={() => setTab("characters")}
        >
          Characters <span>{characters.data?.length ?? "—"}</span>
        </button>
        <button
          type="button"
          aria-current={tab === "locations" ? "page" : undefined}
          onClick={() => setTab("locations")}
        >
          Locations <span>{locations.data?.length ?? "—"}</span>
        </button>
      </nav>
      <div className="toolbar">
        <div>
          <h2>
            {tab === "characters"
              ? "People of this world"
              : "Places of this world"}
          </h2>
          <p className="form-note">
            In {selectedWorld.worldName || "this untitled world"}
          </p>
        </div>
        <button
          type="button"
          className="primary"
          onClick={() => setEditing({ kind: tab })}
        >
          ＋ {tab === "characters" ? "Create character" : "Create location"}
        </button>
      </div>
      {resource.loading ? (
        <Loading />
      ) : resource.error ? (
        <ErrorState message={resource.error} retry={resource.reload} />
      ) : !resource.data?.length ? (
        <Empty
          title={`No ${tab} yet`}
          action={
            <button type="button" onClick={() => setEditing({ kind: tab })}>
              Create {tab === "characters" ? "character" : "location"}
            </button>
          }
        >
          Add the first {tab === "characters" ? "character" : "location"} to{" "}
          {selectedWorld.worldName || "this world"}.
        </Empty>
      ) : (
        <div className="entry-grid">
          {tab === "characters"
            ? characters.data?.map((c) => (
                <article className="entry-card" key={c.characterId}>
                  <p className="eyebrow">
                    {c.characterSpecies || "Species unrecorded"}
                  </p>
                  <h2>{c.characterName || "Unnamed character"}</h2>
                  <p
                    className={`excerpt${c.characterDescription ? "" : " placeholder"}`}
                  >
                    {c.characterDescription || "No description yet."}
                  </p>
                  <div className="actions">
                    <button
                      type="button"
                      aria-label={`View ${c.characterName || "character"}`}
                      onClick={() =>
                        setViewing({ kind: "characters", id: c.characterId })
                      }
                    >
                      View
                    </button>
                    <button
                      type="button"
                      className="ghost"
                      aria-label={`Edit ${c.characterName || "character"}`}
                      onClick={() =>
                        setEditing({ kind: "characters", entry: c })
                      }
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      className="danger"
                      aria-label={`Delete ${c.characterName || "character"}`}
                      onClick={() =>
                        setDeleting({
                          kind: "characters",
                          id: c.characterId,
                          name: c.characterName,
                        })
                      }
                    >
                      Delete
                    </button>
                  </div>
                </article>
              ))
            : locations.data?.map((l) => (
                <article className="entry-card" key={l.id}>
                  <p className="eyebrow">{l.locationType}</p>
                  <h2>{l.locationName || "Unnamed location"}</h2>
                  <p
                    className={`excerpt${l.locationDescription ? "" : " placeholder"}`}
                  >
                    {l.locationDescription || "No description yet."}
                  </p>
                  <div className="actions">
                    <button
                      type="button"
                      aria-label={`View ${l.locationName || "location"}`}
                      onClick={() =>
                        setViewing({ kind: "locations", id: l.id })
                      }
                    >
                      View
                    </button>
                    <button
                      type="button"
                      className="ghost"
                      aria-label={`Edit ${l.locationName || "location"}`}
                      onClick={() =>
                        setEditing({ kind: "locations", entry: l })
                      }
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      className="danger"
                      aria-label={`Delete ${l.locationName || "location"}`}
                      onClick={() =>
                        setDeleting({
                          kind: "locations",
                          id: l.id,
                          name: l.locationName,
                        })
                      }
                    >
                      Delete
                    </button>
                  </div>
                </article>
              ))}
        </div>
      )}
      {editing && (
        <EntryForm
          title={
            editing.kind === "world"
              ? "Edit world"
              : `${editing.entry ? "Edit" : "Create"} ${editing.kind === "characters" ? "character" : "location"}`
          }
          context={
            editing.kind === "world"
              ? "World names must be unique."
              : `Belongs to ${selectedWorld.worldName || "this world"}.`
          }

          fields={
            editing.kind === "world"
              ? worldFields
              : editing.kind === "characters"
                ? characterFields
                : locationFields
          }
          initial={initial}
          onSave={save}
          onClose={() => setEditing(null)}
        />
      )}
      {deleting && (
        <DeleteDialog
          name={deleting.name}
          kind={
            deleting.kind === "world"
              ? "world"
              : deleting.kind === "characters"
                ? "character"
                : "location"
          }
          onDelete={remove}
          onClose={() => setDeleting(null)}
        />
      )}
      {viewing && (
        <EntryDetail
          worldId={id}
          {...viewing}
          onClose={() => setViewing(null)}
        />
      )}
    </>
  );
}
