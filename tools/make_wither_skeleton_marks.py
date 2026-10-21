"""Make the marks and the gene models of the wither skeleton genes.

Usage: python3 tools/make_wither_skeleton_marks.py

The wither skeleton has the skeleton body, 1.2 times larger, and the
skeleton region map, so its marks use the same pixels as the skeleton marks
(see make_skeleton_marks.py). The gene models scale with the body. It has
no outer layer. Its bones are almost black, so a mark that the design calls
dark has a light edge, and the other marks use bright or warm colors.

The wither skeleton holds a stone sword in the right hand. The blade points
forward from the hand (arm row 11). The gene models keep clear of it.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- textures/gene_marks/<gene>_<name>.png, the masks of the erase layers.
- gene_visuals/<gene>.json, with one layer for each pattern and mask.
- The gene models in MODELS, with their textures.

The script prints the pixels that two wither skeleton genes share.

The texture: the front of the body has bone at the ribs (y 22 and 24) and
on y 23, and gaps between them. The back has the ribs and the spine (x 35
and 36). The pelvis (y 30 and 31) goes all round the body.

Pixel plan for the Blademasters.
- Duelist: a red cloth tied round the upper sword arm (the right arm), with
  two loose ends, the gene model duelist_cloth.
- Parry: a guard of pale bone on the outer side of the left forearm, the
  gene model parry_guard.
- Cleave has a wide belt of pale bone plates round the pelvis (y 30 and
  31), and plates down the spine under the ribs (x 35 and 36, y 25 to 29).
- Lunge has long, thin shins: a pale line down the front of each shin (leg
  rows 4 to 11), and pale knee knobs (row 4).
- Bladestorm has notches cut in the arm bones: black cuts with pale edges
  on arm rows 2, 5, and 8.
- Executioner: a black hood with a pale edge and eye holes over the skull,
  the gene model executioner_hood.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_skeleton_marks import BACK, FRONT, INNER, OUTER, REGION_MAP, SIDES, arm, leg
from make_stray_marks import changed_pixels, erase

ASH = ("#C8C0B0", "#9A9488", "#6A665E")
RED = ("#B01E1E", "#7A1414", "#D84A3A")


def duelist():
    # The cloth is the gene model duelist_cloth.
    return []


def duelist_model():
    # A red cloth tied round the upper sword arm, the right arm: a band
    # round the arm (arm rows 2 and 3), a knot on its outer side, and two
    # loose ends that hang from the knot. On the right arm bone, the arm is
    # from x -1 to 1 and from z -1 to 1, and row r is at y r - 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def band(face, x, y):
        return RED[2] if y == 0 and face != "bottom" else RED[0] if (x + y) % 3 else RED[1]

    def tail(face, x, y):
        if face not in ("right", "left"):
            return None
        return RED[1] if y == 3 else RED[0]

    # The band: 2x2x2 at uv (0, 0). The knot: 1x1x1 at uv (8, 0). A loose
    # end: 0x4x1 at uv (12, 0).
    paint_box(im, 0, 0, 2, 2, 2, band)
    paint_box(im, 8, 0, 1, 1, 1, lambda face, x, y: RED[1])
    paint_box(im, 12, 0, 0, 4, 1, tail)
    parts = [
        {"bone": "right_arm", "uv": [0, 0], "from": [-1, 0, -1], "size": [2, 2, 2], "inflate": 0.3},
        {"bone": "right_arm", "uv": [8, 0], "from": [-1.8, 0.5, -0.5], "size": [1, 1, 1]},
        {"bone": "right_arm", "uv": [12, 0], "from": [-1.6, 1.5, -1], "size": [0, 4, 1]},
        {"bone": "right_arm", "uv": [12, 0], "from": [-1.6, 1.5, 0.1], "size": [0, 3, 1]},
    ]
    save_model("duelist_cloth", im, parts)


def parry():
    # The guard is the gene model parry_guard.
    return []


def parry_model():
    # A guard of pale bone on the outer side of the left forearm, the arm
    # that does not hold the sword: a long, curved plate with a raised rim,
    # held by two dark straps. On the left arm bone, the outer side is at x
    # 1, and the forearm is at y 3 to 8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def plate(face, x, y):
        if face == "left":
            if y in (1, 4):
                return "#2E2A26"
            return ASH[0] if x in (0, 2) or y in (0, 5) else ASH[1]
        return ASH[2]

    # The plate: 1x6x3 at uv (0, 0).
    paint_box(im, 0, 0, 1, 6, 3, plate)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [0.9, 3, -1.5], "size": [1, 6, 3]}]
    save_model("parry_guard", im, parts)


def cleave():
    # A wide belt of pale bone plates round the pelvis (y 30 and 31): each
    # plate is 3 pixels wide, with a dark seam between the plates. Small
    # plates go on down the spine under the ribs (x 35 and 36, y 25 to 29),
    # where the belt hangs at the back.
    plate = Mark(ASH[0])
    shade = Mark(ASH[1])
    seam = Mark("#1E1C1A")
    for x in range(16, 40):
        if x % 4 == 3:
            seam.put((x, 30)).put((x, 31))
        else:
            plate.put((x, 30))
            shade.put((x, 31))
    for y in range(25, 30):
        (plate if y % 2 else shade).put((35, y)).put((36, y))
    return [plate, shade, seam]


def lunge():
    # Long, thin shins: a pale line down the front of each shin (leg rows 4
    # to 11, the outer column of the front face), so that the shin reads as
    # one long, thin bone, and a pale knob at each knee (row 4).
    line = Mark(ASH[1])
    knob = Mark(ASH[0])
    for row in range(5, 12):
        line.put(leg(FRONT, 0, row))
        line.put(leg(BACK, 1, row), 200)
    for face in SIDES:
        knob.put(leg(face, 0, 4)).put(leg(face, 1, 4))
    return [line, knob]


def bladestorm():
    # Notches cut in the arm bones by many blades: black cuts on arm rows
    # 2, 5, and 8, each with a pale edge of fresh bone under it, on the
    # outer and the front faces, and on the back.
    cut = Mark("#060606")
    edge = Mark(ASH[0])
    for face in (OUTER, FRONT, BACK):
        for row in (2, 5, 8):
            col = (row // 3 + face // 2) % 2
            cut.put(arm(face, col, row))
            edge.put(arm(face, col, row + 1))
    return [cut, edge]


def executioner():
    # The hood is the gene model executioner_hood.
    return []


def executioner_model():
    # A black hood over the skull, as an executioner wears: a hood a little
    # larger than the head, of black cloth with a pale gray edge, so that it
    # reads against the black skull, with
    # two eye holes and a point at the top back. It goes down to the jaw,
    # so the mouth shows under it. On the head bone, the head is from x -4
    # to 4, from y -8 to 0, and from z -4 to 4.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    hood = ("#1E1818", "#141010", "#6A5A56")

    def cloth(face, x, y):
        if face == "bottom":
            return None
        if face == "front":
            if y >= 6:
                return None
            if y == 4 and x in (1, 2, 6, 7):
                return None
            return hood[2] if y == 5 else hood[0] if (x + y) % 4 else hood[1]
        if face in ("right", "left", "back") and y == 6:
            return hood[2]
        return hood[0] if (x + y) % 4 else hood[1]

    def point(face, x, y):
        return hood[1] if face != "top" else hood[0]

    # The hood: 9x7x9 at uv (0, 0). The point: 3x2x3 at uv (40, 0).
    paint_box(im, 0, 0, 9, 7, 9, cloth)
    paint_box(im, 40, 0, 3, 2, 3, point)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-4.5, -8.5, -4.5], "size": [9, 7, 9]},
        {"bone": "head", "uv": [40, 0], "from": [-1.5, -10, 0.5], "size": [3, 2, 3],
         "pivot": [0, -8.5, 2], "rotation": [-20, 0, 0]},
    ]
    save_model("executioner_hood", im, parts)


GENES = {
    "duelist": duelist,
    "parry": parry,
    "cleave": cleave,
    "lunge": lunge,
    "bladestorm": bladestorm,
    "executioner": executioner,
}

MODELS = {
    "duelist": "originofspawns:duelist_cloth",
    "parry": "originofspawns:parry_guard",
    "executioner": "originofspawns:executioner_hood",
}

MODEL_MAKERS = [duelist_model, parry_model, executioner_model]


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make in MODEL_MAKERS:
        make()
    for gene, make in GENES.items():
        write(gene, make(), MODELS, REGION_MAP)
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in changed_pixels(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for (where, _), genes in owners.items():
        if len(genes) > 1:
            key = where + ": " + " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more wither skeleton genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
