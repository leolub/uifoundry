# 06 — AI Generation Specification

## 1. Provider Policy

V1 provider: Gemini.

Configuration:
- `AI_PROVIDER=GEMINI`
- `AI_MODEL=gemini-2.5-flash` initially

Do not hard-code the model string in controller code.

## 2. Credential Modes

### SERVER_DEMO
Backend environment variable supplies key.
Subject to hard daily caps.

### BYOK
`X-Gemini-Api-Key` header supplies key.
Never persist or log it.

## 3. Privacy Notice

The UI/docs must state that free-tier third-party AI services may process uploaded images according to the provider's free-tier data policy. Do not promise privacy properties the provider does not guarantee.

## 4. Initial Generation Input

AI receives:
- normalized design image;
- input mode;
- fixed target framework contract;
- design reconstruction instructions;
- allowed files/dependencies;
- output JSON schema.

## 5. Output Schema

AI must return machine-parseable JSON similar to:

```json
{
  "summary": "A dark operations dashboard with sidebar, filters and map panel.",
  "files": [
    {"path":"src/App.tsx","content":"..."},
    {"path":"src/index.css","content":"..."},
    {"path":"src/components/Sidebar.tsx","content":"..."}
  ],
  "warnings": ["Original map imagery was replaced with a styled placeholder."]
}
```

No markdown fences.

## 6. Generation Prompt Rules

System-level requirements to embed in prompt:

- Output React + TypeScript + Tailwind only.
- Produce a static but responsive reconstruction.
- Prefer semantic HTML.
- Use components where repetition is clear.
- Use only allowed dependencies.
- Do not import nonexistent local assets.
- Do not use remote scripts.
- Do not create backend code.
- Avoid placeholder explanatory text unless the source contains it.
- Reproduce visual hierarchy, spacing, colors, typography, layout, borders, and major controls.
- If exact imagery cannot be recreated, use visually neutral placeholders and include a warning.
- Code must compile in the provided Vite/Sandpack template.

## 7. Wireframe Prompt Mode

When `inputType=WIREFRAME`:
- prioritize layout and intended component semantics;
- choose a clean neutral visual system;
- do not attempt to reproduce sketch artifacts;
- infer common UI controls conservatively.

## 8. Refinement Input

AI receives:
- current file snapshot;
- user instruction;
- source image reference when useful;
- same dependency/file constraints.

V1 refinement returns a **complete file snapshot**, not a patch/diff.

Reason: simpler validation, rollback, and preview consistency. Token optimization through patch generation is deferred.

## 9. Output Validation

Reject provider result if:
- invalid JSON;
- missing `src/App.tsx`;
- unsafe path;
- too many files;
- unsupported extension;
- total payload too large;
- clearly empty content.

On validation failure:
- mark AI run FAILED;
- keep current project unchanged;
- return sanitized error.

## 10. Prompt Versioning

Maintain prompt templates as versioned source files/classes.
Record a prompt version string in `ai_runs` metadata if convenient.

Do not edit prompts ad hoc in controllers.

## 11. Recommended Initial Prompt Strategy

Start with one robust generation prompt and one refinement prompt.
Do not create complex multi-agent planning/review loops in V1.

Optional later improvement:
- one lightweight repair call only when generated code fails a deterministic validation step.
