---
name: draw-diagrams
description: Draw / create / generate architecture and workflow diagrams as .drawio (draw.io) files — UML activity flowcharts, C4 system-context / container / component diagrams, and ERD / database schemas. Use when asked to draw, diagram, chart, visualize, or produce a flowchart / ERD / architecture diagram for this project. Output is code-generated draw.io XML with a fixed unified palette and English labels.
---

# draw-diagrams

Generate **draw.io (`.drawio`) diagrams as code** via a small Python builder,
so diagrams stay consistent (one palette, UML notation) and reproducible.
The builder is the deliverable; this file is its man page.

Covers the diagram families used in this repo:

| Family | What | Builder primitives |
|---|---|---|
| UML activity flowchart | request → decision cascade → outcome | `start` `end` `action` `decision` `external` `datastore` `result` `fork`/`join` `note` |
| C4 context / container / component | boxes-and-lines architecture, UML notation | `actor` `component` (module «component») `database` (cylinder) `boundary` `dep_edge` |
| ERD / schema | one table per entity, one row per column | `erd_table` (shape=table) |

**Rules baked in:** all diagram **content is written in English**; one
**unified palette** (green=internal, yellow=decision, pink=external/AI,
orange=datastore, blue=actor/result); UML-standard shapes
(`shape=module` for components, `cylinder3` for databases, `umlActor` for
people, `rhombus` for decisions).

Paths below are relative to the repo root (`<unit>/`). The builder lives at
`.claude/skills/draw-diagrams/drawio_builder.py`.

## Prerequisites

Python 3.8+ only — **stdlib, no pip installs**. Verified:

```bash
python --version   # Python 3.10.11 here; any 3.8+ works
```

## Run (agent path) — generate + validate

The driver is `drawio_builder.py`. Regenerate the samples and validate every
file's XML (this is the smoke test — run it first to confirm the builder works):

```bash
cd .claude/skills/draw-diagrams && python examples/make_examples.py
```

Expected output (each line = a written file whose XML parsed clean):

```
OK  sample-activity-flowchart.drawio  (9233 bytes, XML valid)
OK  sample-c4-component.drawio  (6166 bytes, XML valid)
OK  sample-erd.drawio  (10569 bytes, XML valid)
OK  sample-all-pages.drawio  (3 pages, XML valid)
```

Open any `.drawio` in draw.io Desktop, https://app.diagrams.net, or the VS Code
"Draw.io Integration" extension. Export to PNG/JPG from there (this container
has no draw.io renderer, so screenshots must be exported by the user).

## Quickstart — author a new diagram

Import the builder, place nodes with explicit `(x, y)`, connect with `edge`,
`dump()` to XML, then validate. This exact snippet runs and produces a valid
file:

```bash
cd .claude/skills/draw-diagrams && python -c "
from drawio_builder import Diagram
d = Diagram('Quickstart', 600, 400)
s = d.start(280, 40)
a = d.action('Do the work', 200, 110, 200, 50)
q = d.decision('Success?', 220, 200, 160, 90)
ok = d.result('Done', 120, 330, 160, 50)
no = d.result('Retry', 400, 330, 160, 50)
e = d.end(300, 430)
d.edge(s, a); d.edge(a, q)
d.edge(q, ok, 'Yes'); d.edge(q, no, 'No')
open('examples/quickstart.drawio','w',encoding='utf-8').write(d.dump())
import xml.dom.minidom; xml.dom.minidom.parse('examples/quickstart.drawio')
print('quickstart.drawio written + XML valid')
"
```

For anything non-trivial, write a `make_<name>.py` (like
`examples/make_examples.py`) instead of a one-liner — you WILL iterate on
coordinates, and a file is far easier to edit than a `-c` string.

## Recipes

**Palette** (import the constants; never hard-code hex):

```python
from drawio_builder import Diagram, INTERNAL, EXTERNAL, DECISION, STORE, RETURN, ACTOR, ADMIN
```

