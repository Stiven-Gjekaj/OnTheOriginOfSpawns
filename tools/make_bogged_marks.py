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
- Wader has dark, wet mud on the legs up to the knees: leg rows 4 to 11
  and the bottom of the leg.
- Mudcake has a cracked crust of dried mud on the outer layer of the
  sides and the back of the body. The front stays free for the
  Blightburst pustules.
- Mudburrow has mud that drips from the skull: patches on the top of the
  outer head, and drips down its sides, front, and back (y 8 to 12).
- Mire Grip has large webbed hands of green, rotten skin: arm rows 7 to
  10, over the Barkskin bark (order 1).
- Swamp Gas has large, bright green bubbles of gas at the pelvis (y 28 to
  31 of the body) and on the lower edge of the outer layer over it (y 24
  to 30).
- Leechbone: black leeches on the forearms, the gene model
  leechbone_leeches.

Pixel plan for the Venomers.
- Venom Gland: a green sac under the jaw, the gene model venom_gland_sac.
- Toxic Tip has bright green finger tips: arm row 11 and the bottom of the
  arm.
- Bile Shot has large, yellow-green stains on the front of the ribs (x 20
  to 27, y 23 to 25), with drips down to y 29.
- Rotting Touch has green rot in the joints: the shoulders and the hips
  (row 0), the elbows (arm row 5), and the knees (leg rows 4 and 5).
- Antivenom has a bright green light in the eyes of the outer face, and
  the gene model antivenom_eyes makes it glow.
- Blightburst has green pustules on the front of the outer layer of the
  body (x 20 to 27), and on the leaves over the upper arms.

Pixel plan for the Vinebound.
- Rootshot: roots that coil round the bow arm (the right arm), the gene
  model rootshot_roots.
- Creeper Vines: vines that hang down the legs, the gene model
  creeper_vines_vines.
- Thornwrap has two thorny vines that wrap the front of the ribs (x 20 to
  27, y 21 to 28), over the other marks there (order 1), and the same
  vines on the outer layer of the back.
- Snare: thin, twisted roots that grow down out of the shins and the
  feet into the ground, the gene model snare_roots.
- Mossbed: thick pads of moss on the shoulders, the gene model
  mossbed_moss.
- Vine Swing: vines that trail from the hands, the gene model
  vine_swing_vines.

Pixel plan for the Mosshides.
- Lily Pad: a large lily pad with a small white flower on the top of the
  skull, at the right back, tilted toward the front, the gene model
  lily_pad_pad.
- Ambush Shot has a clump of moss over the left eye of the outer face (x
  12 to 15, y 10 to 13). The right eye stays open.
- Silent Rot has moss wrapped round the feet: leg rows 9 to 11 and the
  bottom of the leg, over the Wader mud (order 1).
- Swamp Shroud: a hood of hanging moss, the gene model swamp_shroud_hood.
- Bog Mist has a gray haze of mist over all of the outer layer, with
  paler wisps in it.
- Barkskin has plates of wet bark on the forearms (arm rows 6 to 8) and on
  the thighs (leg rows 1 to 3).
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
WET = ("#2E2216", "#1E160E", "#5A4632", "#8A7458")
DRY = ("#9A8466", "#7E6A50", "#4E3E2A")


