# 10 — Development Plan

Goal: avoid architectural rewrites and wasted coding-agent tokens.

## Phase 0 — Risk Spikes (do before main implementation)

### 0A Gemini structured generation
Create a tiny standalone/backend test that:
- sends one screenshot to Gemini;
- requests exact JSON schema;
- receives `App.tsx` + `index.css`;
- validates parse.

Stop and fix prompt/provider assumptions before building UI if this fails.

### 0B Sandpack compatibility
Hard-code a generated-files object and prove Sandpack can run the exact template/allowed dependency set.

### 0C Figma render
Using a personal test frame and token, prove URL parser + Figma render endpoint.

### 0D Browser screenshot
Prove public URL -> screenshot through chosen hosted browser endpoint.

Once these four risks are proven, freeze integrations.

## Phase 1 — Repository / CI / Base UI
- monorepo/repo layout;
- React/Vite/Tailwind;
- Spring Boot;
- local PostgreSQL;
- baseline GitHub Actions;
- global dark flat design tokens;
- health endpoint.

Exit criteria: frontend/backed both start and CI is green.

## Phase 2 — Identity / Authentication
- users table;
- registration/login/logout/refresh/me;
- Spring Security/JWT;
- guest-session cookie creation/validation helper;
- frontend auth state must support `GUEST` and `USER` principals without a login wall.

Exit criteria: account flow works locally and an unauthenticated visitor receives a usable guest identity on first write action.

## Phase 3 — Project Core + Image Storage
- persistent user project CRUD;
- temporary guest project creation/access with 24-hour expiry;
- project input entity;
- R2 storage adapter + local dev substitute if needed;
- image upload/paste;
- normalization.

Exit criteria: authenticated user can create/reopen a persistent screenshot project, and guest can create/open a temporary screenshot project without registering.

## Phase 4 — Initial AI Generation
- Gemini provider interface + implementation;
- generation prompt;
- structured result validation;
- ai_runs;
- first version creation.

Exit criteria: screenshot project generates valid constrained files and persists them.

## Phase 5 — Workspace
- Monaco file tabs;
- Sandpack preview;
- current draft autosave;
- save status;
- preview responsive presets.

Exit criteria: user edits generated code and preview updates/persists.

## Phase 6 — Refinement + Versions
- natural-language refine;
- automatic pre-refine checkpoint;
- manual checkpoint;
- versions list/view/restore.

Exit criteria: multiple revisions can be created and older version restored safely.

## Phase 7 — Additional Inputs
Order:
1. Wireframe mode (cheap because image pipeline reuse)
2. Website URL
3. Figma frame

Exit criteria: all four entry paths end at same generation pipeline.

## Phase 8 — Usage Protection / BYOK
- guest 3-per-24h demo quota query/enforcement;
- registered-user/IP/global quota enforcement;
- expired guest-project cleanup job;
- global kill switch;
- BYOK header path;
- secret redaction;
- UI quota/BYOK controls.

Exit criteria: a new visitor can complete up to 3 real AI requests without login, abuse caps work, and owner's paid keys are not required for deployed public use.

## Phase 9 — Export
- ZIP generation;
- fixed Vite project template;
- export security tests.

Exit criteria: downloaded project runs locally.

## Phase 10 — Public Examples / Deployment / Polish
- seed 3 examples;
- anonymous read-only example workspace;
- verify guest generation path in production without login;
- deploy all services;
- production CORS/security check;
- README/screenshots/architecture diagram;
- remove debug logs/dead code.

## Scope Control
If time is tight, do **not** cut the core stack. Cut in this order:
1. fancy landing-page polish;
2. additional example count;
3. manual checkpoint note field;
4. responsive preview presets;
5. Figma input last.

Do not cut:
- Spring Boot backend;
- PostgreSQL persistence;
- image generation flow;
- Monaco/Sandpack;
- AI refinement/versioning;
- Docker/CI;
- deployed project.
