"""Finds the names that two genes or families share in the species designs.

The tool reads each file in design/species/. It takes the names of the major
families, the genes, the minor families, and the hybrids. A name with an "s"
at the end counts as the same name, so "Sinkhole" and "Sinkholes" clash.

It prints each clash and exits with code 1 if it finds one. Run it after each
change to a design file:

    python3 tools/check_design_names.py
"""

import glob
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def names_of(path):
    """Returns (name, kind) for each gene and family name in one design file."""
    found = []
    section = None
    columns = None
    for line in open(path, encoding="utf-8"):
        line = line.rstrip("\n")
        if line.startswith("## "):
            section = line[3:].strip()
            columns = None
            continue
        if section == "Major families" and line.startswith("### "):
            found.append((line[4:].strip(), "family"))
            continue
        if not line.startswith("|"):
            columns = None
            continue
        cells = [cell.strip() for cell in line.strip("|").split("|")]
        if columns is None:
            columns = cells
            continue
        if set(line) <= set("|-: "):
            continue
        kind = {"Gene": "gene", "Minor family": "minor family", "Hybrid": "hybrid"}.get(columns[0])
        if kind is not None and section in ("Genes", "Minor families", "Hybrids"):
            found.append((cells[0], kind))
    return found


def key(name):
    return re.sub(r"s$", "", name.lower())


def main():
    seen = {}
    for path in sorted(glob.glob(os.path.join(ROOT, "design", "species", "*.md"))):
        species = os.path.splitext(os.path.basename(path))[0]
        for name, kind in names_of(path):
            seen.setdefault(key(name), []).append(f"{name} ({species} {kind})")
    clashes = [places for places in seen.values() if len(places) > 1]
    for places in clashes:
        print("Clash: " + "; ".join(places))
    print(f"{len(seen)} names checked, {len(clashes)} clashes")
    return 1 if clashes else 0


if __name__ == "__main__":
    sys.exit(main())
