# -*- coding: utf-8 -*-
"""Generate one sample of each diagram family, then validate the XML.
Run:  python examples/make_examples.py
All diagram CONTENT is English (skill rule)."""
import sys, os, xml.dom.minidom
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from drawio_builder import (Diagram, dump_multi,
                            INTERNAL, EXTERNAL, DECISION, STORE, RETURN, ADMIN, ACTOR)

OUT = os.path.dirname(os.path.abspath(__file__))


def activity_flowchart():
    """UML activity diagram: request -> decision cascade -> outcome."""
    d = Diagram("Sample Activity Flowchart", w=980, h=980)
    d.label("Sample — Login Request (UML activity)", 300, 20, 600, 30, 18)
    cx, dcx, rx = 360, 340, 720
    y = 70
    s = d.start(477, y); y += 56
    a1 = d.action("Receive login request\n(username + password)", cx, y, 280, 50); y += 90
    d1 = d.decision("Fields present?", dcx, y, 240, 90); y += 120
    r1 = d.result("Return: missing fields", rx, y - 120 + 20)
    a2 = d.action("Look up user by username", cx, y, 280, 46)
    ds = d.datastore("User store", 60, y - 6, 160, 74); y += 100
    d2 = d.decision("Password matches?", dcx, y, 240, 90); y += 120
    r2 = d.result("Return: invalid credentials", rx, y - 120 + 20)
    a3 = d.external("Issue JWT access + refresh token", cx, y, 280, 50); y += 90
    a4 = d.action("Return session to client", cx, y, 280, 46); y += 80
    e = d.end(477, y)
    d.edge(s, a1); d.edge(a1, d1)
    d.edge(d1, r1, "No"); d.edge(d1, a2, "Yes")
    d.dep_edge(a2, ds, "read", color="#d79b00"); d.edge(a2, d2)
    d.edge(d2, r2, "No"); d.edge(d2, a3, "Yes")
    d.edge(a3, a4); d.edge(a4, e)
    d.legend(60, 700)
    return d


def component_c4():
    """C4 component diagram in UML notation (module + database + actor)."""
    d = Diagram("Sample C4 Component", w=1100, h=760)
    d.label("Sample — C4 Component (UML notation)", 320, 20, 700, 30, 18)
    user = d.actor("User", 80, 120)
    api = d.component("API Gateway<br><font style='font-size:10px'>routing · auth filter</font>",
                      260, 110, 260, 90)
    svc = d.component("Order Service<br><font style='font-size:10px'>place / cancel orders</font>",
                      620, 110, 260, 90)
    db = d.database("PostgreSQL", 640, 320, 200, 100)
    pay = d.component("Stripe<br><font style='font-size:10px'>card payments</font>",
                      620, 480, 260, 80, pair=EXTERNAL, stereotype="external component")
    d.edge(user, api, "uses [HTTPS]")
    d.edge(api, svc, "delegates")
    d.dep_edge(svc, db, "«use» JDBC", color="#2E86C1")
    d.dep_edge(svc, pay, "«use» checkout")
    d.legend(80, 470, items=[("Component", INTERNAL), ("External component", EXTERNAL),
                             ("Datastore", STORE), ("Actor", ACTOR)])
    return d


def erd_schema():
    """Entity-Relationship diagram (shape=table, one row per column)."""
    d = Diagram("Sample ERD", w=900, h=520)
    d.label("Sample — ERD (schema)", 320, 20, 500, 30, 18)
    users = d.erd_table("users", [
        ("id", "uuid", True, False),
        ("email", "varchar", False, False),
        ("region_id", "uuid", False, True),
    ], 80, 90, pair=ACTOR)
    regions = d.erd_table("regions", [
        ("id", "uuid", True, False),
        ("name", "varchar", False, False),
    ], 560, 90, pair=INTERNAL)
    orders = d.erd_table("orders", [
        ("id", "uuid", True, False),
        ("user_id", "uuid", False, True),
        ("amount", "decimal", False, False),
    ], 80, 300, pair=STORE)
    d.edge(users["region_id"], regions["id"], "",
           "endArrow=ERone;startArrow=ERmany;strokeColor=#666666;"
           "edgeStyle=entityRelationEdgeStyle;html=1;")
    d.edge(orders["user_id"], users["id"], "",
           "endArrow=ERone;startArrow=ERmany;strokeColor=#666666;"
           "edgeStyle=entityRelationEdgeStyle;html=1;")
    return d


def main():
    diagrams = {
        "sample-activity-flowchart.drawio": activity_flowchart(),
        "sample-c4-component.drawio": component_c4(),
        "sample-erd.drawio": erd_schema(),
    }
    for fname, d in diagrams.items():
        path = os.path.join(OUT, fname)
        content = d.dump()
        open(path, "w", encoding="utf-8").write(content)
        xml.dom.minidom.parseString(content)  # raises if malformed
        print(f"OK  {fname}  ({len(content)} bytes, XML valid)")
    # also a combined multi-page file
    combined = dump_multi(list(diagrams.values()))
    open(os.path.join(OUT, "sample-all-pages.drawio"), "w", encoding="utf-8").write(combined)
    xml.dom.minidom.parseString(combined)
    print("OK  sample-all-pages.drawio  (3 pages, XML valid)")


if __name__ == "__main__":
    main()
