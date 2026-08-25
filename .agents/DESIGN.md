---
name: CafeStory
description: Social blogging platform for Vietnam's cafe community — warm, intentional, community-first.
colors:
  primary: "#0f766e"
  primary-strong: "#115e59"
  accent: "#9f1239"
  rating: "#d97706"
  espresso: "#271310"
  coffee-muted: "#504442"
  background: "#f7f4ef"
  surface: "#fffdf8"
  surface-muted: "#efe7da"
  border: "#e1d7c8"
  foreground: "#1f2937"
  muted: "#6b7280"
  line-soft: "#d3c3c0"
typography:
  display:
    fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "2rem"
    fontWeight: 900
    lineHeight: 1.1
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 700
    lineHeight: 1.3
  title:
    fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1rem"
    fontWeight: 600
    lineHeight: 1.4
  body:
    fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.714
  label:
    fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 500
    lineHeight: 1.5
    letterSpacing: "0em"
rounded:
  sm: "6px"
  md: "8px"
  lg: "10px"
  xl: "14px"
  2xl: "18px"
  full: "9999px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-primary-hover:
    backgroundColor: "{colors.primary-strong}"
    textColor: "{colors.surface}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-outline:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.foreground}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-ghost-hover:
    backgroundColor: "{colors.surface-muted}"
    textColor: "{colors.primary}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  badge-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "4px 12px"
  badge-secondary:
    backgroundColor: "{colors.surface-muted}"
    textColor: "{colors.primary-strong}"
    rounded: "{rounded.md}"
    padding: "4px 12px"
  badge-rating:
    backgroundColor: "{colors.rating}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "4px 12px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.md}"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.md}"
    padding: "0 16px"
    height: "48px"
---

# Design System: CafeStory

## 1. Overview

**Creative North Star: "The Considered Roast"**

CafeStory is built for people who already know what they like — they don't need to be seduced by the interface, they need the interface to get out of the way. Every design decision is deliberate the way a single-origin roast profile is deliberate: each choice is made with a specific outcome in mind, nothing is added for decoration, and the result is judged by the experience it produces, not how it looks in a screenshot.

The system is warm without being sentimental. The palette draws from roasted tones and cafe environments — aged paper, dark espresso, muted brick — but warmth is delivered through color relationships and typographic weight, never through decorative layering or visual noise. The emotional temperature is "sitting down with your second coffee, not the first."

This system explicitly rejects: the noisy-blue corporate social feed (Facebook); the utilitarian directory feel (Yelp/TripAdvisor); the mass-produced AI-default warm-cream + teal + eyebrow-labels SaaS scaffold; and the engagement-metric-everywhere energy of Instagram clones. If the interface is drawing attention to itself, it has failed.

**Key Characteristics:**
- Single sans-serif (Inter) across all surfaces — no display pairing, no decorative type
- Restrained color: Bloom Teal for actions, Brick Chaff for emotion, Caramel Crack for ratings only
- Tonal depth: layers are separated by surface color shifts, not shadow stacks
- Sidebar-first navigation that expands on focus — chrome shrinks to let content breathe
- Dark mode is first-class, not an afterthought: Italian Roast backgrounds, not inverted pastels

## 2. Colors: The Roastery Palette

Named for the stages and materials of the roasting process. Each name carries the physical reality of the craft — green beans before heat, the bloom of first steam, the crack of caramelization, the char of a dark roast. Nothing here is arbitrary.

### Primary
- **Bloom Teal** (`#0f766e`): All primary actions — CTAs, active nav states, focus rings, interactive links. Named for the bloom phase: the first exhale of CO₂ from freshly roasted beans, where the flavor potential is released. The one color users associate with "do something here."
- **Maillard** (`#115e59`): Hover and pressed state for Bloom Teal. Named for the Maillard reaction — the deep, sustained heat that produces complex flavor. Darker, richer, more committed than the base.

### Secondary
- **Brick Chaff** (`#9f1239`): Emotional accent — liked-state icons (heart filled), destructive actions, alert banners. Named for the red-brick hue of spent chaff from the roasting drum. Used sparingly; its appearance signals weight.