def wader():
    # Dark, wet mud on the legs up to the knees: mud on leg rows 4 to 11 of
    # all the faces and on the bottom of the leg, darkest at the feet, with
    # wet shine spots and a ragged top edge on row 4.
    mud = Mark(WET[0])
    deep = Mark(WET[1])
    wet = Mark(WET[2])
    shine = Mark(WET[3])
    for face in SIDES:
        for col in (0, 1):
            for row in range(4, 12):
                if row == 4 and (col + face // 2) % 2:
                    continue
                n = noise100(face + col, row, 281)
                (shine if n < 8 else wet if n < 25 else deep if row > 8 else mud).put(leg(face, col, row))
    for x in (4, 5):
        for y in (16, 17):
            deep.put((x, y))
    return [mud, deep, wet, shine]


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
    # Large webbed hands of rotten, green skin: the skin covers the hands
    # and the wrists (arm rows 7 to 10), with dark webs between the fingers
    # and bright, slimy spots. The finger tips (row 11) stay dark. The skin
    # goes over the Barkskin bark on the wrists (order 1).
    skin = Mark("#5E8A3A", order=1)
    web = Mark("#2E4A1E", order=1)
    slime = Mark("#9CD060", order=1)
    for face in SIDES:
        for col in (0, 1):
            for row in (7, 8, 9, 10):
                if row == 10 and col == face // 2 % 2:
                    web.put(arm(face, col, row))
                elif (row + col + face) % 5 == 0:
                    slime.put(arm(face, col, row))
                else:
                    skin.put(arm(face, col, row))
    return [skin, web, slime]


def swamp_gas():
    # Large, bright green bubbles of gas over the pelvis: round bubbles of 2
    # by 2 pixels, with a white shine and a dark rim under them, on the
    # pelvis (y 28 to 31) and on the lower edge of the outer layer of the
    # body over it (y 24 to 30).
    marks = []
    for target in (None, "outer"):
        bubble = Mark("#9CF060", target=target)
        shine = Mark("#FFFFFF", target=target)
        rim = Mark("#3E6A1E", target=target)
        spots = (((17, 29), (21, 29), (25, 28), (29, 29), (33, 29), (37, 28)) if target is None else
                 ((16, 26), (20, 24), (26, 25), (30, 27), (34, 26), (38, 24), (22, 28), (36, 28)))
        for x, y in spots:
            shine.put((x, y))
            bubble.put((x + 1, y)).put((x, y + 1)).put((x + 1, y + 1))
            rim.put((x, y + 2)).put((x + 1, y + 2)) if y + 2 < 32 else None
        marks += [bubble, shine, rim]
    return marks


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
    # Large, bright yellow-green stains of bile on the front of the ribs:
    # blotches on y 23 to 25, and drips that run down the ribs from them to
    # y 29, each drip a different length, with a darker edge.
    bile = Mark("#D8E040")
    dark = Mark("#9AA020")
    for x in range(20, 28):
        for y in (23, 24, 25):
            if noise100(x, y, 291) < 70:
                bile.put((x, y))
        end = 26 + (x * 5 + 1) % 4
        if x % 2 == 0:
            for y in range(26, end + 1):
                (dark if y == end else bile).put((x, y))
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


def blightburst():
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


ROOT_COLORS = ("#6A4A2E", "#4A3220", "#A08060")
VINE = ("#4E7A24", "#36581A", "#7AA838")


def rootshot():
    # The roots are the gene model rootshot_roots.
    return []


def rootshot_model():
    # Roots that coil round the bow arm, the right arm: three rings of root
    # round the forearm, each one tilted, so that they read as one root
    # that coils down the arm, and a loose end that hangs from the wrist.
    # On the right arm bone, the arm is from x -1 to 1 and from z -1 to 1,
    # and row r is at y r - 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def ring(face, x, y):
        if face in ("top", "bottom"):
            return None
        return ROOT_COLORS[0] if (x + y) % 3 else ROOT_COLORS[1]

    # A ring: 2x1x2 at uv (0, 0). The loose end: 1x3x1 at uv (0, 4).
    paint_box(im, 0, 0, 2, 1, 2, ring)
    paint_box(im, 0, 4, 1, 3, 1, lambda face, x, y: ROOT_COLORS[2] if y == 2 else ROOT_COLORS[0])
    parts = []
    for y, tilt in ((4, 20), (6, -20), (8, 20)):
        parts.append({"bone": "right_arm", "uv": [0, 0], "from": [-1, y, -1], "size": [2, 1, 2],
                      "inflate": 0.35, "pivot": [0, y + 0.5, 0], "rotation": [0, 0, tilt]})
    parts.append({"bone": "right_arm", "uv": [0, 4], "from": [-1.3, 9, 0.3], "size": [1, 3, 1]})
    save_model("rootshot_roots", im, parts)


def creeper_vines():
    # The vines are the gene model creeper_vines_vines.
    return []


def creeper_vines_model():
    # Vines that hang down the legs: a sheet of thin vines with leaves on
    # the outer side and on the back of each leg, from the hip to the shin.
    # The vines are thin strips with gaps between them. On a leg bone, the
    # leg is from x -1 to 1 and from z -1 to 1, and from y 0 to 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def vines(face, x, y):
        if face not in ("right", "left", "front", "back"):
            return None
        if (x + y // 3) % 2:
            return None if y % 4 else VINE[2]
        return VINE[1] if y % 3 == 0 else VINE[0]

    # The side sheet: 0x10x2 at uv (0, 0). The back sheet: 2x10x0 at uv (8,
    # 0).
    paint_box(im, 0, 0, 0, 10, 2, vines)
    paint_box(im, 8, 0, 2, 10, 0, vines)
    parts = []
    for bone, side in (("right_leg", -1), ("left_leg", 1)):
        parts.append({"bone": bone, "uv": [0, 0], "from": [side * 1.15, 0, -1], "size": [0, 10, 2]})
        parts.append({"bone": bone, "uv": [8, 0], "from": [-1, 0, 1.15], "size": [2, 10, 0]})
    save_model("creeper_vines_vines", im, parts)


def thornwrap():
    # Two thorny vines that wrap the front of the ribs: dark green vines
    # that run across the ribs at a slant, with pale thorns on them. They go
    # over the other marks on the ribs (order 1). The same vines go on over
    # the outer layer of the back, so that they wrap round the body.
    vine = Mark(VINE[1], order=1)
    thorn = Mark("#D8C890", order=1)
    for x in range(20, 28):
        for y0 in (21, 25):
            y = y0 + (x - 20) // 3
            vine.put((x, y))
            if x % 3 == 1:
                thorn.put((x, y - 1))
    back = Mark(VINE[1], target="outer", order=1)
    back_thorn = Mark("#D8C890", target="outer", order=1)
    for x in range(32, 40):
        for y0 in (21, 25):
            y = y0 + (39 - x) // 3
            back.put((x, y))
            if x % 3 == 1:
                back_thorn.put((x, y - 1))
    return [vine, thorn, back, back_thorn]


def snare():
    # The roots are the gene model snare_roots.
    return []


def snare_model():
    # Roots that grow down out of the shins and the feet into the ground:
    # on each leg, four thin, pale roots, each in two parts that bend, so
    # that they look twisted. A root starts on the shin, runs down close to
    # the leg, and then bends out and goes into the ground, with gaps
    # between the roots. The roots are paler than the dark feet of the
    # bogged, so they read. On a leg bone, the leg is from x -1 to 1 and
    # from z -1 to 1, and the ground is at y 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    pale = ("#9A7A50", "#6E5232", "#C8A878")

    def upper(face, x, y):
        return pale[2] if y == 0 else pale[0] if (x + y) % 3 else pale[1]

    def lower(face, x, y):
        return pale[1] if y == 3 else pale[0]

    # An upper part: 1x4x1 at uv (0, 0). A lower part: 1x4x1 at uv (8, 0).
    paint_box(im, 0, 0, 1, 4, 1, upper)
    paint_box(im, 8, 0, 1, 4, 1, lower)
    parts = []
    # Each root: where it starts on the shin (x, y, z), and how far its
    # upper and its lower part lean out, in degrees, round the z axis (to
    # the side) or the x axis (to the front or the back).
    roots = (((-1.4, 6, -0.5), (0, 0, -10), (0, 0, -50)),
             ((-0.5, 6.5, -1.4), (10, 0, 0), (50, 0, 0)),
             ((-0.5, 7, 0.4), (-10, 0, 0), (-45, 0, 0)),
             ((0.4, 7.5, -0.5), (0, 0, 10), (0, 0, 55)))
    for bone in ("right_leg", "left_leg"):
        for (x, y, z), lean, bend in roots:
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, y, z], "size": [1, 4, 1],
                          "pivot": [x + 0.5, y, z + 0.5], "rotation": list(lean)})
            parts.append({"bone": bone, "uv": [8, 0], "from": [x, y + 3.6, z], "size": [1, 4, 1],
                          "pivot": [x + 0.5, y + 3.6, z + 0.5], "rotation": list(bend)})
    save_model("snare_roots", im, parts)


