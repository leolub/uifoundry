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

## Repair Strategy
If parse/validation fails:
- first perform deterministic cleanup if harmless (e.g. remove accidental markdown fence);
- otherwise at most one repair AI request is allowed if quota mode permits;
- do not create an open-ended agent loop.
