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

Pixel plan for the Ashborn.
- Cinderblade has charred hands (arm rows 9 to 11) with cracks of ember,
  and the gene model cinderblade_embers makes the cracks glow.
- Pyre: coals that glow in the rib cage, the gene model pyre_coals.
- Lavawalker has a crust of dried lava on the shins: leg rows 6 to 11.
- Cauterize has orange seams of heat: the front of the legs (column 1,
  rows 1 to 10), the inner side of the arms (column 0, rows 1 to 10), the
  ribs, and the top of the skull.
- Smoke Screen has gray smoke on the shoulders, and wisps of it that rise
  up the sides and the back of the head (y 10 to 15).
- Ember Eyes has orange flames in the eye sockets, and the gene model
  ember_eyes_flames makes them glow.

Pixel plan for the Witherlords.
- Deep Wither has black veins with pale violet edges that spread from the
  joints: down from the shoulders and the hips (rows 0 to 3), and out from
  the elbows and the knees (rows 4 to 7), on the outer, the front, and the
  back faces.
- Wither Aura has black dust with pale ash edges that falls from the ribs:
  under the front ribs, on the spine, on the pelvis, and down the thighs.
- Decay Touch has black, rotten fingers: arm rows 10 and 11 and the bottom
  of the arm are black, with a pale, sick edge on row 9.
- Skull Shot: a small wither skull on the left shoulder, the gene model
  skull_shot_skull.
- Black Rot has wide blotches of black rot with pale rims in the ribs of
  the front and the back (y 22 to 24).
- Grave Chill has frost in the eye sockets: pale blue sockets with white
  frost round them (y 11 and 13).

Pixel plan for the Ramparts.
- Brickplate: a plate of nether brick on the chest, the gene model
  brickplate_plate.
- Holdfast: wide feet of blackstone, the gene model holdfast_feet.
- Sentinel: a tall crest of bone on the skull, the gene model
  sentinel_crest.
- Brickshield: a nether brick shield on the left arm, the gene model
  brickshield_shield.
- Garrison has a red sash with a gold edge across the body, from the right
  shoulder to the left hip, on the front and on the back.
- Smite Ward has gold runes on the brow: face rows 8 to 10, but x 12.

Pixel plan for the Soul Reapers.
- Soulstride: blue soul fire round the feet and the shins, the gene model
  soulstride_flames, which glows.
- Soul Link: a blue soul flame in the rib cage, the gene model
  soul_link_flame, which glows.
- Vengeful Spirit: a small, pale blue ghost that curls up from the back
  and over the right shoulder, the gene model vengeful_spirit_ghost, which
  glows.
- Soul Hunger has soul blue light in the eye sockets (y 12) and in the
  gaps of the teeth (y 14), and thin blue cracks from the eyes across the
  brow and over the top of the skull.
- Ghost Step has faded legs: a pale blue-gray on leg rows 3 to 11, and an
  erase in a dither on them, so that they are half see-through.
- Soul Drain has blue light between the fingers: arm rows 10 and 11, and a
  dim blue on row 9.
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



EMBER = ("#FF8A1E", "#FFD060", "#C83A10", "#3A1A10")


def crack(face, x, y):
    """Return True for a pixel of a crack of ember on a face of a hand."""
    return (x + 2 * y + len(face)) % 4 == 0


def cinderblade():
    # Charred hands with cracks of ember: the hands (arm rows 9 to 11) are a
    # dark, burnt red-brown, and the cracks in them are orange. The gene
    # model cinderblade_embers puts the same cracks over them, and they
    # glow.
    char = Mark(EMBER[3])
    hot = Mark(EMBER[0])
    for face in SIDES:
        for col in (0, 1):
            for row in (9, 10, 11):
                (hot if (col + 2 * row + face) % 4 == 0 else char).put(arm(face, col, row))
    return [char, hot]