### Tertiary
- **Caramel Crack** (`#d97706`): Reserved exclusively for review star scores and rating badges. Named for the first crack in the roasting process — the moment sugars caramelize and the bean's internal structure fractures. Never used for other UI states. Its specificity is the point.

### Neutral
- **Green Bean** (`#f7f4ef`): Body background. Named for the raw, unroasted bean — pale, neutral, full of potential. Not cream — it has a very faint warmth but reads as near-white in ambient light.
- **Parchment Skin** (`#fffdf8`): Card and surface background, one step brighter than Green Bean. Named for the parchment layer surrounding the green bean — thin, translucent, almost invisible. The tonal separation from Green Bean creates depth without shadow.
- **Silver Skin** (`#efe7da`): Secondary surfaces — chips, selected states, sidebar hover, skeleton pulses. Named for the innermost membrane of the coffee cherry — delicate, warm, slightly translucent. Visibly warmer than Parchment Skin.
- **Chaff Dust** (`#e1d7c8`): All border and divider strokes. Named for the dry chaff that separates from the bean during roasting — light, fibrous, warm-toned. Warm and unobtrusive.
- **Ash Gray** (`#6b7280`): Secondary text — timestamps, metadata, inactive nav labels, placeholder text. Named for the residual ash left in the roasting drum. The neutral witness.
- **Dark Roast** (`#1f2937`): Primary body text. Near-black, not pure black — the slight warmth mirrors a French roast bean held to light.
- **Italian Roast** (`#271310`): Dark brand accent used in image overlays and the dark-mode base. Named for the darkest commercial roast — almost carbonized, deeply complex. Not for body text.
- **Medium Roast** (`#504442`): Author secondary labels (e.g. page attribution under post author). Named for the balanced roast that reveals both origin and process.
- **Pale Chaff** (`#d3c3c0`): Softest dividers — intra-card separators, avatar ring overlays. Named for chaff that has floated furthest from the drum and cooled.

### Dark Mode
Light-to-dark mapping: Green Bean → `#161211`, Parchment Skin → `#211b19`, Silver Skin → `#312925`, Chaff Dust → `#4a3d37`. Primary Bloom Teal shifts to its lighter relative `#2dd4bf` for legibility on dark backgrounds; Brick Chaff shifts to `#fb7185`. Dark mode uses the same system, not a second system.

