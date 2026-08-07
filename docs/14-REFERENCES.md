# 14 — References and Inspiration

These are references, not code to copy wholesale.

## Product inspiration
- screenshot-to-code — https://github.com/abi/screenshot-to-code
- Sandpack — https://github.com/codesandbox/sandpack
- Monaco Editor — https://github.com/microsoft/monaco-editor

## APIs / Infrastructure
- Gemini API pricing — https://ai.google.dev/gemini-api/docs/pricing
- Gemini rate limits — https://ai.google.dev/gemini-api/docs/rate-limits
- Figma REST file/image endpoints — https://developers.figma.com/docs/rest-api/file-endpoints/
- Cloudflare Browser Run screenshot endpoint — https://developers.cloudflare.com/browser-run/quick-actions/screenshot-endpoint/
- Cloudflare Browser Run pricing — https://developers.cloudflare.com/browser-run/pricing/
- Cloudflare R2 pricing — https://developers.cloudflare.com/r2/pricing/
- Neon pricing — https://neon.com/pricing
- Render free services — https://render.com/docs/free

## Notes as of August 2026
- Gemini has free-tier model usage, but provider policies/limits may change and free-tier content handling differs from paid usage. Keep model and quotas configurable.
- Render free web services may sleep after inactivity, so client must tolerate cold starts.
- Render free Postgres is not selected because its free database lifecycle is unsuitable for a persistent resume project.
- Cloudflare R2 currently offers a useful free storage/operations allowance for small projects.
- Cloudflare Browser Run free plan has limited daily browser time suitable for low-volume URL screenshots.