def cinderblade_model():
    # A thin shell round each hand (arm rows 9 to 11) that has only the
    # cracks of ember on it, so that the cracks glow in the dark. On an arm
    # bone, row r is at y r - 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def shell(face, x, y):
        if not crack(face, x, y):
            return None
        return EMBER[1] if (x + y) % 3 == 0 else EMBER[0]

    # The shell: 2x3x2 at uv (0, 0).
    paint_box(im, 0, 0, 2, 3, 2, shell)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-1, 7, -1], "size": [2, 3, 2], "inflate": 0.05, "glow": True}
             for bone in ("right_arm", "left_arm")]
    save_model("cinderblade_embers", im, parts)


def pyre():
    # The coals are the gene model pyre_coals.
    return []


def pyre_model():
    # Coals that glow in the rib cage: three lumps of coal inside the body,
    # red at the edges and orange to yellow in the middle. They show through
    # the gaps between the ribs, in the front and in the back. On the body
    # bone, the body is from x -4 to 4, from y 0 to 12, and from z -2 to 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def coal(face, x, y):
        return EMBER[1] if (x + y) % 3 == 0 else EMBER[0] if (x + y) % 3 == 1 else EMBER[2]

    # A large coal: 2x2x2 at uv (0, 0). A flat coal: 3x1x2 at uv (8, 0).
    paint_box(im, 0, 0, 2, 2, 2, coal)
    paint_box(im, 8, 0, 3, 1, 2, coal)
    parts = [
        {"bone": "body", "uv": [0, 0], "from": [-2.5, 4, -1], "size": [2, 2, 2], "glow": True},
        {"bone": "body", "uv": [0, 0], "from": [0.5, 5.5, -0.5], "size": [2, 2, 2], "glow": True},
        {"bone": "body", "uv": [8, 0], "from": [-1.5, 7.5, -1], "size": [3, 1, 2], "glow": True},
    ]
    save_model("pyre_coals", im, parts)


def lavawalker():
    # A crust of dried lava on the shins (leg rows 6 to 11): a gray crust of
    # cooled rock, paler than the bone, with dull red cracks and a few
    # orange spots that are still hot.
    crust = Mark("#5A5450")
    pale = Mark("#7A726C")
    red = Mark("#8A2A10")
    hot = Mark("#D0561C")
    for face in SIDES:
        for col in (0, 1):
            for row in range(6, 12):
                n = noise100(face + col, row, 301)
                (hot if n < 8 else red if n < 30 else pale if n < 50 else crust).put(leg(face, col, row))
    for x in (4, 5):
        for y in (16, 17):
            crust.put((x, y))
    return [crust, pale, red, hot]


def cauterize():
    # Orange seams of heat along the bones, as if the bones were welded:
    # a seam down the front of each leg (column 1, rows 1 to 10), down the
    # inner side of each arm (column 0, rows 1 to 10), across the ribs, and
    # over the top of the skull, with a yellow core in a few places.
    seam = Mark("#E0701E")
    core = Mark("#F8B048")
    for row in range(1, 11):
        (core if row % 4 == 0 else seam).put(leg(FRONT, 1, row))
        (core if row % 4 == 2 else seam).put(arm(INNER, 0, row))
    for x in (20, 22, 25, 27, 30, 33, 38):
        seam.put((x, 22))
    for x in (21, 24, 26, 29, 34, 37):
        seam.put((x, 24))
    for y in range(0, 8):
        (core if y % 3 == 1 else seam).put((12, y))
    for y in range(8, 11):
        seam.put((12, y))
    return [seam, core]


