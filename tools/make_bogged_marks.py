"""Make the marks and the gene models of the bogged genes.

Usage: python3 tools/make_bogged_marks.py

The bogged has the skeleton body and the skeleton region map, so its bone
marks use the same pixels as the skeleton marks (see
make_skeleton_marks.py). Over the bones, it has an outer layer of moss and
vines: the vanilla bogged overlay. The outer layer has the zombie layout,
with 4x12x4 arms and legs, 0.2 pixel larger than the body. A mark with the
target "outer" goes on the outer layer only, and it keeps the holes of the
outer layer. A mark with no target goes on the bones only.

The bogged has mushrooms on its head: a red mushroom on the top at the
left back (x 1 to 5, z 1 to 5), a brown mushroom on the top at the right
front (x -5 to -1, z -5 to -1), and a brown mushroom out of the back of the
head, low on the right. The gene models keep clear of them.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- textures/gene_marks/<gene>_<name>.png, the masks of the erase layers.
- gene_visuals/<gene>.json, with one layer for each pattern and mask.
- The gene models in MODELS, with their textures.

The script prints the pixels that two bogged genes share, on the bones and
on the outer layer.

What the outer layer covers:
- The head: all of it. The outer layer has its own face, with two pale
  green eyes (x 9, 10, 13, and 14 of y 12).
- The body: the shoulders and a V of vines on the front, so the middle of
  the ribs shows (x 22 to 25). It covers the back down to y 25, and the
  sides.
- The arms: the tops, and leaves that hang over the upper arms.
- The legs: nothing. The legs show all of their bone.

Pixel plan for the Sporecaps.
- Puffball: a large puffball mushroom on the top of the skull, at the left
  front, the gene model puffball_cap.
- Shelfback: shelf fungus in steps down the back, the gene model
  shelfback_shelves.
- Mushroom Shot: red mushroom caps on the bow arm (the right arm), the
  gene model mushroom_shot_caps.
- Regrowth has small mushrooms in the eye sockets of the outer face: a
  red cap over the right eye and a brown cap over the left eye (y 11), with
  their stems in the sockets (y 12).
- Rot Mend has white threads of fungus on the front of the ribs (x 21 to
  26, y 21 to 27).
- Spore Lung has pale spores round the mouth of the outer face (y 13 to
  15), and on the bottom of the head.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_skeleton_marks import BACK, FRONT, INNER, OUTER, REGION_MAP, SIDES, arm, leg
from make_stray_marks import changed_pixels, cloth_arm, erase

RED = ("#C8282A", "#8E1C1E", "#F2EEE0")
BROWN = ("#A07850", "#7A5A3A", "#D8C8A8")
CREAM = ("#F2EEDC", "#D8D0B4", "#B4A888")
SPORE = ("#E8E0A8", "#C8D890")


def puffball():
    # The puffball is the gene model puffball_cap.
    return []


def puffball_model():
    # A large puffball mushroom on the top of the skull, at the left front,
    # where the red and the brown mushrooms of the bogged leave room: a
    # round, cream ball with tan specks, and a smaller top on it. On the
    # head bone, the top of the head is at y -8, and the outer layer is 0.2
    # pixel out of it.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def ball(face, x, y):
        if face == "bottom":
            return CREAM[2]
        if face != "top" and y == 3:
            return CREAM[1]
        return CREAM[2] if noise100(x, y + 7 * len(face), 261) < 15 else CREAM[0]

    # The ball: 5x3x5 at uv (0, 0). The top: 3x1x3 at uv (0, 8).
    paint_box(im, 0, 0, 5, 3, 5, ball)
    paint_box(im, 0, 8, 3, 1, 3, lambda face, x, y: CREAM[0] if face == "top" else CREAM[1])
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-0.5, -11, -4.5], "size": [5, 3, 5]},
        {"bone": "head", "uv": [0, 8], "from": [0.5, -12, -3.5], "size": [3, 1, 3]},
    ]
    save_model("puffball_cap", im, parts)


def shelfback():
    # The shelves are the gene model shelfback_shelves.
    return []


def shelfback_model():
    # Shelf fungus in steps down the back: four flat brackets that grow out
    # of the spine, each one a little to the side of the one over it. Each
    # bracket starts 1 pixel inside the back (z 1), so that it holds on
    # where the ribs have gaps, and sticks out 2 pixels. A bracket is tan on top with a cream rim, and
    # cream under it. On the body bone, the body is from y 0 to 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def shelf(face, x, y):
        if face == "top":
            return BROWN[2] if y == 2 else BROWN[0] if x % 3 else BROWN[1]
        if face == "bottom":
            return CREAM[1]
        return BROWN[2]

    # A bracket: 4x1x3 at uv (0, 0). A small bracket: 3x1x3 at uv (0, 6).
    paint_box(im, 0, 0, 4, 1, 3, shelf)
    paint_box(im, 0, 6, 3, 1, 3, shelf)
    parts = []
    for x, y, small in ((-2, 1.5, False), (-0.5, 4.5, False), (-3, 7.5, False), (-1, 10, True)):
        parts.append({"bone": "body", "uv": [0, 6] if small else [0, 0], "from": [x, y, 1],
                      "size": [3 if small else 4, 1, 3]})
    save_model("shelfback_shelves", im, parts)


def mushroom_shot():
    # The caps are the gene model mushroom_shot_caps.
    return []


def mushroom_shot_model():
    # Red mushroom caps on the bow arm, the right arm: a cap on the outer
    # side of the forearm, a cap on its front, and a small cap on its back.
    # Each cap is red with white spots, on a short pale stem that goes into
    # the arm. On the right arm bone, the arm is from x -1 to 1 and from z
    # -1 to 1, and row r is at y r - 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def cap(face, x, y):
        if face == "bottom":
            return RED[1]
        return RED[2] if (x + y) % 3 == 0 and face == "top" else RED[0]

    # A cap: 2x1x2 at uv (0, 0). A small cap: 1x1x1 at uv (0, 4). A stem:
    # 1x1x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 1, 2, cap)
    paint_box(im, 0, 4, 1, 1, 1, lambda face, x, y: RED[0] if face != "bottom" else RED[1])
    paint_box(im, 8, 0, 1, 1, 1, lambda face, x, y: CREAM[1])
    parts = [
        {"bone": "right_arm", "uv": [8, 0], "from": [-1.6, 5, -0.5], "size": [1, 1, 1]},
        {"bone": "right_arm", "uv": [0, 0], "from": [-2.4, 4, -1], "size": [2, 1, 2]},
        {"bone": "right_arm", "uv": [8, 0], "from": [-0.5, 7, -1.6], "size": [1, 1, 1]},
        {"bone": "right_arm", "uv": [0, 0], "from": [-1, 6, -2.4], "size": [2, 1, 2]},
        {"bone": "right_arm", "uv": [0, 4], "from": [-0.5, 8, 1], "size": [1, 1, 1]},
    ]
    save_model("mushroom_shot_caps", im, parts)


def regrowth():
    # Small mushrooms grow in the eye sockets of the outer face: a red cap
    # with a white spot over the right eye, and a brown cap over the left
    # eye (y 11), with their pale stems down in the sockets (y 12). The
    # other pixel of each socket goes dark.
    red = Mark(RED[0], target="outer")
    spot = Mark(RED[2], target="outer")
    brown = Mark(BROWN[0], target="outer")
    stem = Mark(CREAM[1], target="outer")
    dark = Mark("#1E2814", target="outer")
    red.put((9, 11))
    spot.put((10, 11))
    brown.put((13, 11)).put((14, 11))
    stem.put((10, 12)).put((13, 12))
    dark.put((9, 12)).put((14, 12))
    return [red, spot, brown, stem, dark]


def rot_mend():
    # White threads of fungus on the front of the ribs, where the vines
    # leave the ribs bare: thin lines that cross between the ribs, with a
    # few knots where they meet.
    thread = Mark(CREAM[0], alpha=230)
    knot = Mark("#FFFFFF")
    for x in range(21, 27):
        for y in range(21, 28):
            if (x + y) % 3 == 0 or (x - y) % 4 == 0:
                thread.put((x, y))
    for p in ((22, 23), (25, 23), (23, 26)):
        knot.put(p)
    return [thread, knot]


def spore_lung():
    # Pale spores that drift from the jaw: dots of pale yellow and pale
    # green round the mouth of the outer face (y 13 to 15), on the sides of
    # the head next to it, and on the bottom of the head.
    pale = Mark(SPORE[0], target="outer")
    green = Mark(SPORE[1], target="outer")
    for p in ((8, 15), (10, 15), (13, 15), (15, 14), (8, 13), (12, 15)):
        pale.put(p)
    for p in ((9, 14), (14, 15), (15, 12), (7, 14), (16, 15), (1, 15)):
        green.put(p)
    for x in range(8, 16):
        for y in (13, 14, 15):
            if (x, y) not in pale.pixels and (x, y) not in green.pixels and noise100(x, y, 272) < 35:
                (pale if x % 2 else green).put((x, y), 230)
    for x in range(16, 24):
        for y in range(0, 8):
            if noise100(x, y, 271) < 25:
                (pale if (x + y) % 2 else green).put((x, y))
    return [pale, green]


GENES = {
    "puffball": puffball,
    "shelfback": shelfback,
    "mushroom_shot": mushroom_shot,
    "regrowth": regrowth,
    "rot_mend": rot_mend,
    "spore_lung": spore_lung,
}

MODELS = {
    "puffball": "originofspawns:puffball_cap",
    "shelfback": "originofspawns:shelfback_shelves",
    "mushroom_shot": "originofspawns:mushroom_shot_caps",
}

MODEL_MAKERS = [puffball_model, shelfback_model, mushroom_shot_model]


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
    print("pixels that two or more bogged genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
