# UIFoundry — Master Specification

> Working title: **UIFoundry**. Renaming the product later is allowed and does not count as an architectural change.
>
> Purpose: build a portfolio-quality, deployable full-stack application that converts design inputs into runnable React + TypeScript + Tailwind code. The project replaces a basic blog project on a software-engineering resume, so it must visibly exercise Java/Spring Boot, React/TypeScript, PostgreSQL, REST, Docker, CI/CD, and selected external integrations without becoming a research project.

## 1. Frozen Product Direction

UIFoundry accepts four user-facing design input paths:

1. **Image / Screenshot** — PNG, JPEG, WebP; upload, drag/drop, or clipboard paste.
2. **Website URL** — public HTTP(S) URL is rendered to a normalized screenshot through a hosted browser screenshot service.
3. **Figma Frame Link** — user supplies a Figma frame URL plus a Figma access token when required; backend renders the selected frame to PNG through the Figma REST API.
4. **Wireframe / Mockup** — image upload using a wireframe-specific prompt mode. This intentionally reuses the image pipeline rather than adding another parser.

All four paths normalize to the same internal representation:

`Input Source -> Normalized Design Image -> AI Generation -> React/TS/Tailwind Files -> Editor + Live Preview -> Versioning -> Download ZIP`

## 2. Frozen Technology Stack

### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS
- Monaco Editor
- Sandpack for runnable preview

### Backend
- Java 21 LTS
- Spring Boot 3.5.x
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- REST APIs

### Database / Storage
- PostgreSQL
- PostgreSQL JSONB for generated file snapshots and metadata
- Cloudflare R2 for uploaded/normalized images

### AI
- Primary provider: **Google Gemini API**
- Default model: configurable through `AI_MODEL`; initial recommendation `gemini-2.5-flash`
- V1 implementation: Gemini provider only
- Architecture: provider interface so OpenAI or another model can be added later without changing controllers or project/version logic
- No OpenAI API key is deployed by default
- No Codex quota/API is ever used by the deployed product

### External integrations
- Figma REST API for frame rendering
- Hosted browser screenshot service for Website URL input; production recommendation: Cloudflare Browser Run screenshot endpoint

### DevOps
- Docker
- Docker Compose
- GitHub Actions
- Frontend deployment: Cloudflare Pages
- Backend deployment: Render free web service initially
- PostgreSQL deployment: Neon free tier initially
- Object storage: Cloudflare R2

## 3. Authentication / Guest Policy

- Email/password account system is required for persistent personal workspaces.
- JWT authentication is required for registered-user sessions.
- **Registration is not required to try generation.** A first-time visitor receives a secure guest-session cookie and may use the real generation workflow immediately.
- Anonymous/guest visitors may:
  - view landing page and public example projects;
  - create temporary guest projects;
  - use any supported input mode;
  - make up to **3 server-demo AI requests per rolling 24-hour window**;
  - edit generated code in Monaco and run Sandpack preview;
  - use AI refinement while quota remains (each refinement counts as one AI request);
  - download the current project as ZIP.
- Guest projects are temporary and expire after 24 hours. They do not appear in the persistent dashboard and are not guaranteed to survive after expiration.
- Logged-in users receive persistent projects, dashboard access, durable version history, and BYOK support.
- V1 does not automatically transfer/claim a guest project after registration; this may be added later if needed.
- No password reset, email verification, OAuth, social login, organizations, or teams in V1.

## 4. AI Cost Policy

The deployed application must never consume the owner's OpenAI/Codex quota.

Two generation modes exist:

### Server Demo Key
- A dedicated Gemini API key stored only in backend environment variables.
- May use only a free-tier Gemini project unless the owner explicitly changes it later.
- Configurable hard caps:
  - per-guest rolling 24-hour AI request cap;
  - per-account daily AI request cap;
  - per-IP rolling 24-hour abuse cap;
  - global daily AI request cap.
- Recommended defaults:
  - `DEMO_AI_REQUESTS_PER_GUEST_PER_24H=3`
  - `DEMO_AI_REQUESTS_PER_USER_PER_DAY=3`
  - `DEMO_AI_REQUESTS_PER_IP_PER_24H=6`
  - `DEMO_AI_REQUESTS_GLOBAL_PER_DAY=30`
- When limit is reached, server key generation is unavailable.

### BYOK (Bring Your Own Key)
- Logged-in users can provide their own Gemini API key. Guest visitors use only the bounded server-demo quota in V1.
- Key is never stored in PostgreSQL or server logs.
- Frontend may retain the key in `sessionStorage` only when user opts in for the current browser session.
- Key is sent over HTTPS in a dedicated request header and used only for that request.

The UI must clearly show remaining guest/user **Free demo quota**. Logged-in users additionally see **Use my API key**.

