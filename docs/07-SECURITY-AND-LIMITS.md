# 07 — Security, Secrets, and Usage Limits

## 1. Secret Rules
Never commit:
- Gemini key
- Figma token
- Cloudflare API token
- R2 secret
- database password
- JWT signing secret

Use `.env.example` with placeholders only.

## 2. BYOK
- Accept Gemini key only over HTTPS.
- Use dedicated header.
- Add header to logging redaction list.
- Never write key to DB.
- Never include key in exception message.
- Frontend stores key only in memory or sessionStorage if user explicitly chooses session persistence.

## 3. Figma Token
Same transient-secret rules as BYOK Gemini key.

## 4. Authentication
- BCrypt password hashing.
- JWT access token.
- Refresh token rotation/revocation recommended.
- Refresh token stored as hash server-side.
- Cookie should be Secure/HttpOnly where applicable.
- CORS allowlist must contain only deployed frontend and local dev origins.

## 5. Guest Identity and AI Limits
Guest access must not require registration.

Guest identity:
- issue a cryptographically random opaque guest-session cookie on first guest write action;
- cookie: Secure, HttpOnly, SameSite=Lax in production;
- store only a one-way hash/HMAC-derived identifier server-side, never the raw cookie value;
- do not use invasive browser fingerprinting.

Recommended configurable controls:
- 3 demo AI requests/guest/rolling 24 hours;
- 3 demo AI requests/registered user/day;
- 6 demo AI requests/IP/rolling 24 hours as abuse fallback;
- 30 demo AI requests/global/day;
- short-term burst throttle per principal/IP;
- global environment kill switch `DEMO_AI_ENABLED=false`.

Each initial generation or refinement that reaches the AI provider consumes one request. Validation failures before provider invocation do not consume quota. Provider-started requests may count even if the provider later errors, to protect cost.

Guest projects expire after 24 hours and must be deleted from PostgreSQL/R2 by a scheduled cleanup job.

All values must be environment configuration, not constants buried in business code.

## 6. Upload Limits
- 10 MB source upload max.
- MIME/type validation.
- Image decode validation.
- Resize oversized dimensions during normalization.
- Random generated object names; never trust user filename for path.

## 7. SSRF Protection for Website Input
Before calling browser screenshot provider:
- scheme allowlist http/https;
- reject localhost names;
- DNS resolve and reject private/link-local/loopback ranges;
- revalidate redirects where possible;
- timeout;
- prohibit arbitrary headers/cookies from user.

## 8. Generated Code Safety
Sandpack runs generated frontend code in browser sandbox context, but still constrain output:
- no arbitrary npm dependencies;
- no external scripts;
- no secret injection;
- do not pass authenticated backend credentials into generated preview;
- generated code cannot call privileged UIFoundry endpoints with embedded tokens.

## 9. ZIP Safety
- allowlisted paths only;
- prevent zip-slip path traversal;
- sanitized project name;
- no secrets.

## 10. Logging
Allowed:
- request id
- user id
- project id
- provider/model
- run timing
- sanitized error code

Avoid logging:
- raw screenshots
- prompt payload containing user design when unnecessary
- generated code at INFO level
- API keys/tokens/JWTs/passwords