**Activity flowchart** — vertical spine, decisions branch to the side.
Keep node text human-readable prose. When a decision hides a technical
rule, put the plain question in the diamond and the rule (the "script")
either on a second line `⟨ caption_score >= 70 ⟩` or in a `d.panel(...)`
reference box beside the flow. Write raw `<`, `>`, `&` — the builder
escapes them.

**C4 component** — `d.component(text, x, y, pair=INTERNAL, stereotype="component")`.
`text` may contain inline HTML (`<b>`, `<br>`, `<font style='...'>`) — it is
stored escaped and draw.io renders it. Use `dep_edge` for UML «use»
dependencies (dashed open arrow), `edge` for solid calls, `boundary` for the
dashed system frame.

**ERD** — `rows = [(col, type, is_pk, is_fk), ...]`; `d.erd_table(name, rows, x, y)`
returns `{col: row_id}`, so an FK edge attaches row-to-row:

```python
d.edge(users["region_id"], regions["id"], "",
       "endArrow=ERone;startArrow=ERmany;edgeStyle=entityRelationEdgeStyle;html=1;strokeColor=#666666;")
```

**Multi-page file** — `from drawio_builder import dump_multi; dump_multi([d1, d2, d3])`.

**Layout tip:** lay out top-to-bottom with a running `y` and `y += step`;
put side branches / datastores / external calls in fixed left/right columns.
See `examples/make_examples.py` for all three families end to end.

## Gotchas (battle scars — the non-obvious ones)

- **draw.io stores label HTML as escaped entities.** In the file, `<b>Web</b>`
  must appear as `&lt;b&gt;Web&lt;/b&gt;`; draw.io un-escapes it because
  `html=1`. The builder does this for you (`node()` calls `esc()` once). So in
  Python you pass **literal** `<b>…</b>` / `<br>` / `«component»`, not entities.
- **Never pre-encode `&`.** Writing `"A &amp; B"` in Python gets escaped again
  to `&amp;amp;` and renders the literal text `A &amp; B`. Write `"A & B"`.
  (Hit this exactly this session — a band label showed `&amp;`.) Verify:
  `grep -c '&amp;amp;' *.drawio` must be `0`.
- **`\n` is a line break** in a label (html=1 nodes) — use it freely for
  multi-line boxes. Do **not** use `&#10;` or `<br>` for plain flowchart text.
- **Light fills need dark font.** All palette fills are pale; the builder sets
  `fontColor=#1f2933`. If you hand-write a node, do the same or text vanishes.
- **Component tab overlap.** `shape=module` draws tabs at the top-left; set
  `verticalAlign=top; spacingTop=12` (the builder does) so the first text line
  clears the tabs.
- **ERD edges attach to a ROW, not the table.** Use the id returned in the
  `{col: row_id}` dict as edge source/target, or the relationship line lands on
  the whole table.
- **Validate every file** with `xml.dom.minidom.parseString(...)`. An unescaped
  `<` or `&` in any value silently produces malformed XML that draw.io opens as
  a blank page. `make_examples.py` validates automatically.
- **No renderer here.** This container can't rasterize draw.io. Verification =
  XML-valid + inspecting the `value="..."` encoding. Actual PNG/JPG export is a
  user step in draw.io.

## Troubleshooting

| Symptom | Cause → fix |
|---|---|
| `ExpatError: not well-formed (invalid token)` on parse | A `value=` contains a raw `<` or `&` that wasn't escaped — you bypassed `node()` and wrote a cell by hand. Route text through the builder helpers, or `esc()` it. |
| Diagram opens but a box shows literal `<i>…</i>` / `&amp;` | Double-escaped. You pre-encoded HTML/entities in Python. Pass literal markup and raw `&`; let `esc()` run once. |
| `AttributeError: 'str' object has no attribute 'dom'` | A local variable named `xml` shadowed the `xml` module. Rename the local. |
| Text sits on top of the component's corner tabs | Missing `verticalAlign=top; spacingTop=12` on a hand-written `shape=module`. |
