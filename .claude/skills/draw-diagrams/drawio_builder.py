# -*- coding: utf-8 -*-
"""
drawio_builder — generate draw.io (.drawio) diagrams as code.

Supports the diagram families used across this repo, all with ONE unified
palette and UML notation:

  * UML activity flowchart  (start/end, action, decision, fork/join, note)
  * C4 System Context / Container / Component  (UML «component» module shape,
    «database» cylinder, actor)
  * ERD / schema            (shape=table with one row per column)

Design rules baked in (see SKILL.md "Gotchas" for the why):
  - Content text is ENGLISH.
  - Node text is passed as PLAIN text; this module HTML-escapes it. Never
    pre-write entities like `&amp;` / `&lt;` — write `&` / `<` and let the
    builder escape once. (Double-escaping shows literal "&amp;".)
  - `\n` in a label renders as a line break (html=1 nodes).
  - Light fills always get a dark fontColor so text stays readable.
  - Component = UML `shape=module` (rectangle with tabs). Database =
    `cylinder3`. Actor = `umlActor`. These are the UML-standard shapes
    draw.io renders natively.

Stdlib only. Python 3.8+.
"""
import html
from typing import Optional

# ----------------------------------------------------------------------------
# Unified palette — (fill, stroke). Reuse these everywhere for consistency.
# ----------------------------------------------------------------------------
FONT      = "#1f2933"                    # dark text for all light fills
ACTOR     = ("#dae8fc", "#6c8ebf")       # blue   — person / actor
INTERNAL  = ("#d5e8d4", "#82b366")       # green  — our system / process step
DECISION  = ("#fff2cc", "#d6b656")       # yellow — decision / branch
EXTERNAL  = ("#f8cecc", "#b85450")       # pink   — external system / AI call
STORE     = ("#ffe6cc", "#d79b00")       # orange — datastore / database
RETURN    = ("#dae8fc", "#6c8ebf")       # blue   — terminal message / result
ADMIN     = ("#f8cecc", "#b85450")       # pink   — admin-only surface

EDGE = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=classic;"
        "strokeColor=#334e68;fontColor=#334e68;labelBackgroundColor=#ffffff;fontSize=11;")


def esc(s: str) -> str:
    """HTML-escape once. Pass raw `&`, `<`, `>` — do not pre-encode."""
    return html.escape(str(s), quote=True)