def smoke_screen():
    # Smoke that rises from the shoulders: gray smoke on the tops of the
    # shoulders and the arms, and wisps of it that rise up the sides and the
    # back of the head, thinner as they rise (y 15 to 10).
    smoke = Mark("#8A8A86", alpha=220)
    light = Mark("#BEBEB8", alpha=200)
    for x in list(range(20, 28)) + [42, 43]:
        for y in (16, 17, 18, 19):
            if (x, y) != (42, 18) and not (x in (42, 43) and y > 17):
                smoke.put((x, y))
    for face in SIDES:
        for col in (0, 1):
            light.put(arm(face, col, 0))
    for x in list(range(0, 8)) + list(range(16, 32)):
        for y in range(10, 16):
            if (x + y // 2) % 3 == 0 and (y > 12 or x % 2):
                (light if y < 13 else smoke).put((x, y))
    return [smoke, light]


EYE_FLAME = ("#FF8A1E", "#FFE090", "#C83A10")
# The pixels of the ember flames on the face, and their colors: a bright
# core in each socket, a flame that rises from the inner side, and a dark
# red tip.
EYE_PIXELS = (((9, 12), 0), ((10, 12), 1), ((10, 11), 0), ((10, 10), 2),
              ((14, 12), 0), ((13, 12), 1), ((13, 11), 0), ((13, 10), 2))


def ember_eyes():
    # The flames are on the texture, so they show in the day. The gene
    # model ember_eyes_flames makes them glow.
    marks = [Mark(color) for color in EYE_FLAME]
    for p, color in EYE_PIXELS:
        marks[color].put(p)
    return marks


def ember_eyes_model():
    # A square that glows on each flame pixel, just in front of the face (z
    # -4). Face pixel (x, y) is at x - 12, y - 16.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    for n, color in enumerate(EYE_FLAME):
        paint_box(im, 4 * n, 0, 1, 1, 0, lambda face, x, y, c=color: c)
    parts = [{"bone": "head", "uv": [4 * color, 0], "from": [x - 12, y - 16, -4.05], "size": [1, 1, 0],
              "glow": True} for (x, y), color in EYE_PIXELS]
    save_model("ember_eyes_flames", im, parts)


ROT = ("#050505", "#9A9080", "#6A645A")


def deep_wither():
    # Black veins that spread from the joints: lines of deep black that
    # run down from the shoulders and the hips (rows 0 to 3) and out from
    # the elbows and the knees (rows 4 to 7), on the outer, the front, and
    # the back faces. Each vein has a pale violet edge beside it, the color
    # of the wither, so that it reads on the black bone.
    vein = Mark(ROT[0])
    edge = Mark("#8A7A96")
    for at in (arm, leg):
        for face in (OUTER, FRONT, BACK):
            for row, col in ((0, 0), (1, 0), (2, 1), (3, 1), (5, 0), (6, 1), (7, 1)):
                vein.put(at(face, col, row))
                edge.put(at(face, 1 - col, row))
    return [vein, edge]


def wither_aura():
    # Black dust that falls from the ribs: specks of black dust, each with a
    # pale ash edge under it, under the ribs of the front (y 25 and 26), on
    # the spine (x 35 and 36, y 25 to 29), on the pelvis (y 30 and 31), and
    # down the thighs (leg rows 0 to 5), fewer as they fall.
    dust = Mark(ROT[0])
    ash = Mark("#A8A29A")
    for x, y in ((21, 25), (23, 25), (26, 25), (22, 26), (25, 26), (35, 25), (36, 27), (35, 28),
                 (17, 30), (20, 30), (24, 30), (27, 30), (30, 30), (33, 30), (38, 30)):
        dust.put((x, y))
        ash.put((x, y + 1))
    for face in SIDES:
        for row, col in ((0, 0), (2, 1), (4, 0)):
            c = (col + face // 2) % 2
            dust.put(leg(face, c, row))
            ash.put(leg(face, c, row + 1))
    return [dust, ash]


def decay_touch():
    # Black, rotten fingers: the fingers and the hands (arm rows 10 and 11,
    # and the bottom of the arm) are a dead black, with a pale, sick
    # gray-green edge where the rot ends (row 9), and spots of green rot.
    black = Mark(ROT[0])
    sick = Mark("#8A9078")
    spot = Mark("#4E6038")
    for face in SIDES:
        for col in (0, 1):
            black.put(arm(face, col, 11))
            (spot if col == face // 2 % 2 else black).put(arm(face, col, 10))
            sick.put(arm(face, col, 9))
    for x in (44, 45):
        for y in (16, 17):
            black.put((x, y))
    return [black, sick, spot]


def skull_shot():
    # The skull is the gene model skull_shot_skull.
    return []


def skull_shot_model():
    # A small wither skull on the left shoulder, the shoulder that does not
    # hold the sword: a dark gray skull with a paler rim, two black eye
    # holes, and a black mouth. It sits on the top of the left arm, clear
    # of the head. On the left arm bone, the top of the arm is at y -2, and
    # the head is at x -5.5 and less.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def skull(face, x, y):
        if face == "front":
            if y == 1 and x in (0, 2) or y == 3 and x in (1, 2):
                return "#050505"
            return "#5E5E5E" if y == 0 else "#3A3A3A"
        if face == "top":
            return "#4A4A4A"
        return "#5E5E5E" if y == 0 or x in (0, 3) else "#2E2E2E"

    # The skull: 4x4x4 at uv (0, 0).
    paint_box(im, 0, 0, 4, 4, 4, skull)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [-0.5, -6, -2], "size": [4, 4, 4]}]
    save_model("skull_shot_skull", im, parts)


def black_rot():
    # Black rot in the ribs: wide blotches of deep black on the ribs of the
    # front and the back (y 22 to 24), each with a pale, dead rim on the
    # bone round it, so that the rot reads on the black bone.
    rot = Mark(ROT[0])
    rim = Mark(ROT[1])
    blotches = ((21, 22), (22, 22), (22, 23), (25, 24), (26, 24), (26, 23),
                (33, 22), (34, 22), (37, 24), (38, 24), (38, 22))
    for p in blotches:
        rot.put(p)
    for x, y in blotches:
        for n in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if n not in blotches and 20 <= n[0] < 40 and 22 <= n[1] <= 24:
                rim.put(n)
    return [rot, rim]


def grave_chill():
    # Frost in the eye sockets: the sockets are a pale, icy blue, and white
    # frost grows round them (y 11 and 13), more on the outer side.
    ice = Mark("#A8D8F0")
    frost = Mark("#EEF8FF")
    for outer, inner in ((9, 10), (14, 13)):
        ice.put((outer, 12)).put((inner, 12))
        frost.put((outer, 11)).put((outer, 13)).put((inner, 13), 220)
    frost.put((8, 12)).put((15, 12))
    return [ice, frost]


BRICK = ("#6E2C34", "#56222A", "#24100F", "#8E4048")


def brick(face, x, y):
    """Return the color of a pixel of nether brick: rows of bricks with
    dark mortar between them, each row offset by half a brick."""
    if y % 2 == 1:
        return BRICK[2]
    if (x + (y // 2) * 2) % 4 == 3:
        return BRICK[2]
    return BRICK[3] if (x + y) % 5 == 0 else BRICK[0] if (x // 2 + y) % 2 else BRICK[1]


def brickplate():
    # The plate is the gene model brickplate_plate.
    return []


def brickplate_model():
    # A plate of nether brick on the chest, as a wall of the fortress: a
    # flat block of red nether bricks with dark mortar, on the front of the
    # ribs. On the body bone, the front is at z -2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    # The plate: 6x7x1 at uv (0, 0).
    paint_box(im, 0, 0, 6, 7, 1, lambda face, x, y: brick(face, x, y) if face == "front" else BRICK[1])
    parts = [{"bone": "body", "uv": [0, 0], "from": [-3, 1, -3], "size": [6, 7, 1]}]
    save_model("brickplate_plate", im, parts)


BLACKSTONE = ("#3A3640", "#2A262E", "#5E5A66")


def holdfast():
    # The feet are the gene model holdfast_feet.
    return []


def holdfast_model():
    # Wide feet of blackstone: a heavy block of blackstone round each foot,
    # wider than the leg, with a paler top edge and pale specks, so that it
    # reads against the black bone. On a leg bone, the ground is at y 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def stone(face, x, y):
        if face == "top" or face != "bottom" and y == 0:
            return BLACKSTONE[2]
        return BLACKSTONE[2] if (x * 3 + y * 5) % 7 == 0 else BLACKSTONE[0] if (x + y) % 2 else BLACKSTONE[1]

    # A foot: 4x2x5 at uv (0, 0).
    paint_box(im, 0, 0, 4, 2, 5, stone)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-2, 10, -3], "size": [4, 2, 5]}
             for bone in ("right_leg", "left_leg")]
    save_model("holdfast_feet", im, parts)


def sentinel():
    # The crest is the gene model sentinel_crest.
    return []


def sentinel_model():
    # A tall crest of pale bone on the skull, from the brow to the back of
    # the head, highest in the middle, with dark grooves along it. On the
    # head bone, the top of the head is at y -8.
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

    def crest(face, x, y):
        if face in ("right", "left"):
            return ASH[2] if x % 3 == 2 else ASH[0] if y == 0 else ASH[1]
        return ASH[0] if face == "top" else ASH[1]

    # The low crest: 1x2x8 at uv (0, 0). The high crest: 1x2x4 at uv (0, 12).
    paint_box(im, 0, 0, 1, 2, 8, crest)
    paint_box(im, 0, 12, 1, 2, 4, crest)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-0.5, -10, -4], "size": [1, 2, 8]},
        {"bone": "head", "uv": [0, 12], "from": [-0.5, -12, -2], "size": [1, 2, 4]},
    ]
    save_model("sentinel_crest", im, parts)


