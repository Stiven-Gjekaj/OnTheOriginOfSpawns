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
- Rot Mend has white threads of fungus on the front of the upper ribs (x
  21 to 26, y 21 to 24).
- Spore Lung has pale spores round the mouth of the outer face (y 13 to
  15), and on the bottom of the head.

Pixel plan for the Mirefolk.
- Wader has wet mud on the legs up to the knees: leg rows 6 to 11 and the
  bottom of the leg.
- Mudcake has a cracked crust of dried mud on the outer layer of the
  sides and the back of the body. The front stays free for the Plague
  Carrier pustules.
- Mudburrow has mud that drips from the skull: patches on the top of the
  outer head, and drips down its sides, front, and back (y 8 to 12).
- Mire Grip has webbed fingers of rotten skin: arm rows 9 and 10.
- Swamp Gas has bubbles of gas at the pelvis, under the outer layer: y 28
  to 31 of the body.
- Leechbone: black leeches on the forearms, the gene model
  leechbone_leeches.

Pixel plan for the Venomers.
- Venom Gland: a green sac under the jaw, the gene model venom_gland_sac.
- Toxic Tip has bright green finger tips: arm row 11 and the bottom of the
  arm.
- Bile Shot has yellow stains on the lower ribs of the front (x 20 to 27,
  y 24 to 27).
- Rotting Touch has green rot in the joints: the shoulders and the hips
  (row 0), the elbows (arm row 5), and the knees (leg rows 4 and 5).
- Antivenom has a bright green light in the eyes of the outer face, and
  the gene model antivenom_eyes makes it glow.
- Plague Carrier has green pustules on the front of the outer layer of the
  body (x 20 to 27), and on the leaves over the upper arms.
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
    # White threads of fungus on the front of the upper ribs (y 21 to 24),
    # where the vines leave the ribs bare: thin lines that cross between
    # the ribs, with a few knots where they meet.
    thread = Mark(CREAM[0], alpha=230)
    knot = Mark("#FFFFFF")
    for x in range(21, 27):
        for y in range(21, 25):
            if (x + y) % 3 == 0 or (x - y) % 4 == 0:
                thread.put((x, y))
    for p in ((22, 23), (25, 23), (24, 21)):
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



MUD = ("#4A3828", "#36281C", "#6A5238", "#2A1E14")
DRY = ("#9A8466", "#7E6A50", "#4E3E2A")


def wader():
    # Wet mud on the legs up to the knees: dark mud on leg rows 6 to 11 of
    # all the faces and on the bottom of the leg, with glossy wet spots.
    # The knees (rows 4 and 5) stay free for the Rotting Touch rot.
    mud = Mark(MUD[0])
    deep = Mark(MUD[1])
    wet = Mark(MUD[2])
    for face in SIDES:
        for col in (0, 1):
            for row in range(6, 12):
                n = noise100(face + col, row, 281)
                (wet if n < 15 else deep if row > 9 else mud).put(leg(face, col, row))
    for x in (4, 5):
        for y in (16, 17):
            deep.put((x, y))
    return [mud, deep, wet]


def mudcake():
    # A cracked crust of dried mud: pale, dry mud on the outer layer of the
    # sides and the back of the body (y 20 to 31), with dark cracks in a
    # net.
    crust = Mark(DRY[0], target="outer")
    shade = Mark(DRY[1], target="outer")
    crack = Mark(DRY[2], target="outer")
    for x in list(range(16, 20)) + list(range(28, 40)):
        for y in range(20, 32):
            if (x * 2 + y) % 7 == 0 or (x - 2 * y) % 9 == 0:
                crack.put((x, y))
            else:
                (shade if noise100(x, y, 282) < 30 else crust).put((x, y))
    return [crust, shade, crack]


