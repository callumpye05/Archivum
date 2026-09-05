import { useState } from "react";
import * as api from "../api";
import type { World } from "../types";
import { useResource } from "../hooks/useResource";
import { Empty, ErrorState, Loading, dateLabel } from "../components/States";
import { EntryForm, worldFields } from "../components/EntryForm";
import { DeleteDialog } from "../components/DeleteDialog";

export function WorldsPage() {
  const { data, error, loading, reload } = useResource(api.getWorlds);
  const [editing, setEditing] = useState<World | "new" | null>(null);
  const [deleting, setDeleting] = useState<World | null>(null);
  const [notice, setNotice] = useState("");
  return (
    <>
      <header className="page-heading">
        <div>
          <p className="eyebrow">THE COLLECTION</p>
          <h1>Your worlds</h1>
          <p>A home for the people, places, and stories you imagine.</p>
        </div>
        <button className="primary" onClick={() => setEditing("new")}>
          ＋ Create world
        </button>
      </header>
      {notice && (
        <div className="notice" role="status">
          {notice}
        </div>
      )}
      <div className="section-label">
        WORLD ARCHIVE <span>{data?.length ?? "—"} worlds</span>
      </div>
      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} retry={reload} />
      ) : !data?.length ? (
        <Empty title="Every story begins with a world">
          Create your first world to start creating its people and places.
        </Empty>
      ) : (
        <div className="world-grid">
          {data.map((world, index) => (
            <article className="world-card" key={world.worldId}>
              <a href={`#/worlds/${world.worldId}`}>
                <div
                  className={`world-art tone-${index % 3}`}
                  aria-hidden="true"
                >
                  <span>✧</span>
                </div>
                <div className="card-body">
                  <p className="eyebrow">
                    WORLD / {String(world.worldId).padStart(2, "0")}
                  </p>
                  <h2>{world.worldName || "Untitled world"}</h2>
                  <p className="excerpt">
                    {world.worldDesc || "A world waiting to be described."}
                  </p>
                  <footer>
                    <span>Explore  world ↗</span>
                  </footer>
                </div>
              </a>
              <div className="world-actions">
                <button
                  aria-label={`Edit ${world.worldName || "world"}`}
                  onClick={() => setEditing(world)}
                >
                  Edit
                </button>
                <button
                  className="danger"
                  aria-label={`Delete ${world.worldName || "world"}`}
                  onClick={() => setDeleting(world)}
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
          title={editing === "new" ? "Create a world" : "Edit world"}
          context="Give your world a name and a description. World names must be unique."
          fields={worldFields}
          initial={
            editing === "new"
              ? {}
              : { worldName: editing.worldName,worldDesc: editing.worldDesc }
          }
          onClose={() =>setEditing(null)}
          onSave={async (values) => {
            const dto = {
              worldName: values.worldName,
              worldDesc: values.worldDesc,
            };
            if (editing === "new")   {
              const world =await api.createWorld(dto);
              setEditing(null);
              window.location.hash = `/worlds/${world.worldId}`;
            }
            else
            {
              await api.updateWorld(editing.worldId, dto);
              setEditing(null);
              setNotice("World updated.");
              reload();
            }
          }}
        />
      )}
      {deleting && (
        <DeleteDialog
          name={deleting.worldName}
          isWorld
          onClose={() => setDeleting(null)}
          onDelete={async () =>
          {
            await api.deleteWorld(deleting.worldId);
            setDeleting(null);
            setNotice("World deleted.");
            reload();
          }}
        />
      )}
    </>
  );
}
