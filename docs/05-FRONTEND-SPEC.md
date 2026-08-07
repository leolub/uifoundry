# 05 — Frontend & UI Specification

## 1. Design Identity

The application is a serious developer tool with a distinctive but restrained visual identity.

### Core style
- matte near-black background;
- solid charcoal surfaces;
- thin hard borders;
- high-contrast white text;
- crimson red as primary interactive accent;
- orange/cyan/yellow used only as secondary signal colors;
- flat color blocks inspired by bold graphic/anime UI composition, used sparingly.

### Do not use
- glass cards;
- blur behind panels;
- translucent floating UI;
- glowing neon borders;
- giant gradient headlines;
- overly soft SaaS pastel look;
- excessive 20–30px radii.

## 2. Global Layout

Desktop-first because Monaco + preview is the main product.

### Landing
- simple top nav;
- concise hero copy;
- one strong red **Try without account** CTA;
- secondary Sign in action;
- guest quota text such as `3 free generations — no account required`;
- four input-mode tiles;
- public example cards;
- architecture/feature strip near bottom.

No elaborate marketing animations.

### Dashboard
- slim left sidebar or compact top navigation;
- project list/grid;
- “New Project” primary button;
- quota indicator;
- minimal account menu.

### New Project
Available to both guests and authenticated users. Guest UI must clearly state that the project is temporary (24 hours) and show remaining free AI requests.

Four flat input cards/tabs:
- Image
- Website
- Figma
- Wireframe

The card selection uses a red hard accent line/block, not a glow.

## 3. Workspace

Recommended desktop layout:

```text
+--------------------------------------------------------------+
| project name | saved state | quota | Save Version | Download |
+----------------+----------------------+-----------------------+
| source/files   | Monaco Editor        | Live Preview          |
| versions       |                      |                       |
| AI refine      |                      |                       |
+----------------+----------------------+-----------------------+
```

Practical implementation may use:
- left rail 240–280px;
- editor/preview split adjustable or approximately 45/55;
- source/version/refine as tabs within left rail to avoid clutter.

### Workspace controls
- Files tab
- Source tab
- Versions tab
- AI Refine tab
- Preview viewport buttons: Desktop / Tablet / Mobile
- Reload preview
- Save Version
- Download ZIP

## 4. Monaco
- Dark theme matching application shell.
- Tabs for generated files.
- Unsaved/dirty indicator.
- Do not expose arbitrary filesystem operations in V1.
- User may edit existing generated files; optional creation/deletion of files is deferred unless implementation is trivial and safe.

## 5. Sandpack Preview
- Preview area itself defaults to white unless generated page defines otherwise.
- Show runtime/build error panel.
- Responsive viewport presets.
- Reload action.

## 6. AI Refine UI
Not a full chat product.

Use:
- multiline instruction box;
- Generate/Apply button;
- current quota text;
- for authenticated users only: credential mode selector `Free Demo / My Gemini Key`;
- for guests: server-demo mode only, with `Sign in to use your own key and save permanently` secondary message;
- last 3–5 refine instructions as compact history items.

Avoid chat bubbles and assistant avatars.

## 7. Guest + Auth UI
- generation must not be blocked by a login wall;
- guest receives 3 free AI requests per rolling 24 hours;
- workspace header shows `Guest · N/3 free generations left` and `Sign in to save permanently`;
- guest can download ZIP and use the temporary workspace;
- login/register remains available for durable projects and BYOK;
- simple centered solid auth card;
- no glass;
- email/password;
- clear validation;
- link between login/register.

## 8. Color Use
Suggested primary tokens are in MASTER-SPEC.

Rules:
- Red = primary action/active state.
- Orange = warning/progress emphasis.
- Cyan = informational state / link accent when differentiation is needed.
- Green = success only.
- Yellow = warning only.

Do not use all accents in one component.

## 9. Motion
- 120–180ms transitions for hover/tab/panel.
- No background motion.
- No parallax.
- No spring-heavy animation library required.

## 10. Responsive Behavior
- Landing/dashboard should work on mobile.
- Workspace is desktop/tablet optimized.
- On small screens, editor and preview become tabs rather than side-by-side.
