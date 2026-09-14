# tenant-a-record-export

A MiSC-Cloud customization microservice for tenant `tenant-a`: adds an "Export CSV" action to the record tab bar and a page to pick a record type, preview it, and download it as CSV.

Frontend-only — no backend API of its own. Fetches directly from the existing `record`/`type` services using the bearer token it's handed, and renders using the host's shared `Button`/`Panel`/`RecordTable` components (exposed on `window.__miscCloudHostUI__` by the main app).

Registers itself with Tenant Manager on startup for two customization points: `record.tabs.actions` (the button, an additive slot in `RecordTabs.tsx`) and `record.csv-export` (the page, at `/private/custom/record.csv-export`) — both served from the same static bundle.

Build: `npm install && npm run build` produces `dist/ui/index.js` (esbuild, IIFE, classic JSX transform, `react`/`react-dom` aliased to shims reading `window.React`/`window.ReactDOM`).