### Named Rules
**The One Signal Rule.** Each color role is used for one semantic purpose only. Bloom Teal = action. Brick Chaff = emotion/danger. Caramel Crack = rating. Never borrow a role color for a non-role purpose (e.g. don't use Caramel Crack for "new" badges just because they need to stand out).

**The Tonal Depth Rule.** Depth between layers is created by stepping through the neutral ramp (Green Bean → Parchment Skin → Silver Skin), never by stacking box-shadows on content cards. Structural chrome (the sidebar) may carry a shadow as a positional signal; content never does.

## 3. Typography

**Display/Body Font:** Inter (with `ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, Helvetica, sans-serif` fallback)
**Label/UI Font:** Inter (same family — consistent voice across all scales)

**Character:** A single-family system. Inter's geometric clarity handles dense product UI without fatigue; its weight range (400–900) provides enough contrast for hierarchy without needing a second face. No serif display pairings — this is not a magazine, it's a tool that feels like a cafe.

### Hierarchy
- **Display** (900 weight, 2rem / 32px, line-height 1.1, letter-spacing −0.02em): Post heroes, page headings, profile display names at maximum size. Rarely used; appears only when a single piece of content needs to anchor the screen.
- **Headline** (700 weight, 1.125rem / 18px, line-height 1.3): Card titles, cafe names in listing views, section headings within content. The dominant typographic event on most surfaces.
- **Title** (600 weight, 1rem / 16px, line-height 1.4): Structural labels — sidebar section titles, modal headings, form section names.
- **Body** (400 weight, 0.875rem / 14px, line-height 1.714): Post captions, review text, comment body. Line-height is generous for readability; max line length capped at 65–75ch where layout allows.
- **Label** (500 weight, 0.75rem / 12px, line-height 1.5): Badges, metadata chips, timestamps, navigation labels. Never uppercase; tracking stays at 0.

### Named Rules
**The No-Display-In-UI Rule.** Body copy, button labels, form fields, and navigation items always use Body or Label scale. Display and Headline are for content identity (post titles, cafe names), not chrome. A button labeled in 18px bold is a mistake.

**The Weight-Not-Size Rule.** Hierarchy is communicated first through font-weight, then through size. Avoid size jumps larger than one step between adjacent hierarchy levels; use weight contrast (400 → 700 → 900) to do the heavy lifting.

## 4. Elevation

CafeStory uses **tonal layering**, not shadow stacks, as its primary depth system. Content layers are separated by background-color shifts through the neutral ramp: Green Bean (`#f7f4ef`) → Parchment Skin (`#fffdf8`) → Silver Skin (`#efe7da`). This keeps the visual field quiet and lets content photography carry spatial weight.

Structural chrome — the sidebar, modals, and bottom sheets — uses a single `box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)` (Tailwind `shadow-lg`) as a positional signal: it marks elements that float above the content plane. This is a structural role, not a decorative one.

### Shadow Vocabulary
- **Structural** (`box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -4px rgba(0,0,0,0.1)`): Sidebar at rest and hovered state, sheet/drawer panels. Marks navigation chrome as above the content layer.
- **Ambient card** (`box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05)`): Tailwind `shadow-sm` on Card component. The minimal signal that a card is a discrete surface — not elevation, just containment.

### Named Rules
**The Flat-By-Default Rule.** Content surfaces — post cards, cafe cards, comment threads — have no elevation shadow at rest. If an element needs a shadow to feel distinct, check whether a tonal background shift would serve better first.

## 5. Components

### Buttons
Familiar and reliable: no unusual shapes, consistent padding, clear state vocabulary across all screens.
- **Shape:** Gently rounded (8px radius / `rounded-md`)
- **Primary:** Bloom Teal background (`#0f766e`), white text, padding 10px 16px. Hover shifts to Maillard (`#115e59`). Focus shows a 3px ring at 30% opacity of the ring token.
- **Outline:** Parchment Skin background, Dark Roast text, Chaff Dust border. Hover shifts border to Bloom Teal, text to Bloom Teal.
- **Ghost:** Transparent background, Dark Roast text. Hover: Silver Skin background, Bloom Teal text. Used for action buttons inside cards (Like, Comment, Share, Bookmark).
- **Disabled:** 50% opacity on all variants, pointer-events none.
- **Sizes:** xs (28px h, 8px px), sm (36px h, 12px px), default (40px h, 16px px), lg (44px h, 20px px), icon variants for icon-only buttons.

### Badges / Chips
- **Primary:** Bloom Teal background, white text, 8px radius, padding 4px 12px. Used for category tags, status labels.
- **Secondary:** Silver Skin background, Maillard text. Used for content tags on post cards — the default chip variant.
- **Rating:** Caramel Crack background at 15% opacity (`rgba(217,119,6,0.15)`), Caramel Crack text (`#d97706`). Exclusively for review score display.
- **Outline:** Parchment Skin background, Dark Roast text, Chaff Dust border. UI state chips.

### Cards / Containers
- **Corner style:** Gently rounded (8px radius / `rounded-md`)
- **Background:** Parchment Skin (`#fffdf8`)
- **Shadow strategy:** Minimal ambient (`shadow-sm`) — containment signal, not elevation claim. Hover may shift border to `primary/50` for interactive cards.
- **Border:** Chaff Dust (`#e1d7c8`) at 1px
- **Internal padding:** 24px (`p-6`) for standard CardHeader/CardContent; 16px (`p-4`) for compact variants (post cards, cafe cards)

### Inputs / Fields
- **Style:** Stroke border (Chaff Dust `#e1d7c8`), Parchment Skin background, 8px radius, 48px height, 16px horizontal padding.
- **Focus:** Border shifts to Bloom Teal (`#0f766e`). No glow ring on inputs — clean transition only.
- **Placeholder:** Ash Gray (`#6b7280`). Must maintain ≥4.5:1 contrast against Parchment Skin background.
- **Disabled:** 50% opacity, `cursor-not-allowed`.
- **Error:** Inherits the destructive/accent token (Brick Chaff `#9f1239`) for border and label color.

### Navigation (Sidebar)
- **Style:** Fixed left sidebar, 64px collapsed / 240px expanded. Expands on hover or keyboard focus (`:focus-within`). Parchment Skin background with structural shadow.
- **Items:** Ghost button style (transparent, Ash Gray text). Active state: font-bold, Maillard text. Hover state: Silver Skin background, Ash Gray text maintained.
- **Brand mark:** 40px icon visible at all widths. Label text fades in on expand via opacity + translate transition (150ms ease-out).
- **Mobile:** Collapses to 64px icon-only rail by default, expands on hover/focus.

### Post Card (Signature Component)
The primary repeating unit of the feed. 85% container width, centered, max-width constrained.
- Header: 44px avatar (circular, Chaff Dust ring, subtle Caramel Crack inner-shadow tint for cafe page association), author name (font-bold), optional secondary cafe-page label in Medium Roast.
- Media: Full-bleed carousel directly below header, no padding. `aspect-auto` or `aspect-[4/3]` per media count.
- Actions: Ghost icon buttons (Like, Comment, Share) left-aligned; Bookmark right-aligned. Like fills Brick Chaff when active; Share fills Bloom Teal when active.
- Tags: Secondary badge chips below caption.

## 6. Do's and Don'ts

### Do:
- **Do** keep Bloom Teal (`#0f766e`) exclusive to interactive actions and current selection. Its scarcity is what makes it legible as a signal.
- **Do** separate content layers through background tonal shifts (Green Bean → Parchment Skin → Silver Skin) before reaching for a shadow.
- **Do** use font-weight contrast (400 → 700 → 900) as the primary hierarchy tool within a given type size.
- **Do** maintain ≥4.5:1 contrast for all body and placeholder text against their background — verify at the border cases (Ash Gray on Parchment Skin, Ash Gray on Silver Skin).
- **Do** support `@media (prefers-reduced-motion: reduce)` for all transitions. The sidebar expand, card hover transitions, and sheet animations must all have instant/crossfade fallbacks.
- **Do** use Inter system-stack fallback so the layout doesn't shift if the web font is slow — the fallback metrics are close enough.
- **Do** keep the sidebar nav labels hidden at rest (opacity 0) and revealed on hover/focus only — chrome earns space, it doesn't claim it.
- **Do** use the dark mode token mapping consistently: never invert or desaturate the light palette; map each role token to its dark counterpart explicitly.

### Don't:
- **Don't** make the interface feel like Facebook/generic social feed — too noisy, too blue, too many competing elements on screen at once. The feed should feel quieter and more editorial.
- **Don't** use the Yelp/TripAdvisor utilitarian directory pattern — no star-count grids divorced from community context, no transactional-only layouts.
- **Don't** use the generic SaaS warm-cream body + teal primary + uppercase eyebrow label scaffold. That is the AI-default 2024–2026 pattern; CafeStory must not be mistakable for it.
- **Don't** clone the engagement-metric-everywhere energy of Instagram — social counters are present but subordinate. Never let the number dominate the identity.
- **Don't** use `border-left` or `border-right` greater than 1px as a colored accent stripe on cards or list items. Rewrite with full borders, background tints, or leading icons.
- **Don't** use gradient text (`background-clip: text` with a gradient). Use solid color; weight or size for emphasis.
- **Don't** nest cards inside cards. If an element inside a card needs to be a card, the parent structure is wrong.
- **Don't** use Caramel Crack (`#d97706`) for anything other than rating scores. Borrowing it for "new" labels or highlights breaks its semantic contract.
- **Don't** use decorative motion — page-load choreography, scroll-triggered entrance stacks applied uniformly to every section. Motion signals state, not aesthetics.
- **Don't** use a display or headline font weight in navigation labels, button text, or form fields. UI chrome stays at body/label scale.
