# 12 — Environment & Local Setup Plan

## Disk Strategy
Because local system drive space may be constrained, keep project caches/workspaces off the system drive where practical.

Recommended Windows layout:
```text
F:\Dev\UIFoundry\
F:\DevCaches\npm\
F:\DevCaches\maven\
F:\DevCaches\docker\   (only if Docker Desktop relocation is configured safely)
```

Do not start by downloading local AI model weights; this project uses remote Gemini API.

## Required Tools
- Git
- Java 21 JDK
- Maven Wrapper (prefer repository wrapper)
- Node.js current LTS
- npm/pnpm (choose one during scaffold and freeze; npm is simplest)
- Docker Desktop
- PostgreSQL via Docker for local development
- VS Code/IntelliJ as preferred

## Local Services
- frontend: `localhost:5173`
- backend: `localhost:8080`
- postgres: Docker mapped locally

## External Accounts Needed Eventually
- Google AI Studio / Gemini API project
- Cloudflare account (Pages, R2, Browser Run)
- Neon account
- Render account
- Figma account + personal access token for development test

## `.env.example`
Repository must include placeholders and setup comments, never real values.

## First-Day Validation Checklist
Before implementing core features, validate:
- `java -version`
- `node -v`
- Docker starts
- local PostgreSQL connection works
- Gemini smoke test succeeds
- Sandpack sample runs
- Figma image export smoke test succeeds
- Browser Run screenshot smoke test succeeds
