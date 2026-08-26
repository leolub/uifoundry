# 03 — Database Design

Database: PostgreSQL.

Use UUID primary keys unless implementation has a strong reason not to.

## 1. `users`

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| email | varchar unique | normalized lowercase |
| password_hash | varchar | BCrypt |
| display_name | varchar nullable | optional |
| created_at | timestamptz | |
| updated_at | timestamptz | |

## 2. `refresh_tokens`

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK users | indexed |
| token_hash | varchar | never store raw refresh token |
| expires_at | timestamptz | |
| revoked_at | timestamptz nullable | |
| created_at | timestamptz | |

## 3. `projects`

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| owner_user_id | uuid FK users nullable | set for persistent registered-user projects |
| guest_session_hash | varchar nullable | set for temporary guest projects; never store raw guest cookie |
| expires_at | timestamptz nullable | required for guest projects; null for persistent user projects/examples |
| name | varchar | |
| slug | varchar nullable | used for public examples if desired |
| input_type | varchar | IMAGE/WEBSITE/FIGMA/WIREFRAME |
| status | varchar | DRAFT/READY/ERROR |
| current_files_json | jsonb | mutable working draft |
| current_version_number | int | default 0 |
| is_public_example | boolean | default false |
| created_at | timestamptz | |
| updated_at | timestamptz | |

Ownership invariant:
- registered project: `owner_user_id` set, `guest_session_hash` null, `expires_at` null;
- guest project: `owner_user_id` null, `guest_session_hash` set, `expires_at` set;
- seeded public example: both owner fields null and `is_public_example=true`.

Indexes:
- `(owner_user_id, updated_at desc)`
- `(guest_session_hash, expires_at)` for temporary guest access/cleanup
- partial/index on `is_public_example`
- index on `expires_at` for cleanup job

## 4. `project_sources`

One active normalized visual source per project in V1. Phase 3B implements only
`IMAGE_UPLOAD`; Website and Figma sources remain future phases. Image bytes are
stored outside PostgreSQL.

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| project_id | uuid FK unique | cascades on project deletion |
| source_type | varchar | currently `IMAGE_UPLOAD` |
| storage_key | varchar unique | generated opaque storage key; never returned by API |
| original_filename | varchar | metadata only, never used as a filesystem path |
| content_type | varchar | |
| size_bytes | bigint | positive file size |
| created_at | timestamptz | |
| updated_at | timestamptz | |

The current local adapter stores files under a configurable ignored directory.
The same `storage_key` boundary can later address Cloudflare R2 without changing
the controller or persisted public metadata. Never store Figma PAT or AI API key
in source metadata.

## 5. `project_versions`

Immutable snapshots.

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| project_id | uuid FK | indexed |
| version_number | int | unique per project |
| version_type | varchar | INITIAL_AI/AI_REFINE/MANUAL/MANUAL_AUTO |
| instruction | text nullable | refinement request or manual note |
| files_json | jsonb | immutable snapshot |
| created_by_user_id | uuid FK users nullable | |
| created_at | timestamptz | |

Constraint: unique `(project_id, version_number)`.

## 6. `ai_runs`

Tracks usage, status, errors, and quota accounting.

| Column | Type | Notes |
|---|---|---|
| id | uuid PK | |
| project_id | uuid FK | |
| user_id | uuid FK nullable | set for authenticated user runs |
| guest_session_hash | varchar nullable | set for guest runs |
| run_type | varchar | INITIAL/REFINE |
| credential_mode | varchar | SERVER_DEMO/BYOK |
| provider | varchar | GEMINI |
| model | varchar | configured model name |
| status | varchar | RUNNING/SUCCESS/FAILED |
| instruction | text nullable | |
| request_ip_hash | varchar nullable | optional privacy-preserving quota support |
| prompt_tokens | bigint nullable | if provider reports |
| output_tokens | bigint nullable | |
| error_code | varchar nullable | sanitized |
| resulting_version_id | uuid nullable | |
| started_at | timestamptz | |
| completed_at | timestamptz nullable | |

Identity invariant: exactly one of `user_id` or `guest_session_hash` is set for non-system AI runs.

Indexes:
- `(user_id, started_at)` for registered-user daily quota
- `(guest_session_hash, started_at)` for guest rolling quota
- `(request_ip_hash, started_at)` for abuse throttling
- `(started_at, credential_mode)` for global quota

## 7. Guest Cleanup

A scheduled cleanup job deletes guest projects whose `expires_at` is in the past. Cleanup follows the same DB/R2 deletion rules as explicit project deletion. Failure to delete an R2 object is logged for later cleanup.

## 8. Deletion Behavior

Deleting a project:
1. authorize owner;
2. delete DB child rows in transaction;
3. schedule/delete R2 input asset;
4. generated code snapshots are deleted through cascade;
5. failure to delete R2 object must be logged for cleanup but should not resurrect DB data.

## 9. JSONB File Format

Recommended format:

```json
{
  "src/App.tsx": "...",
  "src/index.css": "...",
  "src/components/Header.tsx": "..."
}
```

Do not normalize each generated source file into a separate DB row in V1.
