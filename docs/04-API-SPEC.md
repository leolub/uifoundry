# 04 — REST API Specification

Base prefix: `/api/v1`

Exact DTO names may vary, but endpoint semantics are frozen.

## Auth

### `POST /api/v1/auth/register`
Request:
```json
{"email":"user@example.com","password":"...","displayName":"User"}
```
Response: authenticated user/session result.

### `POST /auth/login`
Request:
```json
{"email":"user@example.com","password":"..."}
```
Response:
```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {"id": "uuid", "email": "user@example.com", "createdAt": "..."}
}
```

### `POST /auth/refresh`
Uses refresh token cookie.

### `POST /auth/logout`
Revokes refresh token and clears cookie.

### `GET /auth/me`
Requires `Authorization: Bearer <accessToken>` and returns the current safe user profile.

## Public examples

### `GET /examples`
No auth.

### `GET /examples/{projectId}`
No auth. Returns read-only project data required for workspace preview.

## Guest Session

Guest identity is an opaque Secure/HttpOnly/SameSite=Lax cookie created lazily on the first guest write action. The raw value is never stored in PostgreSQL; only a server-side hash is used for ownership/quota checks.

## Projects

### `GET /projects`
Auth required. Return current registered user's persistent projects. V1 has no guest dashboard/list endpoint.

### `POST /projects`
Auth optional.
- authenticated request -> persistent user project;
- unauthenticated request -> temporary guest project expiring after 24 hours.
Request:
```json
{
  "name": "Dashboard Recreation",
  "inputType": "IMAGE"
}
```

### `GET /projects/{id}`
Owner only unless public example. Owner may be the authenticated user or matching guest-session cookie.

### `PATCH /projects/{id}`
Owner only. Rename only in V1; guest rename is allowed during temporary lifetime.

### `DELETE /projects/{id}`
Owner only; supports registered or matching guest owner.

## Input APIs

Phase 3B supports authenticated `IMAGE_UPLOAD` sources through these endpoints:

### `PUT /projects/{id}/source-image`
Owner-only `multipart/form-data` upload or replacement with a `file` field.
Accepts PNG, JPEG, and WebP up to the configured limit (10 MB by default).

### `GET /projects/{id}/source-image`
Returns safe source metadata. It never returns a storage key or filesystem path.

### `GET /projects/{id}/source-image/content`
Returns the protected image bytes with their persisted content type. Clients must
send the Bearer token; the endpoint is not public.

### `DELETE /projects/{id}/source-image`
Deletes source metadata and the stored image.

All four endpoints return ownership-safe `404` responses for projects owned by a
different authenticated user. Website, Figma, and wireframe endpoints below are
future contracts and are not implemented in Phase 3B.

### `POST /projects/{id}/input/website`
```json
{"url":"https://example.com"}
```

### `POST /projects/{id}/input/figma`
Headers:
- `X-Figma-Token: <token>`

Body:
```json
{"figmaUrl":"https://www.figma.com/design/...?...node-id=..."}
```

Token is never persisted.

### `GET /projects/{id}/input`
Returns metadata and signed/temporary asset URL as needed.

## Generation APIs

### `POST /projects/{id}/generate`
Auth optional for server-demo generation. Guest ownership is validated by guest cookie.

Headers optional for authenticated BYOK only:
- `X-Gemini-Api-Key` for BYOK

Body:
```json
{
  "credentialMode": "SERVER_DEMO"
}
```
or
```json
{
  "credentialMode": "BYOK"
}
```

Response may be synchronous initially if provider latency is acceptable; UI must still show generation status. If later changed to async polling, preserve service contract through explicit run resource.

Recommended response:
```json
{
  "runId": "uuid",
  "status": "SUCCESS",
  "versionNumber": 1,
  "files": {"src/App.tsx":"..."},
  "summary": "...",
  "warnings": []
}
```

### `POST /projects/{id}/refine`
Auth optional for server-demo refinement while guest quota remains.

Optional header for authenticated BYOK only:
- `X-Gemini-Api-Key`

Body:
```json
{
  "credentialMode": "SERVER_DEMO",
  "instruction": "Make the sidebar narrower and use red as the primary accent."
}
```

## Draft APIs

### `PUT /projects/{id}/draft`
Body:
```json
{
  "files": {
    "src/App.tsx": "...",
    "src/index.css": "..."
  },
  "clientRevision": 12
}
```

Use optimistic revision/check if implemented. Do not silently overwrite a newer server draft.

### `POST /projects/{id}/versions/checkpoint`
Creates manual version.
Optional body:
```json
{"note":"Before changing navigation"}
```

## Version APIs

### `GET /projects/{id}/versions`
Metadata list; does not need return every file snapshot.

### `GET /projects/{id}/versions/{versionNumber}`
Returns snapshot.

### `POST /projects/{id}/versions/{versionNumber}/restore`
Copies snapshot into current draft.

## Export

### `GET /projects/{id}/export.zip`
Returns ZIP download.

## Usage

### `GET /usage/ai`
No auth required. Returns quota state for the current principal (guest cookie or authenticated user):
```json
{
  "principalType": "GUEST",
  "demoEnabled": true,
  "usedInWindow": 1,
  "demoLimit": 3,
  "remaining": 2,
  "window": "ROLLING_24H",
  "globalAvailable": true,
  "byokAvailable": false
}
```
For authenticated users, `principalType=USER`, the configured user daily limit applies, and `byokAvailable=true`.

## Common HTTP Statuses
- 200/201 success
- 400 invalid request
- 401 unauthenticated
- 403 unauthorized/forbidden
- 404 resource not found
- 409 revision/version conflict
- 413 upload too large
- 422 semantic validation error
- 429 quota/rate limit
- 502 external provider failure