def mudburrow():
    # Mud that drips from the skull: patches of dark, wet mud on the top of
    # the outer head, and drips of it down the sides, the front, and the
    # back (y 8 to 12), each drip a different length. The eyes stay clear.
    mud = Mark(MUD[0], target="outer")
    wet = Mark(MUD[2], target="outer")
    for x in range(8, 16):
        for y in range(0, 8):
            if noise100(x // 2, y // 2, 283) < 60:
                (wet if (x + y) % 4 == 0 else mud).put((x, y))
    for x in range(0, 32):
        length = 8 + (x * 7 + 3) % 5
        for y in range(8, length + 1):
            if (x, y) in ((9, 12), (10, 12), (13, 12), (14, 12)):
                continue
            (wet if y == length else mud).put((x, y))
    return [mud, wet]


def mire_grip():
    # Webbed fingers of rotten skin: a sheet of pale, bloated, gray-green
    # skin over the hands (arm rows 9 and 10), with darker lines where the
    # webs pull between the fingers. The hands of the bogged are dark, so
    # the pale skin reads against them. The finger tips (row 11) stay dark.
    skin = Mark("#A8B088")
    web = Mark("#6A7050")
    for face in SIDES:
        for col in (0, 1):
            skin.put(arm(face, col, 9))
            (web if col == face // 2 % 2 else skin).put(arm(face, col, 10))
    return [skin, web]


def swamp_gas():
    # Bubbles of gas at the pelvis, under the outer layer: small pale green
    # bubbles with a bright spot, on the front, the sides, and the back of
    # the body, y 28 to 31.
    bubble = Mark("#D2F0A0")
    shine = Mark("#FFFFFF")
    rim = Mark("#4E6A28")
    for cx, cy in ((21, 30), (25, 29), (18, 30), (30, 29), (34, 30), (38, 29), (23, 28)):
        bubble.put((cx, cy)).put((cx + 1, cy))
        shine.put((cx, cy - 1)) if cy > 28 else shine.put((cx + 1, cy + 1))
        rim.put((cx, cy + 1)) if cy < 31 else None
    return [bubble, shine, rim]


def leechbone():
    # The leeches are the gene model leechbone_leeches.
    return []


def leechbone_model():
    # Black leeches on the forearms: on each arm, a leech on the outer side
    # and a leech on the front, each a short, fat body that holds on to the
    # bone (it goes a little into the arm). A leech is black with a dark
    # red band near its head. On an arm bone, the arm is from x -1 to 1 and
    # from z -1 to 1, and row r is at y r - 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def leech(face, x, y):
        return "#4A1E1E" if y == 0 else "#1A1614" if (x + y) % 2 else "#2A2420"

    # A leech: 1x3x1 at uv (0, 0).
    paint_box(im, 0, 0, 1, 3, 1, leech)
    parts = []
    for bone, side in (("right_arm", -1), ("left_arm", 1)):
        x = -1.6 if side < 0 else 0.6
        parts.append({"bone": bone, "uv": [0, 0], "from": [x, 4, -0.5], "size": [1, 3, 1], "inflate": 0.1})
        parts.append({"bone": bone, "uv": [0, 0], "from": [-0.5, 6.5, -1.6], "size": [1, 3, 1], "inflate": 0.1})
    save_model("leechbone_leeches", im, parts)


VENOM = ("#78B838", "#3E6A1E", "#B8E878", "#A8F040")


def venom_gland():
    # The sac is the gene model venom_gland_sac.
    return []


def venom_gland_model():
    # A green sac under the jaw: a swollen sac of venom that hangs from the
    # bottom of the skull, in front of the neck, with dark veins and a
    # bright, wet shine, and a smaller bulge under it. On the head bone, the
    # bottom of the head is at y 0, the face is at z -4, and the front of
    # the body is at z -2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def sac(face, x, y):
        if face == "front" and x == 1 and y == 0:
            return VENOM[2]
        return VENOM[1] if (x + 2 * y) % 4 == 0 else VENOM[0]

    # The sac: 4x2x2 at uv (0, 0). The bulge: 2x1x2 at uv (0, 6).
    paint_box(im, 0, 0, 4, 2, 2, sac)
    paint_box(im, 0, 6, 2, 1, 2, lambda face, x, y: VENOM[0] if face != "bottom" else VENOM[1])
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-2, 0, -4.4], "size": [4, 2, 2], "inflate": 0.2},
        {"bone": "head", "uv": [0, 6], "from": [-1, 2, -4.2], "size": [2, 1, 2]},
    ]
    save_model("venom_gland_sac", im, parts)


def toxic_tip():
    # Bright green finger tips, as if the fingers were dipped in venom: the
    # tips (arm row 11) and the bottom of the arm are a bright, toxic green
    # with a pale shine. The bogged has dark hands, so the green reads.
    green = Mark(VENOM[3])
    shine = Mark("#E0FFB0")
    for face in SIDES:
        for col in (0, 1):
            (shine if (face, col) in ((FRONT, 0), (BACK, 1)) else green).put(arm(face, col, 11))
    for x in (44, 45):
        for y in (16, 17):
            green.put((x, y))
    return [green, shine]