def brickshield():
    # The shield is the gene model brickshield_shield.
    return []


def brickshield_model():
    # A shield of nether brick on the left arm, the arm that does not hold
    # the sword: a flat slab of bricks on the outer side of the arm, with a
    # gold boss in its middle. On the left arm bone, the outer side is at x
    # 1, and the arm is from y -2 to 10.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def slab(face, x, y):
        # The outer face and the inner face both show the bricks.
        if face in ("left", "right"):
            if x in (2, 3) and y in (3, 4):
                return "#E8B830"
            return brick(face, x, y)
        return BRICK[1]

    # The slab: 1x8x6 at uv (0, 0).
    paint_box(im, 0, 0, 1, 8, 6, slab)
    parts = [{"bone": "left_arm", "uv": [0, 0], "from": [1.2, 0, -3], "size": [1, 8, 6]}]
    save_model("brickshield_shield", im, parts)


def garrison():
    # A red sash of the fortress across the body: a band of red, 2 pixels
    # wide, with a gold edge, from the right shoulder down to the left hip,
    # on the front and on the back. It shows on the bones of the ribs and
    # the pelvis.
    sash = Mark("#A01818")
    gold = Mark("#D8A030")
    for y in range(20, 32):
        step = (y - 20) * 7 // 11
        for x0, sign in ((20, 1), (39, -1)):
            x = x0 + sign * step
            sash.put((x, y)).put((x + sign, y))
            gold.put((x + 2 * sign, y)) if 16 <= x + 2 * sign < 40 else None
    return [sash, gold]