MOSS = ("#6E8A2E", "#55702A", "#3E5420", "#8EA848")


def mossbed():
    # The moss is the gene model mossbed_moss.
    return []


def mossbed_model():
    # Thick pads of moss on the shoulders: a wide, soft pad on the top of
    # each arm, on the leaves of the outer layer, and a smaller lump on top
    # of it. The pads keep clear of the head (x -4 to 4). On an arm bone,
    # the outer layer of the top is at y -2.2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def pad(face, x, y):
        if face == "top":
            return MOSS[3] if (x * 3 + y) % 5 == 0 else MOSS[0]
        if face == "bottom":
            return MOSS[2]
        return MOSS[1] if y == 0 else MOSS[2] if x % 2 else MOSS[1]

    # The pad: 4x2x5 at uv (0, 0). The lump: 2x1x3 at uv (0, 8).
    paint_box(im, 0, 0, 4, 2, 5, pad)
    paint_box(im, 0, 8, 2, 1, 3, lambda face, x, y: MOSS[3] if face == "top" else MOSS[1])
    parts = [
        {"bone": "right_arm", "uv": [0, 0], "from": [-3.5, -4.2, -2.5], "size": [4, 2, 5]},
        {"bone": "right_arm", "uv": [0, 8], "from": [-2.5, -5.2, -1.5], "size": [2, 1, 3]},
        {"bone": "left_arm", "uv": [0, 0], "from": [-0.5, -4.2, -2.5], "size": [4, 2, 5], "mirror": True},
        {"bone": "left_arm", "uv": [0, 8], "from": [0.5, -5.2, -1.5], "size": [2, 1, 3], "mirror": True},
    ]
    save_model("mossbed_moss", im, parts)


