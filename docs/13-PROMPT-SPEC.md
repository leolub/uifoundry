# 13 — Product AI Prompt Specification

This document specifies prompts used **inside UIFoundry**, not prompts given to Codex.

## Generation System Prompt — Requirements

The implementation should build a stable template containing the following ideas:

1. You are generating a runnable React + TypeScript + Tailwind interface from a visual reference.
2. Return only the required structured JSON response.
3. Match layout hierarchy, spacing, typography, colors, borders, and component organization.
4. Do not add business behavior that cannot be inferred.
5. Output must compile in the fixed UIFoundry Vite template.
6. Use only React, React DOM, Tailwind, and lucide-react.
7. Do not reference local files that are not included.
8. Do not install packages.
9. Use placeholders for source imagery that cannot be reconstructed.
10. Maximum 8 component files.

## Generation User Context

Include:
- input type;
- target viewport;
- any user-supplied short instruction (optional future field);
- image payload.

## Refinement System Prompt

Requirements:
1. You are editing an existing runnable project.
2. Preserve unaffected behavior/layout.
3. Apply only the user's requested changes plus minimal required corrections.
4. Return a complete valid file snapshot using the same JSON schema.
5. Do not introduce unsupported dependencies.

## JSON Contract

```json
{
  "summary": "string",
  "files": [
    {"path":"src/App.tsx","content":"string"}
  ],
  "warnings": ["string"]
}
```

## Implemented Phase 4A Contract

The active versioned resource is `screenshot-to-react-v1.txt`. Gemini structured
output is constrained to the contract above. Before persistence, the backend
requires `src/App.tsx`, permits only `.ts`, `.tsx`, and `.css` beneath `src/`,
rejects traversal/absolute/duplicate paths, permits at most 10 total files, and
limits total source content to 500,000 characters. The screenshot remains the
primary input; the optional supplementary instruction is capped at 2,000
characters.

## Repair Strategy
Phase 4A performs no repair request. Malformed or unsafe output marks the
generation `FAILED` and returns a sanitized error. A bounded repair strategy may
be considered later with explicit quota accounting; open-ended loops remain
forbidden.