def smite_ward():
    # Gold runes on the brow: two bright gold glyphs on face rows 8 to 10 (a
    # cross on the right side and a sun on the left side), with a darker
    # gold edge, so that they read as holy gold on the black skull. They
    # keep off the Cauterize seam (x 12).
    gold = Mark("#F0C040")
    dark = Mark("#9A7418")
    for p in ((9, 8), (9, 9), (9, 10), (8, 9), (10, 9)):
        gold.put(p)
    for p in ((14, 8), (13, 9), (15, 9), (14, 10)):
        gold.put(p)
    dark.put((14, 9)).put((11, 9))
    return [gold, dark]


SOUL = ("#50D8F0", "#C8F8FF", "#2A8AB0", "#123A48")


def flame(x, y, w, h, seed):
    """Return True for a pixel of a flame on a face of w by h pixels: each
    column burns up to its own height from the bottom row."""
    top = (0, 3, 1, 2)[(x + seed) % 4] if h > 3 else (x * 5 + seed) % (h - 1)
    return y >= top


def soulstride():
    # The flames are the gene model soulstride_flames.
    return []


def soulstride_model():
    # Blue soul fire round the feet: a shell of flames round each foot and
    # shin (leg rows 7 to 11), each column of flame a different height,
    # white-blue at the bottom and blue at the tips. It glows. On a leg
    # bone, the ground is at y 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def fire(face, x, y):
        if face in ("top", "bottom"):
            return None
        if not flame(x, y, 4, 5, len(face)):
            return None
        top = (0, 3, 1, 2)[(x + len(face)) % 4]
        return SOUL[1] if y == 4 else SOUL[2] if y == top else SOUL[0]

    # The shell: 4x5x4 at uv (0, 0).
    paint_box(im, 0, 0, 4, 5, 4, fire)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-2, 7, -2], "size": [4, 5, 4], "glow": True}
             for bone in ("right_leg", "left_leg")]
    save_model("soulstride_flames", im, parts)


