# 01 — Product Requirements Document

## 1. Product Objectives

### Primary objectives
- Generate usable React + TypeScript + Tailwind code from a visual reference.
- Make generated output immediately editable and runnable in-browser.
- Support iterative natural-language changes.
- Persist projects and versions.
- Support several useful input paths without implementing several separate generation engines.
- Be publicly deployable at approximately $0/month under hobby usage.

### Non-objectives
- Compete with a production website builder.
- Generate complete multi-page production systems.
- Perfect pixel-level reproduction.
- Train or fine-tune models.
- Support arbitrary frontend frameworks.

## 2. Personas

### Registered developer
Needs to create projects, generate code, refine, edit, save, restore, and export.

### Anonymous reviewer / guest
Needs to validate that the project works without creating an account. Can create a temporary project, make up to 3 demo AI requests in a rolling 24-hour window, edit/preview the result, and download ZIP. Persistent storage requires registration.

## 3. Functional Requirements

### FR-01 Registration
- Email + password.
- Unique normalized email.
- Password minimum 8 characters.
- Password hashed with BCrypt.
- No email verification in V1.

### FR-02 Login
- Correct credentials return authenticated session/token state.
- Invalid credentials return generic error.
- Refresh flow supported.

### FR-03 Public Examples
Anonymous user can:
- view example list;
- open an example workspace in read-only mode;
- switch between generated files;
- run live preview;
- view original design input;
- view generation metadata.

### FR-04 Guest Trial + Create Project
- Registration is not required for the real generation workflow.
- A visitor receives a secure opaque guest-session cookie on first write action.
- Guest can create temporary projects using any of the four modes below.
- Guest can make up to 3 server-demo AI requests per rolling 24-hour window; initial generation and each AI refinement each consume one request.
- Guest can edit, preview, restore versions inside the temporary project, and download ZIP.
- Guest projects expire after 24 hours and are not shown in the persistent user dashboard.
- Registered users create persistent projects.

Supported project modes:
- IMAGE
- WEBSITE
- FIGMA
- WIREFRAME

### FR-05 Image Input
- Accept PNG/JPEG/WebP.
- Upload via file picker, drag/drop, clipboard paste.
- Max size: 10 MB before normalization.
- Invalid file types rejected before upload when possible.

### FR-06 Website Input
- Accept only `http://` and `https://` URLs.
- Only public, unauthenticated pages.
- Backend validates URL to reduce SSRF risk.
- Hosted browser service captures screenshot using fixed desktop viewport.
- Default viewport: 1440x900.
- Timeout: 15 seconds.
- One page only.

### FR-07 Figma Input
- User pastes Figma frame URL containing file key and node id.
- User supplies Figma token if required.
- Token is not persisted.
- Backend calls Figma image render endpoint and obtains PNG.
- No full node-tree parsing in V1.

### FR-08 Wireframe Input
- Same file constraints as image input.
- `inputMode=WIREFRAME` changes AI instructions toward semantic layout reconstruction rather than pixel/style matching.

### FR-09 Input Normalization
Every source produces:
- normalized PNG/JPEG asset;
- width/height;
- source type;
- metadata JSON;
- R2 storage key.

### FR-10 Initial Generation
- User selects Generate.
- Server chooses demo Gemini key or BYOK key according to request.
- AI receives normalized image and generation contract.
- AI returns structured JSON, not markdown fences.
- Backend validates file paths and allowed file count.
- Generated files are saved as current project draft.
- Successful initial generation creates immutable Version 1.

### FR-11 Live Preview
- Sandpack renders current generated files.
- Runtime errors appear in UI without crashing workspace.
- Preview can refresh/reload.
- Preview supports responsive width presets: desktop/tablet/mobile.

### FR-12 Code Editing
- Monaco Editor supports file tabs.
- User can directly edit files.
- Changes update preview.
- Draft autosaves after debounce (target 1000–2000ms).
- Autosave failure shown unobtrusively.

### FR-13 AI Refinement
User can enter requests such as:
- “make the sidebar narrower”
- “change primary color to red”
- “make cards square instead of rounded”

Requirements:
- AI sees current files, user instruction, and source image when useful.
- If draft is dirty, create auto-checkpoint first.
- Successful refinement creates a new immutable version.
- Failure must not overwrite current working files.

### FR-14 Manual Checkpoint
- User can click Save Version.
- Creates immutable manual version of current files.

### FR-15 Version History
- Display version number, type, timestamp, instruction summary.
- User can inspect any version.
- Restore copies selected version into current draft.
- Later versions remain intact.

### FR-16 Download ZIP
- Download creates standard Vite React TypeScript project.
- Contains fixed configuration files + current generated source files.
- Must run via documented commands.
- Never include API keys, backend secrets, or user tokens.

### FR-17 Project Management
Registered-user dashboard supports:
- create;
- list own persistent projects;
- rename;
- open;
- delete.

Guest projects are temporary, accessible only through the current guest session/project URL, and expire after 24 hours. No guest dashboard is required in V1.

No folders/tags/search required in V1.

### FR-18 Quotas
- Guest server-demo quota: 3 AI requests per rolling 24 hours.
- Registered-user server-demo quota: configurable, default 3 AI requests/day.
- Per-IP abuse cap: configurable, default 6 demo AI requests per rolling 24 hours.
- Global server-demo cap: configurable, default 30 AI requests/day.
- Each initial generation or refinement that reaches the AI provider counts as one request; local validation failures do not consume quota.
- BYOK is available to logged-in users and bypasses demo-token quota but still uses basic abuse throttling.
- UI shows remaining quota before generation.

### FR-19 Example Projects
- At least 3 seeded public examples.
- Prefer visually distinct examples: dashboard, landing page, wireframe/mockup.
- Examples are read-only.

## 4. Non-functional Requirements

### Reliability
- Failed AI call does not corrupt current code.
- Failed input normalization does not create incomplete project state unless status clearly indicates failure.

### Security
- Never persist BYOK keys or Figma tokens.
- Never log Authorization headers, AI key header, Figma token, JWT, password.
- Validate generated paths against an allowlist.
- Sanitize ZIP filenames.

### Performance targets
These are goals, not hard SLAs:
- Standard non-AI API: <500ms excluding cold start.
- Project draft save: <1s excluding cold start.
- Input upload under normal broadband: reasonable progress feedback.
- AI generation: asynchronous-feeling UI with visible status; no hard completion guarantee.

### Accessibility
- Keyboard navigation for major controls.
- Visible focus states.
- Color contrast suitable for dark UI.

## 5. Acceptance Criteria
A feature is complete only when:
1. happy path works;
2. expected invalid inputs are handled;
3. backend test exists where logic is non-trivial;
4. UI has loading/error state;
5. no secret is committed;
6. docs are updated if behavior/API changed.
