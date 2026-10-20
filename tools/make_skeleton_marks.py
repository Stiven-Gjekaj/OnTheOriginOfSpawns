"""Make the marks and the gene models of the skeleton genes.

Usage: python3 tools/make_skeleton_marks.py

The skeleton texture is 64x32. Its head and body have the humanoid layout,
but its arms and legs are 2x12x2, so each side face of a limb is 2 pixels
wide. The left limbs mirror the right limbs. Each visual names the skeleton
region map (textures/region/skeleton.png), from make_skeleton_region_map.py.
This script uses the helpers of make_gene_marks.py and make_husk_marks.py.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one pattern layer for each pattern.
- The gene models in MODELS, with their textures.

The script prints the pixels that two skeleton genes share.

Texture plan of the skeleton:
- The face is x 8 to 15, y 8 to 15. The eye sockets are x 9, 10, 13, and 14
  of y 12. The jaw is y 14.
- The body: the front is x 20 to 27, the back x 32 to 39, the sides x 16 to
  19 and x 28 to 31, from y 20 to 31. The ribs are y 22 and y 24 on all
  faces, the shoulders y 20, and the pelvis y 30 and 31. The spine is x 35
  and 36 of the back.
- An arm is at uv (40, 16), a leg at uv (0, 16). The side faces are, from
  left to right: outer, front, inner, back, 2 pixels each, from y 18 to 29.

Pixel plan for the Marksmen.
- Eagle Eye has the eye sockets, and the pixels over and under them.
- Deadeye has a crack over the right eye: face x 9 to 11, y 8 to 10, and
  the top of the head over it.
- Longdraw: a leather bracer on the left forearm, the gene model
  longdraw_bracer.
- Quickdraw has the fingertips: arm rows 10 and 11, and the bottom of the
  arm.
- Shieldbreaker has a notch in each rib, at y 22 and y 24.
- High Ground: claws of bone on the feet, the gene model high_ground_claws.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, touched, write
from make_husk_marks import paint_box, save_model

REGION_MAP = "originofspawns:textures/region/skeleton.png"

# The side faces of a limb, as the x offset from the start of the limb UV.
OUTER, FRONT, INNER, BACK = 0, 2, 4, 6
SIDES = (OUTER, FRONT, INNER, BACK)
ARM, LEG = (40, 16), (0, 16)

BONE = ("#E8E4D8", "#C8C2B0", "#8E887A")


def arm(face, col, row):
    """Return the texture pixel of a pixel on a side face of the arm."""
    return (ARM[0] + face + col, ARM[1] + 2 + row)


def leg(face, col, row):
    return (LEG[0] + face + col, LEG[1] + 2 + row)


def eagle_eye():
    # A bright ring in each eye socket: the socket glows amber, and a thin
    # pale gold ring shows over and under it.
    ring = Mark("#FFD860")
    core = Mark("#B07A20")
    halo = Mark("#F2D890", alpha=150)
    for outer, inner in ((9, 10), (14, 13)):
        ring.put((outer, 12))
        core.put((inner, 12))
        halo.put((outer, 11)).put((inner, 11)).put((outer, 13)).put((inner, 13))
    return [ring, core, halo]


def deadeye():
    # A crack over the right eye (x 9 and 10 of the face): a dark, jagged
    # line from the top of the head down to the brow, with a pale chipped
    # edge. It stops at y 10, over the Eagle Eye ring.
    crack = Mark("#3A3834")
    chip = Mark("#F6F2E8")
    for p in ((10, 7), (10, 8), (10, 9), (9, 10)):
        crack.put(p)
    crack.put((11, 6), 220)
    chip.put((11, 8), 230).put((9, 9), 230).put((10, 10), 210)
    return [crack, chip]


LEATHER = ("#7A4E2C", "#5E3A20", "#B08A5A")


def longdraw():
    # The bracer is the gene model longdraw_bracer.
    return []


def longdraw_model():
    # A leather bracer round the left forearm, arm rows 6 to 9, with two
    # straps and a light stitch. On an arm bone, the arm is from x -1 to 1
    # and from y -2, so row 6 is at y 4.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def bracer(face, x, y):
        if face in ("top", "bottom"):
            return LEATHER[1]
        if y in (0, 3):
            return LEATHER[1]
        return LEATHER[2] if (x + y) % 3 == 0 else LEATHER[0]

    # The bracer: 2x4x2 at uv (0, 0).
    paint_box(im, 0, 0, 2, 4, 2, bracer)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [-1, 4, -1], "size": [2, 4, 2], "inflate": 0.3}]
    save_model("longdraw_bracer", im, parts)


def quickdraw():
    # Worn, white fingertips: the tips are a clean white, with a gray line
    # of wear above them.
    tip = Mark("#FFFFFF")
    wear = Mark("#5A564C")
    for face in SIDES:
        for col in (0, 1):
            tip.put(arm(face, col, 11)).put(arm(face, col, 10), 240)
            wear.put(arm(face, col, 9), 220 if (face + col) % 2 else 255)
    for x in (44, 45):
        for y in (16, 17):
            tip.put((x, y))
    return [tip, wear]


def shieldbreaker():
    # A notch cut in each rib: a dark V in the ribs at y 22 and y 24, on the
    # front, the sides, and the back (but the spine), with a light cut edge.
    notch = Mark("#2E2C28")
    cut = Mark("#F4F0E6")
    for x in (21, 26, 17, 30, 33, 38):
        notch.put((x, 22))
    for x in (20, 27, 18, 29, 34, 37):
        notch.put((x, 24))
    for x in (22, 25, 16, 31):
        cut.put((x, 22), 230)
    return [notch, cut]


CLAW = ("#E2DAC2", "#B8AE94", "#5A5446")


def high_ground():
    # The claws are the gene model high_ground_claws.
    return []


def high_ground_model():
    # Two claws of bone on the front of each foot, that curve down to the
    # ground, with dark tips. On a leg bone, the leg is from x -1 to 1 and
    # from z -1 to 1, and the foot ends at y 12.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def claw(face, x, y):
        if face == "front":
            return CLAW[2]
        return CLAW[0] if face == "top" else CLAW[1]

    # A claw: 1x1x3 at uv (0, 0).
    paint_box(im, 0, 0, 1, 1, 3, claw)
    parts = []
    for bone in ("right_leg", "left_leg"):
        for x in (-1, 0):
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, 11, -3.2], "size": [1, 1, 3],
                          "pivot": [x + 0.5, 11.5, -1], "rotation": [-20, 0, 0]})
    save_model("high_ground_claws", im, parts)


GENES = {
    "eagle_eye": eagle_eye,
    "deadeye": deadeye,
    "longdraw": longdraw,
    "quickdraw": quickdraw,
    "shieldbreaker": shieldbreaker,
    "high_ground": high_ground,
}

MODELS = {
    "longdraw": "originofspawns:longdraw_bracer",
    "high_ground": "originofspawns:high_ground_claws",
}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    longdraw_model()
    high_ground_model()
    for gene, make in GENES.items():
        marks = make()
        write(gene, marks, MODELS, REGION_MAP)
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
    print("pixels that two or more skeleton genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