def soul_link():
    # The flame is the gene model soul_link_flame.
    return []


def soul_link_model():
    # A blue soul flame in the rib cage: two crossed planes of flame inside
    # the body, that rise from the lower ribs, white-blue in the core and
    # blue at the edges and the tip. It glows, and it shows through the
    # gaps between the ribs. On the body bone, the body is from z -2 to 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def plane(face, x, y):
        if face not in ("front", "back"):
            return None
        shape = {0: (1, 2), 1: (1, 2), 2: (0, 3), 3: (0, 3), 4: (0, 3)}
        lo, hi = shape[y]
        if not lo <= x <= hi:
            return None
        return SOUL[1] if 1 <= x <= 2 and y >= 2 else SOUL[0] if y >= 1 else SOUL[2]

    # A plane: 4x5x0 at uv (0, 0).
    paint_box(im, 0, 0, 4, 5, 0, plane)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-2, 3, 0], "size": [4, 5, 0], "glow": True,
              "pivot": [0, 5, 0], "rotation": [0, angle, 0]} for angle in (45, 135)]
    save_model("soul_link_flame", im, parts)


def vengeful_spirit():
    # The spirit is the gene model vengeful_spirit_ghost.
    return []


GHOST = ("#C8F0FF", "#8AD0F0", "#4A9AC8", "#0E2A3A")


def vengeful_spirit_model():
    # A small, pale blue ghost, as a vex, that curls up from the back and
    # over the right shoulder: a tail that starts on the back of the ribs,
    # a body that rises behind the shoulder, and a head with dark eyes that
    # looks forward over the shoulder, with two small arms. All the parts
    # touch, and the tail lies on the back, so nothing floats. It glows.
    # It is over the right shoulder, as the left shoulder has the Skull Shot
    # skull. On the body bone, the back is at z 2, the head is from x -4 to
    # 4 and from y -8 to 0, and the right arm is from x -6 to -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def body(face, x, y):
        return GHOST[0] if face == "top" or y == 0 else GHOST[1] if (x + y) % 3 else GHOST[2]

    def head(face, x, y):
        if face == "front" and y == 1 and x in (0, 2):
            return GHOST[3]
        return GHOST[0] if face in ("top", "front") else GHOST[1]

    # A tail part: 2x3x1 at uv (0, 0). The body: 2x3x2 at uv (8, 0). The
    # head: 3x3x3 at uv (0, 6). An arm: 0x2x1 at uv (16, 0).
    paint_box(im, 0, 0, 2, 3, 1, body)
    paint_box(im, 8, 0, 2, 3, 2, body)
    paint_box(im, 0, 6, 3, 3, 3, head)
    paint_box(im, 16, 0, 0, 2, 1, lambda face, x, y: GHOST[1])
    glow = {"glow": True}
    parts = [
        dict({"bone": "body", "uv": [0, 0], "from": [-2, 4, 2], "size": [2, 3, 1]}, **glow),
        dict({"bone": "body", "uv": [0, 0], "from": [-3.5, 1.5, 2.2], "size": [2, 3, 1]}, **glow),
        dict({"bone": "body", "uv": [8, 0], "from": [-5, -1.5, 1.5], "size": [2, 3, 2]}, **glow),
        dict({"bone": "body", "uv": [0, 6], "from": [-7, -4.5, -0.5], "size": [3, 3, 3]}, **glow),
        dict({"bone": "body", "uv": [16, 0], "from": [-7.1, -1.5, -0.2], "size": [0, 2, 1]}, **glow),
        dict({"bone": "body", "uv": [16, 0], "from": [-3.9, -1.5, -0.2], "size": [0, 2, 1]}, **glow),
    ]
    save_model("vengeful_spirit_ghost", im, parts)