def vine_swing():
    # The vines are the gene model vine_swing_vines.
    return []


def vine_swing_model():
    # Vines that trail from the hands: from the bottom of each hand, two
    # thin vines with small leaves hang down, one at the front and one at
    # the back, of different lengths. On an arm bone, the hand ends at y
    # 10.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def vine(face, x, y):
        if face not in ("right", "left"):
            return None
        if x == 1:
            return VINE[2] if y % 2 else None
        return VINE[0] if y % 3 else VINE[1]

    # A long vine: 0x6x2 at uv (0, 0). A short vine: 0x4x2 at uv (8, 0).
    paint_box(im, 0, 0, 0, 6, 2, vine)
    paint_box(im, 8, 0, 0, 4, 2, vine)
    parts = []
    for bone in ("right_arm", "left_arm"):
        parts.append({"bone": bone, "uv": [0, 0], "from": [0, 9.5, -1.2], "size": [0, 6, 2]})
        parts.append({"bone": bone, "uv": [8, 0], "from": [0, 9.5, 0.2], "size": [0, 4, 2]})
    save_model("vine_swing_vines", im, parts)


LILY = ("#4E8A2E", "#6EAA40", "#36621E")


def lily_pad():
    # The pad is the gene model lily_pad_pad.
    return []


def lily_pad_model():
    # A large lily pad on the top of the skull, at the right back, where the
    # mushrooms of the bogged leave room: a flat, round green leaf with
    # pale veins, a dark rim, and the notch of a lily pad. Its back edge is
    # raised, so it tilts toward the front and shows its top from the front.
    # A small white flower with a pink middle sits on it. The front edge
    # lies on the outer layer of the top of the head (y -8.2).
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def pad(face, x, y):
        if face not in ("top", "bottom"):
            return None
        corner = (x in (0, 6)) and (y in (0, 6)) or (x, y) in ((1, 0), (0, 1), (5, 0), (6, 1))
        notch = x == 3 and y < 3
        if corner or notch:
            return None
        if x in (0, 6) or y in (0, 6) or (x, y) in ((1, 1), (5, 1)):
            return LILY[2]
        return LILY[1] if x == y or x + y == 6 else LILY[0]

    def flower(face, x, y):
        if face == "top":
            return "#F4A8C8" if (x, y) == (1, 1) else "#FAF6F2"
        return "#FAF6F2" if face != "bottom" else "#D8D0D0"

    # The pad: 7x0x7 at uv (0, 0). The flower: 3x1x3 at uv (16, 0).
    paint_box(im, 0, 0, 7, 0, 7, pad)
    paint_box(im, 16, 0, 3, 1, 3, flower)
    tilt = {"pivot": [-2.5, -8.3, -1], "rotation": [20, 0, 0]}
    parts = [
        dict({"bone": "head", "uv": [0, 0], "from": [-6, -8.3, -1], "size": [7, 0, 7]}, **tilt),
        dict({"bone": "head", "uv": [16, 0], "from": [-4, -9.3, 2.5], "size": [3, 1, 3]}, **tilt),
    ]
    save_model("lily_pad_pad", im, parts)


def ambush_shot():
    # A clump of moss over the left eye of the outer face (x 12 to 15, y 10
    # to 13), thick and dark, with bright tufts, so that the face has one
    # open eye.
    moss = Mark(MOSS[2], target="outer")
    tuft = Mark(MOSS[3], target="outer")
    for x in range(12, 16):
        for y in range(10, 14):
            if (x, y) in ((12, 10), (15, 13)):
                continue
            (tuft if (x + y) % 3 == 0 else moss).put((x, y))
    return [moss, tuft]