class Diagram:
    """One draw.io page. Add nodes/edges, then `.dump()` to XML string."""

    def __init__(self, name: str, w: int = 1400, h: int = 1600):
        self.name = name
        self.w, self.h = w, h
        self.cells = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
        self._i = 0

    def _id(self) -> str:
        self._i += 1
        return f"n{self._i}"

    # -- nodes --------------------------------------------------------------
    def node(self, text, x, y, w, h, style) -> str:
        """Add a vertex. `text` is ESCAPED once into the value attribute.

        draw.io stores label HTML as ENTITIES inside value= and un-escapes it
        for rendering (html=1). So inline markup like `<b>`/`<br>`/`<font>` is
        passed as literal `<b>` here and correctly becomes `&lt;b&gt;` in the
        file. Likewise write raw `&` `<` — never pre-encode to `&amp;`/`&lt;`."""
        nid = self._id()
        self.cells.append(
            f'<mxCell id="{nid}" value="{esc(text)}" style="{style}" vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
        return nid

    # -- activity-diagram primitives ---------------------------------------
    def start(self, x, y) -> str:
        return self.node("", x, y, 34, 34,
                         "ellipse;html=1;fillColor=#333333;strokeColor=#333333;")

    def end(self, x, y) -> str:
        outer = self.node("", x, y, 38, 38,
                          "ellipse;html=1;fillColor=none;strokeColor=#333333;strokeWidth=2;")
        self.node("", x + 8, y + 8, 22, 22,
                  "ellipse;html=1;fillColor=#333333;strokeColor=none;")
        return outer

    def bar(self, x, y, w) -> str:
        """Fork / join synchronization bar."""
        return self.node("", x, y, w, 8,
                         "rounded=0;fillColor=#333333;strokeColor=#333333;html=1;")

    fork = bar
    join = bar

    def _box(self, text, x, y, w, h, pair, arc=18, extra=""):
        f, s = pair
        return self.node(text, x, y, w, h,
                         f"rounded=1;whiteSpace=wrap;html=1;fillColor={f};strokeColor={s};"
                         f"fontColor={FONT};arcSize={arc};fontSize=12;{extra}")

    def action(self, text, x, y, w=300, h=60):
        return self._box(text, x, y, w, h, INTERNAL)

    def external(self, text, x, y, w=300, h=64):
        return self._box(text, x, y, w, h, EXTERNAL)

    def result(self, text, x, y, w=250, h=56):
        return self._box(text, x, y, w, h, RETURN, arc=30, extra="fontStyle=1;")

    def decision(self, text, x, y, w=280, h=100):
        f, s = DECISION
        return self.node(text, x, y, w, h,
                         f"rhombus;whiteSpace=wrap;html=1;fillColor={f};strokeColor={s};"
                         f"fontColor={FONT};fontSize=11;")

    def datastore(self, text, x, y, w=180, h=80):
        f, s = STORE
        return self.node(text, x, y, w, h,
                         f"shape=cylinder3;whiteSpace=wrap;html=1;fillColor={f};strokeColor={s};"
                         f"fontColor={FONT};boundedLbl=1;backgroundOutline=1;size=10;fontSize=11;")

    # -- C4 / UML primitives -----------------------------------------------
    def component(self, text, x, y, w=250, h=90, pair=INTERNAL, stereotype="component"):
        """UML component (module shape with tabs). `text` is treated as HTML
        (may contain <b>, <br>, <font>); prepends the «stereotype» line."""
        f, s = pair
        label = (f"<i>«{stereotype}»</i><br>{text}") if stereotype else text
        return self.node(label, x, y, w, h,
                         f"shape=module;jettyWidth=16;jettyHeight=8;whiteSpace=wrap;html=1;"
                         f"fillColor={f};strokeColor={s};fontColor={FONT};align=center;"
                         f"verticalAlign=top;spacingTop=12;fontSize=12;")

    def database(self, text, x, y, w=180, h=90, stereotype="database"):
        """UML «database» cylinder. `text` is treated as HTML."""
        f, s = STORE
        label = (f"<i>«{stereotype}»</i><br>{text}") if stereotype else text
        return self.node(label, x, y, w, h,
                         f"shape=cylinder3;whiteSpace=wrap;html=1;fillColor={f};strokeColor={s};"
                         f"fontColor={FONT};boundedLbl=1;backgroundOutline=1;size=12;"
                         f"align=center;verticalAlign=top;spacingTop=6;fontSize=12;")

    def actor(self, text, x, y, w=60, h=80):
        f, s = ACTOR
        return self.node(text, x, y, w, h,
                         f"shape=umlActor;verticalLabelPosition=bottom;labelPosition=center;"
                         f"verticalAlign=top;html=1;outlineConnect=0;fillColor={f};strokeColor={s};"
                         f"fontColor={FONT};fontSize=12;spacingTop=8;whiteSpace=wrap;")

    def boundary(self, text, x, y, w, h, color="#0b4884"):
        """Dashed system/container boundary frame."""
        return self.node(text, x, y, w, h,
                         f"rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor={color};"
                         f"dashed=1;dashPattern=8 6;fontSize=13;fontStyle=1;fontColor={color};"
                         f"align=left;verticalAlign=top;spacingLeft=14;spacingTop=8;strokeWidth=2;")

    # -- annotation helpers -------------------------------------------------
    def note(self, text, x, y, w=320, h=140):
        return self.node(text, x, y, w, h,
                         "shape=note;whiteSpace=wrap;html=1;fillColor=#FFF7D6;strokeColor=#d6b656;"
                         "fontColor=#1f2933;fontSize=11;align=left;verticalAlign=top;"
                         "spacingLeft=8;spacingTop=6;size=16;")

    def band(self, text, x, y, w, h, fill):
        """Faint background swim-band grouping a phase."""
        return self.node(text, x, y, w, h,
                         f"rounded=0;whiteSpace=wrap;html=1;fillColor={fill};strokeColor=none;"
                         f"opacity=35;align=left;verticalAlign=top;fontSize=13;fontStyle=1;"
                         f"fontColor=#5a5a5a;spacingLeft=10;spacingTop=6;")

    def label(self, text, x, y, w, h, size=18):
        return self.node(text, x, y, w, h,
                         f"text;html=1;strokeColor=none;fillColor=none;align=left;"
                         f"verticalAlign=middle;fontSize={size};fontColor={FONT};fontStyle=1;")

    def panel(self, text, x, y, w, h):
        """Reference / explanation panel (script + explanation)."""
        return self.node(text, x, y, w, h,
                         "rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#c0c8d1;"
                         "fontColor=#1f2933;fontSize=11;align=left;verticalAlign=top;"
                         "spacingLeft=10;spacingTop=8;")

    def legend(self, x, y, items=None):
        """items: list[(label, (fill,stroke))]. Defaults to the full palette."""
        if items is None:
            items = [("Process step", INTERNAL), ("Decision", DECISION),
                     ("External / AI call", EXTERNAL), ("Datastore", STORE),
                     ("Result / message", RETURN)]
        h = 34 + len(items) * 28 + 8
        self.node("Legend", x, y, 250, h,
                  "rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#c0c8d1;"
                  "fontSize=12;fontStyle=1;align=left;verticalAlign=top;spacingLeft=8;spacingTop=6;")
        for k, (t, (f, s)) in enumerate(items):
            yy = y + 30 + k * 28
            self.node("", x + 12, yy, 26, 18, f"rounded=1;html=1;fillColor={f};strokeColor={s};")
            self.node(t, x + 46, yy - 3, 196, 24,
                      "text;html=1;strokeColor=none;fillColor=none;align=left;"
                      "verticalAlign=middle;fontSize=11;")

    # -- ERD table ----------------------------------------------------------
    def erd_table(self, name, rows, x, y, pair=INTERNAL, col_w=170, type_w=120, row_h=22):
        """rows: list of (col_name, col_type, is_pk, is_fk).
        Returns {col_name: row_cell_id} so edges can attach to a column row.
        """
        f, s = pair
        header_h = 30
        w = col_w + type_w
        h = header_h + len(rows) * row_h
        tid = self.node(name, x, y, w, h,
                        f"shape=table;startSize={header_h};container=1;collapsible=0;"
                        f"childLayout=tableLayout;fontSize=13;fontStyle=1;fillColor={f};"
                        f"strokeColor={s};align=center;verticalAlign=middle;")
        row_ids = {}
        ry = header_h
        for (col, ctype, pk, fk) in rows:
            rid = self._id()
            self.cells.append(
                f'<mxCell id="{rid}" value="" style="shape=tableRow;horizontal=0;startSize=0;'
                f'swimlaneHead=0;swimlaneBody=0;strokeColor=inherit;top=0;left=0;bottom=0;right=0;'
                f'collapsible=0;dropTarget=0;fillColor=none;points=[[0,0.5],[1,0.5]];'
                f'portConstraint=eastwest;fontSize=12;" vertex="1" parent="{tid}">'
                f'<mxGeometry y="{ry}" width="{w}" height="{row_h}" as="geometry"/></mxCell>')
            name_txt = ("PK " if pk else ("FK " if fk else "")) + col
            font = "fontStyle=5;" if pk else ("fontStyle=4;" if fk else "")
            nm = self._id()
            self.cells.append(
                f'<mxCell id="{nm}" value="{esc(name_txt)}" style="shape=partialRectangle;html=1;'
                f'whiteSpace=wrap;connectable=0;strokeColor=inherit;overflow=hidden;fillColor=none;'
                f'top=0;left=0;bottom=0;right=0;pointerEvents=1;fontSize=12;{font}" vertex="1" '
                f'parent="{rid}"><mxGeometry width="{col_w}" height="{row_h}" as="geometry"/></mxCell>')
            tp = self._id()
            self.cells.append(
                f'<mxCell id="{tp}" value="{esc(ctype)}" style="shape=partialRectangle;html=1;'
                f'whiteSpace=wrap;connectable=0;strokeColor=inherit;overflow=hidden;fillColor=none;'
                f'top=0;left=0;bottom=0;right=0;pointerEvents=1;fontSize=11;align=left;spacingLeft=6;'
                f'fontColor=#555555;" vertex="1" parent="{rid}">'
                f'<mxGeometry x="{col_w}" width="{type_w}" height="{row_h}" as="geometry"/></mxCell>')
            row_ids[col] = rid
            ry += row_h
        return row_ids

    # -- edges --------------------------------------------------------------
    def edge(self, src, dst, label="", extra=""):
        eid = self._id()
        self.cells.append(
            f'<mxCell id="{eid}" value="{esc(label)}" style="{EDGE}{extra}" edge="1" parent="1" '
            f'source="{src}" target="{dst}"><mxGeometry relative="1" as="geometry"/></mxCell>')
        return eid

    def dep_edge(self, src, dst, label="", color="#8E44AD"):
        """UML dependency «use» — dashed open arrow."""
        return self.edge(src, dst, label,
                         f"endArrow=open;dashed=1;strokeColor={color};fontColor={color};")

    # -- output -------------------------------------------------------------
    def _page_xml(self) -> str:
        inner = "\n        ".join(self.cells)
        return (f'  <diagram id="{esc(self.name)}" name="{esc(self.name)}">\n'
                f'    <mxGraphModel dx="1600" dy="1000" grid="0" gridSize="10" guides="1" '
                f'tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" '
                f'pageWidth="{self.w}" pageHeight="{self.h}" math="0" shadow="0">\n'
                f'      <root>\n        {inner}\n      </root>\n'
                f'    </mxGraphModel>\n  </diagram>')

    def dump(self) -> str:
        return ('<mxfile host="app.diagrams.net" agent="drawio_builder">\n'
                + self._page_xml() + "\n</mxfile>\n")


def dump_multi(diagrams) -> str:
    """Combine several Diagram objects into one multi-page .drawio file."""
    body = "\n".join(d._page_xml() for d in diagrams)
    return '<mxfile host="app.diagrams.net" agent="drawio_builder">\n' + body + "\n</mxfile>\n"
