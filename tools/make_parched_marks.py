"""Make the marks and the gene models of the parched genes.

Usage: python3 tools/make_parched_marks.py

The parched (the desert skeleton) has a 64x64 texture and its own region
map (textures/region/parched.png, from make_parched_region_map.py). The top
half of the texture has the bones of the skeleton, with the right arm at
uv (40, 16) and the left arm on its own uv (56, 16). The bottom half has
wraps of cloth over the bones: the head wrap at uv (0, 32), the body wrap
at uv (16, 48), the right sleeve (3x12x3) at uv (42, 33), the left sleeve at
uv (40, 48), and the leg wraps (3x12x3) at uv (0, 49) and (4, 49). The arms
hang from x -5.5 and x 5.5. The parched holds a bow in the right hand.

What the wraps leave in view: most of the face (the wrap covers the brow),
the middle of the chest and of the back, the forearms and the hands (arm
rows 8 to 11), and the shins and the feet (leg rows 8 to 11). A mark that
must read goes on the bones and on the wrap over them.

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- textures/gene_marks/<gene>_<name>.png, the masks of the erase layers.
- gene_visuals/<gene>.json, with one layer for each pattern and mask.
- The gene models in MODELS, with their textures.

The script prints the pixels that two parched genes share.

Pixel plan for the Enfeeblers.
- Sap has gray, drained streaks on the bow arm (the right arm): every other
  column of the bones and of the sleeve, rows 1 to 8. The hands stay free.
- Fumble has long, crooked fingers: pale finger lines with dark gaps on arm
  rows 9 to 11 of both arms, and on the bottom of the arms.
- Exploit has a thin, pale ring round each eye socket (y 11 and 13, and
  beside the sockets), with dark sockets.
- Curse Cloud has gray dust in the rib cage: specks on the front ribs (y 21
  to 27) and on the wrap of the chest.
- Unnerving Rattle has loose teeth: crooked pale teeth with dark gaps on the
  mouth (y 14), and one tooth that has dropped (y 15).
- Withering Glare has a dull gray light in the eye sockets.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_skeleton_marks import BACK, FRONT, INNER, OUTER, SIDES, leg
from make_stray_marks import changed_pixels, erase

REGION_MAP = "originofspawns:textures/region/parched.png"

BONE = ("#E8E0C8", "#B8AE98", "#6E6656")
SAND = ("#D8C090", "#B89A68", "#8A7048")
DARK = "#2A2218"

# The x offset of each side face of a 3x12x3 wrap from its uv.
WRAP_SIDES = {OUTER: 0, FRONT: 3, INNER: 6, BACK: 9}


def arm(face, col, row, left=False):
    """Return the texture pixel of a pixel on a side face of an arm bone.

    The right arm is at uv (40, 16), the left arm at uv (56, 16).
    """
    return ((56 if left else 40) + face + col, 18 + row)


def sleeve(face, col, row, left=False):
    """Return a pixel of a side face of a sleeve (3 pixels wide).

    The right sleeve is at uv (42, 33), the left sleeve at uv (40, 48). On
    the left sleeve, the outer face is the left face of the box.
    """
    if left:
        face = {OUTER: INNER, INNER: OUTER}.get(face, face)
        return (40 + WRAP_SIDES[face] + col, 51 + row)
    return (42 + WRAP_SIDES[face] + col, 36 + row)


def leg_wrap(face, col, row):
    """Return the pixels of a side face of the leg wraps, of both legs."""
    return [(0 + WRAP_SIDES[face] + col, 52 + row), (4 + WRAP_SIDES[face] + col, 52 + row)]


def both_arms(face, col, row):
    return [arm(face, col, row), arm(face, col, row, left=True)]


def both_sleeves(face, col, row):
    return [sleeve(face, col, row), sleeve(face, col, row, left=True)]


def sap():
    # Gray, drained streaks on the bow arm, the right arm: every other
    # column of the bones and of the sleeve is a dull gray, rows 1 to 8,
    # as if the life ran out of the arm. The hand stays free for Fumble.
    gray = Mark("#9A9894")
    dull = Mark("#6E6C68")
    for face in SIDES:
        for row in range(1, 9):
            gray.put(arm(face, 0, row))
            if row > 3:
                dull.put(arm(face, 1, row), 200)
        for col in (0, 2):
            for row in range(1, 9):
                gray.put(sleeve(face, col, row))
    return [gray, dull]


def fumble():
    # Long, crooked fingers: on each face of the hands of both arms, one
    # long pale finger bone runs down from the knuckle (row 9) to the tip
    # (row 11), and it bends to the side at the tip. A dark gap is beside
    # it. The bottom of the arm has the finger tips.
    finger = Mark(BONE[0])
    gap = Mark(DARK)
    for face in SIDES:
        col = face // 2 % 2
        for row, c in ((9, col), (10, col), (11, 1 - col)):
            for p in both_arms(face, c, row):
                finger.put(p)
            for p in both_arms(face, 1 - c, row):
                gap.put(p) if row > 9 else None
    for u in (40, 56):
        finger.put((u + 4, 16)).put((u + 5, 17))
        gap.put((u + 5, 16)).put((u + 4, 17))
    return [finger, gap]


def exploit():
    # A thin, pale ring round each eye socket: pale bone pixels over and
    # under each socket (y 11 and 13) and beside it, and the sockets
    # themselves go dark, so the ring reads as a cold, watching eye.
    ring = Mark("#F4F0E4")
    dark = Mark("#141008")
    for outer, inner, side in ((9, 10, 8), (14, 13, 15)):
        dark.put((outer, 12)).put((inner, 12))
        for x in (outer, inner):
            ring.put((x, 11)).put((x, 13))
        ring.put((side, 12))
    return [ring, dark]


def curse_cloud():
    # Gray dust in the rib cage: specks of gray dust on the front ribs (y
    # 21 to 27) and on the wrap of the chest, thicker low down, as if the
    # dust settles in the cage.
    dust = Mark("#8A8884")
    pale = Mark("#B8B6B0")
    for x in range(20, 28):
        for y in range(21, 28):
            n = noise100(x, y, 311)
            if n < 20 + 5 * (y - 21):
                (pale if n % 3 == 0 else dust).put((x, y))
        for y in range(52, 60):
            if noise100(x, y, 312) < 30:
                dust.put((x, y))
    return [dust, pale]


def unnerving_rattle():
    # Loose teeth that rattle: the mouth (y 14) has crooked, pale teeth
    # with dark gaps between them, set at different heights, and one tooth
    # has dropped down (y 15).
    tooth = Mark(BONE[0])
    gap = Mark(DARK)
    for x in range(9, 15):
        (tooth if x % 2 else gap).put((x, 14))
    tooth.put((10, 13)).put((12, 15))
    gap.put((13, 13))
    return [tooth, gap]


def withering_glare():
    # A dull gray light in the eye sockets: the sockets have a flat, dull
    # gray in place of the pale yellow eyes, with a darker gray on their
    # inner side.
    dull = Mark("#8A8A86")
    dark = Mark("#5A5A56")
    for outer, inner in ((9, 10), (14, 13)):
        dull.put((outer, 12))
        dark.put((inner, 12))
    return [dull, dark]


GENES = {
    "sap": sap,
    "fumble": fumble,
    "exploit": exploit,
    "curse_cloud": curse_cloud,
    "unnerving_rattle": unnerving_rattle,
    "withering_glare": withering_glare,
}

MODELS = {}

MODEL_MAKERS = []


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
    print("pixels that two or more parched genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