def bile_shot():
    # Yellow stains of bile: blotches of sick yellow on the lower ribs of
    # the front (y 24 to 27), under the Rot Mend threads and over the Swamp
    # Gas bubbles.
    bile = Mark("#C8B030")
    dark = Mark("#8A7418")
    for x in range(20, 28):
        for y in range(24, 28):
            n = noise100(x, y, 291)
            if n < 45:
                bile.put((x, y))
            elif n < 60:
                dark.put((x, y))
    return [bile, dark]


def rotting_touch():
    # Green rot in the joints: dark green rot with bright spots at the
    # shoulders and the hips (row 0, and the tops of the limbs), at the
    # elbows (arm row 5), and at the knees (leg rows 4 and 5).
    rot = Mark("#3E6420")
    spot = Mark("#8CC040")
    for face in SIDES:
        for col in (0, 1):
            rot.put(arm(face, col, 0)).put(leg(face, col, 0))
            (spot if col == face // 2 % 2 else rot).put(arm(face, col, 5))
            rot.put(leg(face, col, 4))
            (spot if col != face // 2 % 2 else rot).put(leg(face, col, 5))
    for u, v in ((40, 16), (0, 16)):
        rot.put((u + 2, v)).put((u + 3, v + 1))
        spot.put((u + 3, v)).put((u + 2, v + 1))
    return [rot, spot]


ANTI = ("#50FF50", "#B0FFB0")
# The eye pixels of the outer face and their colors: a bright core in the
# inner pixel of each eye, and a pale rim in the outer pixel.
ANTI_PIXELS = (((9, 12), 0), ((10, 12), 1), ((13, 12), 1), ((14, 12), 0))


def antivenom():
    # A bright green light in the eyes of the outer face. The light is on
    # the texture, so it shows in the day, and the gene model antivenom_eyes
    # makes it glow.
    marks = [Mark(color, target="outer") for color in ANTI]
    for p, color in ANTI_PIXELS:
        marks[color].put(p)
    return marks


def antivenom_model():
    # A square that glows on each eye pixel, just in front of the outer face
    # (z -4.2). Face pixel (x, y) is at x - 12, y - 16.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    for n, color in enumerate(ANTI):
        paint_box(im, 4 * n, 0, 1, 1, 0, lambda face, x, y, c=color: c)
    parts = [{"bone": "head", "uv": [4 * color, 0], "from": [x - 12, y - 16, -4.3], "size": [1, 1, 0],
              "glow": True} for (x, y), color in ANTI_PIXELS]
    save_model("antivenom_eyes", im, parts)


def plague_carrier():
    # Green pustules on the cloth of the chest: round, bright green
    # pustules with a pale top and a dark rim, on the vines of the front of
    # the outer layer (x 20 to 27), and on the leaves over the upper arms.
    pus = Mark(VENOM[0], target="outer")
    top = Mark(VENOM[2], target="outer")
    rim = Mark("#2E4A16", target="outer")
    for x, y in ((20, 20), (26, 21), (21, 23), (27, 24), (20, 26), (26, 27),
                 (40, 21), (44, 22), (46, 20), (50, 21), (53, 23)):
        pus.put((x, y))
        top.put((x + 1, y))
        rim.put((x, y + 1)).put((x + 1, y + 1))
    return [pus, top, rim]

GENES = {
    "puffball": puffball,
    "shelfback": shelfback,
    "mushroom_shot": mushroom_shot,
    "regrowth": regrowth,
    "rot_mend": rot_mend,
    "spore_lung": spore_lung,
    "wader": wader,
    "mudcake": mudcake,
    "mudburrow": mudburrow,
    "mire_grip": mire_grip,
    "swamp_gas": swamp_gas,
    "leechbone": leechbone,
    "venom_gland": venom_gland,
    "toxic_tip": toxic_tip,
    "bile_shot": bile_shot,
    "rotting_touch": rotting_touch,
    "antivenom": antivenom,
    "plague_carrier": plague_carrier,
}

MODELS = {
    "puffball": "originofspawns:puffball_cap",
    "shelfback": "originofspawns:shelfback_shelves",
    "mushroom_shot": "originofspawns:mushroom_shot_caps",
    "leechbone": "originofspawns:leechbone_leeches",
    "venom_gland": "originofspawns:venom_gland_sac",
    "antivenom": "originofspawns:antivenom_eyes",
}

MODEL_MAKERS = [puffball_model, shelfback_model, mushroom_shot_model, leechbone_model, venom_gland_model,
                antivenom_model]


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
