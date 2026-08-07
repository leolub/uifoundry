# 11 — UI Design System

## Design Goal
A professional developer tool with a compact, flat, dark visual system. The references indicate preference for:
- near-black matte backgrounds;
- hard-edged high-contrast blocks;
- crimson/red focal areas;
- dark navy/charcoal supporting surfaces;
- orange and cyan as controlled high-energy accents;
- graphic composition rather than translucent/glassy styling.

## CSS Variables

```css
:root {
  --bg: #0B0C0F;
  --surface-1: #121318;
  --surface-2: #191B21;
  --surface-3: #20232A;
  --border: #2A2D34;

  --text: #F4F4F5;
  --text-muted: #9B9DA5;
  --text-dim: #686B73;

  --primary: #E63B3B;
  --primary-hover: #FF4D45;
  --orange: #F28C28;
  --cyan: #37C6D0;
  --yellow: #E8C547;
  --green: #63C174;

  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 10px;
}
```

Exact color values may be tuned during visual implementation without changing the visual direction.

## Components

### Buttons
- Primary: solid red.
- Secondary: solid dark surface + border.
- Danger: red outline/solid depending context.
- No gradient buttons.

### Cards/Panels
- solid surface background;
- 1px border;
- subtle or no box-shadow;
- never translucent.

### Tabs
- compact;
- active state: red bottom/left block or short bar;
- no giant pills.

### Inputs
- dark solid background;
- visible border;
- red/cyan focus line is acceptable;
- no floating glass fields.

### Status chips
Small rounded chip allowed for statuses such as `Generated`, `Saving`, `Error`, but do not apply pill styling to all navigation.

## Graphic Accent Rule
One bold accent device per screen is enough. Examples:
- red vertical rail on active input mode;
- small diagonal red corner on selected project;
- segmented primary-color indicator near generation state.

Do not turn the whole application into a game/anime UI.

## Code Workspace
- outer shell dark;
- Monaco dark;
- Preview uses actual generated background;
- resizer/divider is visible but subtle;
- toolbar is compact and functional.
