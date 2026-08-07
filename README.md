# UIFoundry

UIFoundry is a design-to-code workspace that turns screenshots, public web
pages, Figma frames, and wireframes into editable React + TypeScript + Tailwind
interfaces. This repository is the first independent project inside the parent
`githubProject` workspace.

## Repository layout

```text
uifoundry/
├── frontend/              React, TypeScript, Vite, Tailwind
├── backend/               Java 21, Spring Boot REST API
├── docs/                  Product and engineering specifications
├── .github/workflows/     Continuous integration
├── docker-compose.yml     Local full-stack services
└── MASTER-SPEC.md         Frozen product direction
```

The parent `githubProject` directory is only a workspace for multiple future
repositories. Initialize Git and push from this `uifoundry` directory so each
project keeps independent history, issues, deployment, and secrets.

## Current phase

Phase 1 repository scaffold is in place: a baseline dark UI, backend health
endpoint, PostgreSQL service, Docker configuration, and CI checks. Product
features such as authentication, project persistence, AI generation, and
external integrations belong to later phases in `docs/10-DEVELOPMENT-PLAN.md`.

## Prerequisites

- Java 21
- Maven 3.6.3 or newer (Maven 3.9 recommended)
- Node.js 22 or another Vite-supported LTS release
- Docker Desktop

## Run locally

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Backend (with PostgreSQL running):

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

Or start the complete scaffold:

```bash
docker compose up --build
```

Open <http://localhost:5173>. The API health endpoint is
<http://localhost:8080/api/health>.

On Windows PowerShell systems that block `npm.ps1`, use `npm.cmd` in place of
`npm`. Copy `.env.example` to `.env` only when overriding local defaults; `.env`
and real credentials are intentionally ignored.

## Checks

```bash
cd frontend && npm ci && npm run build
cd backend && mvn test
docker compose config
```

## Documentation

Start with [MASTER-SPEC.md](MASTER-SPEC.md), then read
[the product requirements](docs/01-PRD.md),
[technical design](docs/02-TECHNICAL-DESIGN.md), and
[development plan](docs/10-DEVELOPMENT-PLAN.md). These files are versioned with
the code and are the source of truth.

## Deployment target

The planned production stack is Cloudflare Pages (frontend), Render (backend),
Neon PostgreSQL, Cloudflare R2, Cloudflare Browser Run, and Gemini. See
[docs/09-DEPLOYMENT.md](docs/09-DEPLOYMENT.md). No production service is
provisioned in this scaffold.
