# Archivum frontend

React + TypeScript + Vite, using npm. All implementation is confined to this directory; the existing Spring Boot backend is unchanged.

## Run locally

```sh
cd frontend
npm install
npm run dev
```

Open the URL printed by Vite (normally http://localhost:5173). On Windows PowerShell with script executio is disabled, use `npm.cmd` instead of `npm`.

The existing backend must run separately at **http://localhost:8080**. Browser requests go to the  `/api`; Vite forwards them to the backend and removes that prefix. No backend CORS changes are needed for local development.

To change configuration you need to, copy `.env.example` to `.env.local` and then restart Vite. `BACKEND_URL` changes the proxy target; `VITE_API_BASE_URL` changes the browser API prefix.An absolute browser API URL requires that server to allow the frontend origin through CORS.

```sh
npm run build
npm run preview
npm test
```

`dist/` contains the production output. Vite preview also uses the proxy for local verification. A production static host must configure its own `/api` reverse proxy or use a CORS-enabled `VITE_API_BASE_URL` at build time. Hash navigation supports world deep links without a server-side route fallback.

## the Architecture and files

- `src/App.tsx`, `src/main.tsx`: application shell and hash world selection.
- `src/pages/WorldsPage.tsx`: world collection, creation, editing, deletion
- `src/pages/WorldDetailPage.tsx`: selected world, character/location navigation
- `src/components/EntryForm.tsx`: DTO-aligned fields, limits, numeric validation and pending/error handling.
- `src/components/Modal.tsx`, `DeleteDialog.tsx`, `EntryDetail.tsx`, `States.tsx`: accessible native dialogs, confirmation, scoped record views and shared states.
- `src/api/client.ts`, `src/api/index.ts`: typed endpoint functions, timeouts, empty responses and Spring error handling.
- `src/types/index.ts`: backend entity, DTO and enum types.
- `src/hooks/useResource.ts`: asynchronous reads, retry and stale-response protection.
- `src/styles.css`: responsive archive styling, generated CSS ornamentation, no remote assets.
- `src/api/api.test.ts`, `src/App.test.tsx`: isolated API and interaction tests. Fixtures exist only in tests, never in the running application.
- `index.html`, `vite.config.ts`, `tsconfig.json`, `package.json`, `package-lock.json`, `.gitignore`, `.env.example`: frontend setup.

## Backend contract

The entities, all six create/update DTOs, controllers, services, enum, exception handler and SQL schema were inspected before form implementation.

- Lists are plain arrays; individual reads and writes return entities, not envelopes.
- World IDs use `worldId`, character IDs use `characterId`, location IDs use `id`.
- Characters and locations include a nested `world`. Their parent is selected through the create URL, never through an invented body field.
- Location requests use `locationDesc`; responses use `locationDescription`.
- Allowed location types: `CITY`, `VILLAGE`, `DISTRICT`, `LANDMARK`, `FACILITY`.
- Text limits: 100 for names/species/nationality; 500 for descriptions. Create DTOs require non-null fields but permit empty strings (`@NotNull`, not `@NotBlank`). Forms send strings for all text fields, including empty optional prose. Age is required and constrained to a non-negative Java integer (0–2147483647).
- Update DTOs allow missing/null fields; backend services ignore null values. Forms submit complete values, with empty strings clearing text.
- Deletes return an empty response body, handled for both 200 and 204.
- Dates are displayed in the browser's locale. IDs are represented as JavaScript numbers, assuming they remain within the safe integer range.
- No authentication or pagination was found in the inspected backend.

## Existing backend limitations

- World names are unique at the database level; no dedicated duplicate-name error handler exists.
- Foreign keys prevent deleting a world containing characters or locations. No cascade is implemented. The UI confirms deletion and reports failure; it never automatically removes children.
- Validation/database errors are not normalized by the custom exception handler; only not-found cases have explicit plain-text handlers. The frontend supports plain text and common Spring JSON errors, with fallback messages for server failures.
- No CORS configuration was found. The local Vite proxy addresses this without changing backend files.
- The backend was unreachable at localhost:8080 during implementation. Production builds and isolated tests can be verified, but live database-backed CRUD needs the backend running.

## Manual live verification

Verified during implementation: `npm run build` passed without warnings; `npm test` passed all 24 API/DOM interaction tests; the Vite dev server returned HTTP 200. Browser visual verification was unavailable because no browser was connected to the environment. Live backend CRUD remains unverified because port 8080 was unreachable.

With the backend running: create a uniquely named world, select it, create and view a character and location, edit all three, cancel a deletion, then delete the character and location before deleting the world. Confirm field values after refresh. Also check narrow/mobile layout, keyboard dialog navigation, and error/retry behavior with the backend stopped.
