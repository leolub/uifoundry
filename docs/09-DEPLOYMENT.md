# 09 — Deployment Plan

## Target Low-Cost Stack

### Frontend
Cloudflare Pages
- static Vite build;
- inexpensive/free hobby hosting;
- environment variable for backend API base URL.

### Backend
Render free Web Service initially
- Docker deployment;
- expected idle spin-down/cold start on free plan;
- no local persistent file storage.

UI should tolerate backend cold start and show a normal loading message rather than immediately reporting failure.

### Database
Neon PostgreSQL free tier initially.
Reason: free project with no 30-day database expiration; adequate for portfolio-sized metadata/code snapshots.

### Object Storage
Cloudflare R2 Standard.
Store:
- uploaded screenshots;
- normalized website screenshots;
- normalized Figma frame images;
- wireframe images.

Do not store generated ZIPs.

### Website Capture
Cloudflare Browser Run screenshot endpoint.
Free-tier browser time is sufficient for low-volume portfolio use; when unavailable/quota exhausted, website input returns a clear provider-limit error while image/Figma inputs remain functional.

### AI
Gemini free-tier project for limited demo quota.
No paid OpenAI/Codex key deployed.

## Environment Variables

Backend example:
```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
JWT_SECRET=
FRONTEND_ORIGIN=

R2_ENDPOINT=
R2_BUCKET=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=

GEMINI_API_KEY=
GEMINI_MODEL=gemini-3.6-flash
DEMO_AI_ENABLED=true
DEMO_AI_REQUESTS_PER_GUEST_PER_24H=3
DEMO_AI_REQUESTS_PER_USER_PER_DAY=3
DEMO_AI_REQUESTS_PER_IP_PER_24H=6
DEMO_AI_REQUESTS_GLOBAL_PER_DAY=30
GUEST_PROJECT_TTL_HOURS=24

CLOUDFLARE_ACCOUNT_ID=
CLOUDFLARE_BROWSER_API_TOKEN=
```

Figma token is user-provided; do not define shared Figma PAT unless owner explicitly chooses to support only owner-controlled designs.

Frontend:
```text
VITE_API_BASE_URL=
```

## Local Docker Compose
Must provide:
- PostgreSQL container;
- backend container/profile;
- frontend may run through npm dev outside Docker during active development, but final Compose should have a documented path to start full stack.

Do not run heavyweight AI models locally.

## Deployment Order
1. create Neon DB;
2. create R2 bucket;
3. configure Cloudflare Browser Run token;
4. deploy backend Render;
5. deploy frontend Cloudflare Pages;
6. configure CORS/origins;
7. seed public examples;
8. run production smoke test.

## Operational Limitations to Document
- Render free backend cold start.
- Gemini free-tier quota and provider policy; anonymous generation is bounded by guest/IP/global caps.
- Browser Run free-tier quota.
- Figma access token required for private frames.
- generated UI is an approximation, not guaranteed pixel-perfect.
