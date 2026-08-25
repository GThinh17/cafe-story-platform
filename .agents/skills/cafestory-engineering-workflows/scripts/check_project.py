#!/usr/bin/env python3
"""Emit a deterministic JSON summary of CafeStory project structure."""

from __future__ import annotations

import json
import sys
from pathlib import Path


EXPECTED = {
    "backend": Path("1-cafe-story-backend-javaspring"),
    "web": Path("2-cafe-story-nextjs-web"),
    "mobile": Path("3-cafe-story-reactnative-mobile"),
}


def exists(root: Path, relative: Path) -> bool:
    return (root / relative).exists()


def read_text(root: Path, relative: Path) -> str:
    path = root / relative
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    result = {
        "root": str(root),
        "areas": {},
        "rules": {
            "root_instruction_file": exists(root, Path("# CafeStory AI Agent Instruction.txt")),
            "web_agents_md": exists(root, Path("2-cafe-story-nextjs-web/AGENTS.md")),
            "web_codex_md": exists(root, Path("2-cafe-story-nextjs-web/CODEX.md")),
        },
        "manifests": {
            "backend_pom": exists(root, Path("1-cafe-story-backend-javaspring/pom.xml")),
            "web_package_json": exists(root, Path("2-cafe-story-nextjs-web/package.json")),
            "mobile_package_json": exists(root, Path("3-cafe-story-reactnative-mobile/package.json")),
        },
        "warnings": [],
    }

    for name, relative in EXPECTED.items():
        path = root / relative
        result["areas"][name] = {
            "path": str(relative),
            "exists": path.exists(),
            "file_count": sum(1 for p in path.rglob("*") if p.is_file()) if path.exists() else 0,
        }

    agents_text = read_text(root, Path("2-cafe-story-nextjs-web/AGENTS.md"))
    next_docs = root / "2-cafe-story-nextjs-web/node_modules/next/dist/docs"
    result["nextjs"] = {
        "agents_rule_mentions_docs": "node_modules/next/dist/docs" in agents_text,
        "local_next_docs_exist": next_docs.exists(),
    }

    if result["rules"]["web_agents_md"] and not next_docs.exists():
        result["warnings"].append(
            "Next.js AGENTS.md requires node_modules/next/dist/docs, but local docs were not found."
        )

    missing = [name for name, data in result["areas"].items() if not data["exists"]]
    if missing:
        result["warnings"].append("Missing expected project areas: " + ", ".join(missing))

    print(json.dumps(result, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
