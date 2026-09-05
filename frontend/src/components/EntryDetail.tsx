import { useCallback } from "react";
import * as api from "../api";
import { useResource } from "../hooks/useResource";
import { Modal } from "./Modal";
import { ErrorState, Loading, dateLabel } from "./States";
import type { WorldCharacter, Location } from "../types";
export function EntryDetail({
  worldId,
  id,
  kind,
  onClose,
}: {
  worldId: number;
  id: number;
  kind: "characters" | "locations";
  onClose: () => void;
}) {
  const { data, error, loading, reload } = useResource<
    WorldCharacter | Location
  >(
    useCallback(
      () =>
        kind === "characters"
          ? api.getCharacterByWorld(worldId, id)
          : api.getLocationByWorld(worldId, id),
      [worldId, id, kind],
    ),
  );
  let fields: [string, string | number | null][] = [];
  if (data) {
    fields =
      "characterId" in data
        ? [
            ["Name", data.characterName],
            ["Species", data.characterSpecies],
            ["Age", data.age],
            ["Nationality", data.characterNationality],
            ["Description", data.characterDescription],
          ]
        : [
            ["Name", data.locationName],
            ["Type", data.locationType],
            ["Description", data.locationDescription],
          ];
    fields.push(
      ["World", data.world.worldName],
      ["Created", dateLabel(data.createdAt)],
    );
  }
  return (
    <Modal
      title={kind === "characters" ? "Character record" : "Location record"}
      onClose={onClose}
    >
      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} retry={reload} />
      ) : (
        <dl className="details">
          {fields.map(([label, value]) => (
            <div key={label}>
              <dt>{label}</dt>
              <dd>{value === "" || value === null ? "Not recorded" : value}</dd>
            </div>
          ))}
        </dl>
      )}
    </Modal>
  );
}