def soul_hunger():
    # A hunger for souls that burns inside the skull: bright soul blue light
    # fills the eye sockets (y 12), and a dimmer blue shines through the
    # gaps of the teeth (y 14). Thin blue cracks run from the eyes up across
    # the brow and over the top of the skull, as if the light breaks out.
    # The cracks keep off the Cauterize seam (x 12).
    light = Mark(SOUL[0])
    core = Mark(SOUL[1])
    crack = Mark(SOUL[2])
    for x in (9, 10, 13, 14):
        (core if x in (10, 13) else light).put((x, 12))
    for x in (10, 12):
        crack.put((x, 14))
    light.put((11, 14))
    for p in ((10, 11), (11, 10), (11, 9), (10, 8), (13, 11), (13, 10), (14, 9), (14, 8),
              (10, 7), (9, 6), (11, 5), (14, 7), (14, 6), (13, 4)):
        crack.put(p)
    return [light, core, crack]


def ghost_step():
    # Faded legs, as if the legs were half a ghost: a pale blue-gray over
    # leg rows 3 to 11, and an erase in a dither on them, so that they are
    # half see-through. The erase goes after the other marks (order 1).
    fade = Mark("#7A96A4", alpha=220)
    gone = []
    for face in SIDES:
        for col in (0, 1):
            for row in range(3, 12):
                if (col + row + face // 2) % 2 == 0 and row > 4:
                    gone.append(leg(face, col, row))
                else:
                    fade.put(leg(face, col, row))
    return [fade, erase("ghost_step", "legs", gone, order=1)]


def soul_drain():
    # Blue light between the fingers: on the hands (arm rows 10 and 11),
    # every other column is a bright soul blue between the dark fingers,
    # and a dim blue glow is on row 9.
    light = Mark(SOUL[0])
    dim = Mark(SOUL[2])
    for face in SIDES:
        for col in (0, 1):
            if (col + face // 2) % 2 == 0:
                light.put(arm(face, col, 10)).put(arm(face, col, 11))
            dim.put(arm(face, col, 9), 200)
    return [light, dim]

GENES = {
    "duelist": duelist,
    "parry": parry,
    "cleave": cleave,
    "lunge": lunge,
    "bladestorm": bladestorm,
    "executioner": executioner,
    "cinderblade": cinderblade,
    "pyre": pyre,
    "lavawalker": lavawalker,
    "cauterize": cauterize,
    "smoke_screen": smoke_screen,
    "ember_eyes": ember_eyes,
    "deep_wither": deep_wither,
    "wither_aura": wither_aura,
    "decay_touch": decay_touch,
    "skull_shot": skull_shot,
    "black_rot": black_rot,
    "grave_chill": grave_chill,
    "brickplate": brickplate,
    "holdfast": holdfast,
    "sentinel": sentinel,
    "brickshield": brickshield,
    "garrison": garrison,
    "smite_ward": smite_ward,
    "soulstride": soulstride,
    "soul_link": soul_link,
    "vengeful_spirit": vengeful_spirit,
    "soul_hunger": soul_hunger,
    "ghost_step": ghost_step,
    "soul_drain": soul_drain,
}

MODELS = {
    "duelist": "originofspawns:duelist_cloth",
    "parry": "originofspawns:parry_guard",
    "executioner": "originofspawns:executioner_hood",
    "cinderblade": "originofspawns:cinderblade_embers",
    "pyre": "originofspawns:pyre_coals",
    "ember_eyes": "originofspawns:ember_eyes_flames",
    "skull_shot": "originofspawns:skull_shot_skull",
    "brickplate": "originofspawns:brickplate_plate",
    "holdfast": "originofspawns:holdfast_feet",
    "sentinel": "originofspawns:sentinel_crest",
    "brickshield": "originofspawns:brickshield_shield",
    "soulstride": "originofspawns:soulstride_flames",
    "soul_link": "originofspawns:soul_link_flame",
    "vengeful_spirit": "originofspawns:vengeful_spirit_ghost",
}

MODEL_MAKERS = [duelist_model, parry_model, executioner_model, cinderblade_model, pyre_model, ember_eyes_model,
                skull_shot_model, brickplate_model, holdfast_model, sentinel_model, brickshield_model,
                soulstride_model, soul_link_model, vengeful_spirit_model]


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