def silent_rot():
    # Moss wrapped round the feet, so that they make no sound: soft moss on
    # leg rows 9 to 11 of all the faces and on the bottom of the leg, with
    # bright tufts. It goes over the Wader mud (order 1).
    moss = Mark(MOSS[0], order=1)
    tuft = Mark(MOSS[3], order=1)
    dark = Mark(MOSS[2], order=1)
    for face in SIDES:
        for col in (0, 1):
            dark.put(leg(face, col, 9)) if (col + face // 2) % 2 else tuft.put(leg(face, col, 9))
            moss.put(leg(face, col, 10))
            (tuft if col == face // 2 % 2 else moss).put(leg(face, col, 11))
    for x in (4, 5):
        for y in (16, 17):
            dark.put((x, y))
    return [moss, tuft, dark]


def swamp_shroud():
    # The hood is the gene model swamp_shroud_hood.
    return []


def swamp_shroud_model():
    # A hood of hanging moss: long strands of moss that hang from the edge
    # of the top of the head, down the sides to the jaw and down the back to
    # the shoulders, each strand a different length. A short fringe hangs
    # over the brow. The top stays open, so the mushrooms of the bogged grow
    # out through it, and the back strands leave a gap round the brown
    # mushroom on the back of the head. On the head bone, the outer layer
    # is 0.2 pixel out of the head (x -4 to 4, y -8 to 0, z -4 to 4).
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def hood(face, x, y):
        if face in ("top", "bottom"):
            return None
        if face == "front":
            if y > 1 or y == 1 and x % 2:
                return None
            return MOSS[1]
        end = (7 if face != "back" else 11) - (x * 5 + 2) % 4
        if face == "back" and 4 <= x <= 8 and 5 <= y <= 8:
            return None
        if y > end:
            return None
        return MOSS[3] if y == end else MOSS[2] if x % 3 == 0 else MOSS[0] if y % 2 else MOSS[1]

    # The hood: 9x12x9 at uv (0, 0).
    paint_box(im, 0, 0, 9, 12, 9, hood)
    parts = [{"bone": "head", "uv": [0, 0], "from": [-4.5, -8.2, -4.5], "size": [9, 12, 9]}]
    save_model("swamp_shroud_hood", im, parts)


def bog_mist():
    # A gray haze of mist on all of the outer layer: a pale gray film over
    # the moss and the vines, with paler wisps that drift across it.
    haze = Mark("#C8CCC8", alpha=120, target="outer")
    wisp = Mark("#E8ECE8", alpha=170, target="outer")
    for x in range(64):
        for y in range(32):
            ((wisp if (x + 2 * y) % 9 < 2 else haze)).put((x, y))
    return [haze, wisp]


BARK = ("#5A4430", "#3E2E20", "#7A6044", "#8A7458")


def barkskin():
    # Plates of wet bark on the forearms (arm rows 6 to 8) and on the
    # thighs (leg rows 1 to 3): brown plates with dark grooves between
    # them, and a wet shine on the top edge of each plate.
    bark = Mark(BARK[0])
    groove = Mark(BARK[1])
    shine = Mark(BARK[3])
    for at, rows in ((arm, (6, 7, 8)), (leg, (1, 2, 3))):
        for face in SIDES:
            for col in (0, 1):
                for row in rows:
                    if col == (face // 2 + row) % 2 and row != rows[0]:
                        groove.put(at(face, col, row))
                    elif row == rows[0]:
                        shine.put(at(face, col, row))
                    else:
                        bark.put(at(face, col, row))
    return [bark, groove, shine]

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
    "blightburst": blightburst,
    "rootshot": rootshot,
    "creeper_vines": creeper_vines,
    "thornwrap": thornwrap,
    "snare": snare,
    "mossbed": mossbed,
    "vine_swing": vine_swing,
    "lily_pad": lily_pad,
    "ambush_shot": ambush_shot,
    "silent_rot": silent_rot,
    "swamp_shroud": swamp_shroud,
    "bog_mist": bog_mist,
    "barkskin": barkskin,
}

MODELS = {
    "puffball": "originofspawns:puffball_cap",
    "shelfback": "originofspawns:shelfback_shelves",
    "mushroom_shot": "originofspawns:mushroom_shot_caps",
    "leechbone": "originofspawns:leechbone_leeches",
    "venom_gland": "originofspawns:venom_gland_sac",
    "antivenom": "originofspawns:antivenom_eyes",
    "rootshot": "originofspawns:rootshot_roots",
    "creeper_vines": "originofspawns:creeper_vines_vines",
    "snare": "originofspawns:snare_roots",
    "mossbed": "originofspawns:mossbed_moss",
    "vine_swing": "originofspawns:vine_swing_vines",
    "lily_pad": "originofspawns:lily_pad_pad",
    "swamp_shroud": "originofspawns:swamp_shroud_hood",
}

MODEL_MAKERS = [puffball_model, shelfback_model, mushroom_shot_model, leechbone_model, venom_gland_model,
                antivenom_model, rootshot_model, creeper_vines_model, snare_model, mossbed_model,
                vine_swing_model, lily_pad_model, swamp_shroud_model]


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