## 5. Version Granularity

- Every successful initial AI generation creates an immutable version.
- Every successful AI refinement creates an immutable version.
- Monaco keystrokes do **not** create versions.
- Manual editor changes are saved as the current draft using debounce.
- User can explicitly create a manual checkpoint.
- If the current draft has unsaved changes and the user starts AI refinement, backend automatically snapshots the current draft as a manual checkpoint before sending it to AI.
- Version rollback copies an old immutable version into the current draft; it does not delete later versions.

## 6. UI Direction — Frozen

### Visual language
- Tool-first, flat, matte dark UI.
- Primary palette: black / charcoal / crimson red.
- Secondary accents: controlled orange, cyan, yellow, and green for status only.
- Inspiration: high-contrast saturated graphic blocks and dark tactical/UI surfaces from the provided references.
- Personality-style color blocking may appear in small accents, tab indicators, empty states, and status chips, but must never distract from the editor/preview workflow.

### Explicitly forbidden
- Glassmorphism.
- `backdrop-filter: blur(...)` for cards or panels.
- Frosted/translucent floating panels.
- Neon glow as a primary design device.
- Large decorative gradients.
- Excessive pill-shaped UI.
- Overly animated landing page.

### Recommended design tokens
- `--bg: #0B0C0F`
- `--surface-1: #121318`
- `--surface-2: #191B21`
- `--border: #2A2D34`
- `--text: #F4F4F5`
- `--text-muted: #9B9DA5`
- `--primary: #E63B3B`
- `--primary-hover: #FF4D45`
- `--accent-orange: #F28C28`
- `--accent-cyan: #37C6D0`
- `--accent-yellow: #E8C547`
- `--success: #63C174`
- Radius: 6–10px for most components; 12px maximum for large containers.
- Typography: Inter/Geist-style sans-serif; JetBrains Mono or equivalent for code.

## 7. Generated Code Contract

V1 outputs only:

- React
- TypeScript
- Tailwind CSS

Generated projects must use a controlled template. The AI is not allowed to invent arbitrary package dependencies.

Allowed runtime dependencies in generated output:
- React
- React DOM
- Tailwind CSS
- `lucide-react` for icons

The application owns the base `package.json`, `vite.config`, `tsconfig`, and bootstrap files. AI primarily produces:
- `src/App.tsx`
- `src/components/*.tsx`
- `src/index.css`

Recommended max generated component files in V1: 8.

External images in the source design are represented with safe placeholders unless an accessible asset URL is explicitly available. V1 does not implement full asset extraction from screenshots.

## 8. Main Pages

- `/` landing + public examples
- `/login`
- `/register`
- `/dashboard`
- `/projects/new`
- `/projects/:projectId`

Workspace `/projects/:projectId` contains:
- compact top toolbar;
- source/input panel;
- Monaco code editor;
- Sandpack live preview;
- AI refine drawer/panel;
- versions/history panel;
- download ZIP action.

## 9. V1 Out of Scope

Do not implement unless explicitly promoted into scope later:
- Vue/Angular/Svelte generation
- plain HTML-only generation mode
- mobile native generation
- Figma plugin
- full Figma node-tree reconstruction
- Figma OAuth
- website crawling beyond one target page
- authenticated website capture
- CAPTCHA bypass
- browser agent workflows
- video-to-code
- multi-page site inference
- AI image generation
- arbitrary npm dependencies
- collaborative editing
- teams/organizations
- billing/subscriptions
- password reset/email verification
- Google/GitHub OAuth
- Kubernetes/Kafka/RabbitMQ/microservices

## 10. Definition of Done

The project is considered resume-ready when all of the following are true:

1. A visitor can generate from an image/screenshot without registering, with a 3-request guest quota.
2. User can register and log in for persistent project storage.
3. User or guest can create a project from image/screenshot.
4. Gemini returns valid constrained React/TS/Tailwind files.
5. Files are editable in Monaco and runnable in Sandpack.
6. User or guest can issue a natural-language refinement and receive a new version while quota/credentials permit.
7. Manual edits are persisted as draft for the lifetime of the owning project.
8. Persistent user version history and restore work; guest history works only within the temporary project lifetime.
9. Website URL input works for public pages.
10. Figma frame input works with user-provided token.
11. Wireframe mode works through the shared image pipeline.
12. Download ZIP produces a project that runs with documented commands, including for guest users.
13. Public examples can be opened without login.
14. Demo API usage is rate-limited and can be disabled globally.
15. Guest projects expire and are cleaned up after 24 hours.
16. Docker Compose starts the local application stack.
17. CI runs backend and frontend checks.
18. Hosted frontend/backend/database are reachable through public URLs.
19. README accurately describes architecture, setup, limitations, and deployment.

