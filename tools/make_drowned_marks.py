"""Make the marks and the gene models of the drowned genes.

Usage: python3 tools/make_drowned_marks.py

The drowned has split limbs, but the marks use the zombie layout: the mod
copies the marks of the right limbs, mirrored, to the left limbs. The mod
puts the marks on the base texture and on the outer layer, so a mark shows
where the outer layer covers the base texture. A gene model must sit more
than 0.25 pixel out from the body, outside the outer layer.
This script uses the helpers of make_gene_marks.py and make_husk_marks.py.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.
- The gene models in MODELS, with their textures.

The script prints the pixels that two drowned genes share.

Pixel plan for the Riptides.
- Undertow: long kelp that drifts back from the back and the sides of the
  head, the gene model undertow_kelp.
- Torrent: a small fin on the back of each calf and on the outer side of
  each ankle, the gene model torrent_fins.
- Surge has foam streaks on the outer, the front, and the back faces of the
  arms, rows 1 to 8.
- Capsizer has webbed fingers: the hands, arm rows 9 to 11, and the bottom
  of the arms.
- Breacher: two pieces of driftwood that stick out of the back, the gene
  model breacher_driftwood.
- Gills has open gill slits on the sides of the head, columns 3 to 6, rows
  3 to 7.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import BACK, FRONT, INNER, MARK_DIR, OUTER, VISUAL_DIR, Mark, arm, head_side, touched, write
from make_husk_marks import paint_box, save_model

# The outer layer is 0.25 pixel out from the body. A gene model sits a
# little further out.
OUT = 0.3

KELP = ("#3E7A2E", "#2E5E22", "#5A9A3A")
BLADDER = "#A8943E"


def undertow():
    # The kelp is the gene model undertow_kelp.
    return []


def undertow_model():
    # Long, flat blades of kelp. Three hang from the back of the head, and
    # one from each side. They hang below the head and drift back, as in a
    # current. On the head bone, the head is from y -8 to 0, and the back of
    # the head is at z 4.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def blade(length):
        def color(face, x, y):
            # The lowest rows are ragged.
            if y >= length - 2 and (x + y) % 2:
                return None
            if y % 4 == 2 and x == 0:
                return BLADDER
            return KELP[2] if x == 1 and y % 3 == 0 else KELP[(x + y) % 2]
        return color

    # A back blade: 2x10x0 at uv (0, 0). A side blade: 0x7x2 at uv (0, 12).
    paint_box(im, 0, 0, 2, 10, 0, blade(10))
    paint_box(im, 0, 12, 0, 7, 2, blade(7))
    back_z = 4 + OUT
    parts = []
    for x, rot in ((-3.5, [18, 0, 6]), (-0.5, [24, 0, 0]), (2, [16, 0, -6])):
        parts.append({"bone": "head", "uv": [0, 0], "from": [x, -6, back_z], "size": [2, 10, 0],
                      "pivot": [x + 1, -6, back_z], "rotation": rot})
    for x, rot in ((-4 - OUT, [12, 0, 10]), (4 + OUT, [12, 0, -10])):
        parts.append({"bone": "head", "uv": [0, 12], "from": [x, -5, 1], "size": [0, 7, 2],
                      "pivot": [x, -5, 2], "rotation": rot})
    save_model("undertow_kelp", im, parts)


FIN = ("#A8DCE4", "#7CB8C8", "#2E5E6E")


def torrent():
    # The fins are the gene model torrent_fins.
    return []


def torrent_model():
    # A small fin on the back of each calf, and a smaller fin on the outer
    # side of each ankle. Each fin is a flat shape that starts at the outer
    # layer, so it touches the leg. On the leg bone, the leg is from x -2 to
    # 2 and from y 0 to 12, and the back of the leg is at z 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def calf(face, x, y):
        # x counts from the leg out to the back. The fin is tall next to the
        # leg and short at its tip, and it has light rays.
        if x > [3, 3, 2, 1, 0][y]:
            return None
        return FIN[2] if x == y % 2 + 1 and y < 4 else FIN[x % 2]

    def ankle(face, x, y):
        if x > [2, 1, 0][y]:
            return None
        return FIN[2] if x == 1 else FIN[0]

    # The calf fin: 0x5x4 at uv (0, 0). The ankle fin: 0x3x3 at uv (10, 0).
    # The light fin, with dark rays, shows against the teal skin.
    paint_box(im, 0, 0, 0, 5, 4, calf)
    paint_box(im, 10, 0, 0, 3, 3, ankle)
    parts = []
    for bone, side in (("right_leg", -1), ("left_leg", 1)):
        parts.append({"bone": bone, "uv": [0, 0], "from": [0, 5, 2.25], "size": [0, 5, 4]})
        x = side * (2 + OUT)
        parts.append({"bone": bone, "uv": [10, 0], "from": [x, 8, -1], "size": [0, 3, 3],
                      "pivot": [x, 8, 0.5], "rotation": [0, 0, -side * 20]})
    save_model("torrent_fins", im, parts)


def surge():
    # Pale streaks of foam that run down the arms at a slant, on the outer,
    # the front, and the back faces, rows 1 to 8, with a soft trail.
    foam = Mark("#E4F6F2", alpha=230)
    trail = Mark("#E4F6F2", alpha=110)
    for face in (OUTER, FRONT, BACK):
        for row in range(1, 9):
            for col in range(4):
                turn = (row - col + face // 4) % 5
                if turn == 0:
                    foam.put(arm(face, col, row), 255 if row % 3 else 225)
                elif turn == 1 and row < 8:
                    trail.put(arm(face, col, row))
    return [foam, trail]


def capsizer():
    # Webbed fingers: dark fingers with a pale green web between them, on
    # the hands (arm rows 9 to 11) and under the hands. The lowest row of
    # the web has a scalloped edge.
    finger = Mark("#22403A")
    web = Mark("#86CCA8", alpha=215)
    for face in (OUTER, FRONT, INNER, BACK):
        for col in range(4):
            for row in (9, 10, 11):
                if col % 2 == 0:
                    finger.put(arm(face, col, row), 255 if row < 11 else 225)
                elif row > 9 and not (row == 11 and face in (OUTER, INNER)):
                    web.put(arm(face, col, row), 255 if row == 10 else 225)
    for x in range(48, 52):
        for y in range(16, 20):
            (finger if x % 2 == 0 else web).put([(x, y), (x - 8, y + 32)], 230)
    return [finger, web]


WOOD = ("#8A7A66", "#6E604E", "#B4A48C")


def breacher():
    # The driftwood is the gene model breacher_driftwood.
    return []


def breacher_model():
    # Two pieces of gray driftwood stuck in the back: a long one high on the
    # left that points up and back, and a short one low on the right that
    # points back and out. Each starts inside the body, so it comes out of
    # the back with no gap. On the body bone, the back is at z 2.
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def wood(face, x, y):
        if face in ("front", "back"):
            return WOOD[2]
        return WOOD[1] if (x + 2 * y) % 5 == 0 else WOOD[0]

    # The long piece: 2x2x10 at uv (0, 0). The short piece: 1x1x7 at uv (0,
    # 14). The long piece rises past the left shoulder, so it also shows
    # from the front.
    paint_box(im, 0, 0, 2, 2, 10, wood)
    paint_box(im, 0, 14, 1, 1, 7, wood)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [1, 3, 0], "size": [2, 2, 10],
         "pivot": [2, 4, 1], "rotation": [50, 25, 0]},
        {"bone": "body", "uv": [0, 14], "from": [-3, 8, 0], "size": [1, 1, 7],
         "pivot": [-2.5, 8.5, 1], "rotation": [-10, -40, 0]},
    ]
    save_model("breacher_driftwood", im, parts)


def gills():
    # Open gills on the sides of the neck: three slits on each side of the
    # head, low and toward the back, with a raw pink edge.
    slit = Mark("#4A0E18")
    rim = Mark("#C86478")
    for side in (0, 1):
        for col in (3, 5):
            for row in (4, 5, 6):
                slit.put(head_side(side, col, row), 255 if row == 5 else 225)
                rim.put(head_side(side, col + 1, row), 230)
        slit.put(head_side(side, 4, 7), 200)
    return [slit, rim]


GENES = {
    "undertow": undertow,
    "torrent": torrent,
    "surge": surge,
    "capsizer": capsizer,
    "breacher": breacher,
    "gills": gills,
}

MODELS = {
    "undertow": "originofspawns:undertow_kelp",
    "torrent": "originofspawns:torrent_fins",
    "breacher": "originofspawns:breacher_driftwood",
}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    undertow_model()
    torrent_model()
    breacher_model()
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks, MODELS)
        marked = [m for m in marks if not isinstance(m, dict) and m.order >= 0]
        print(gene, sum(len(m.pixels) for m in marked), "mark pixels")
    owners = {}
    for gene in GENES:
        with open(os.path.join(VISUAL_DIR, gene + ".json")) as fh:
            for p in touched(json.load(fh)):
                owners.setdefault(p, set()).add(gene)
    shared = {}
    for p, genes in owners.items():
        if len(genes) > 1:
            key = " and ".join(sorted(genes))
            shared[key] = shared.get(key, 0) + 1
    print("pixels that two or more drowned genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
