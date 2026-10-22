"""Make the gene visuals of the cave spider, from design/species/cave_spider.md.

Usage: python3 tools/make_cave_spider_marks.py (from the root of the repository)

The cave spider has the model and the texture layout of the spider, and it
uses the spider region map. make_spider_marks.py gives the faces of the
parts. The skin of the cave spider is dark teal. The glowing eyes draw over
the marks, so a gene that changes the eyes puts a rim round them.

Pixel plan for the Toxins.
- Neurotoxin has blue tips on the fangs: pale blue fangs with bright blue
  tips, and blue drops under them.
- Hemotoxin has dark red fangs, with a dark red stain at the root of each
  fang.
- Toxic Cloud has green spots on the abdomen: round green spots with a dark
  green rim on the top and the sides.
- Venom Spray has green pores on the body: dark pores with a green rim on
  each face of the body.
- Lingering Venom has green drool from the fangs: green fang tips, green
  drool along the bottom row of the face, and drops under the head.
- Corrosive has yellow fangs, with pits of dull yellow beside them.

Pixel plan for the Gloomers.
- Lightsnuffer has black around the eyes: the pixels next to the eyes are
  black.
- Shade Skin has dark blue patches on the body, and on the front of the
  abdomen next to it.
- Echo Sense has pale, blind eyes: a milky, pale rim round the eyes.
- Night Eyes has green eyes: a bright green rim round the eyes.
- Dread has a dark mist pattern on the back: waves of gray violet mist on
  the top of the abdomen.
- Cave Ghost has faded, pale legs: the legs are pale gray teal, palest at
  the tip.

Pixel plan for the Scurriers.
- Dash has thin blue stripes on the legs: a line of short blue dashes along
  each leg, pale blue at the tip.
- Juke has a zigzag pattern on the body: a pale zigzag line across each face
  of the body.
- Gap Runner has a smaller, flat body: a pale line along the middle of the
  sides of the body and the abdomen, with dark bands above and below it,
  so that the body looks flat. The game makes it smaller with the scale
  attribute.
- Nimble has pale joints: pale bands on the joints of the legs.
- Frenzy Bite has chipped fangs: each fang lacks its tip or its root, and a
  bright chip edge shows where it broke.
- Ceiling Crawler: hooked claws at the tip of each leg, the gene model
  ceiling_crawler_claws.

Pixel plan for the Broodkeepers.
- Brood Call has red hairs on the back: short red hairs with bright tips on
  the top of the abdomen.
- Nest Weaver has a web pattern on the abdomen: a web of pale silk on the
  top of the abdomen, with lines from the middle and two rings.
- Egg Layer: white eggs on the back, the gene model egg_layer_eggs.
- Nest Mend has a pale silk band round the middle of the body.
- Swarm Mind has a pale crown of hairs on the head: pale hairs round the
  edge of the top of the head, with tips on the top row of its sides.
- Spawner Bond has iron gray bands on the legs: three bands of iron round
  each leg, with a bright upper edge.

Pixel plan for the Geodes.
- Amethyst Shell: purple crystals that grow from the top of the abdomen,
  the gene model amethyst_shell_crystals, and purple glints on the top of
  the abdomen round them.
- Shatter: sharp, pale crystal shards along the middle of the back, the
  gene model shatter_shards, and dark cracks with pale edges on the top of
  the abdomen.
- Chime has thin crystal lines on the body: pale violet lines that cross
  each face of the body.
- Ore Skin has flecks of iron ore on the body: tan and pale flecks on the
  body, and a few on the sides of the abdomen.
- Stone Jaws has gray stone fangs, on a jaw of pale stone with dark specks.
- Deepslate Plates has dark deepslate plates on the body: tiles of dark
  gray, with black seams and a pale upper edge.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_spider_marks import ABDOMEN, ALL, BODY, EYES, FANGS, HEAD, leg_pixels, pixels
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/spider.png"


def eye_rim():
    """Return the pixels of the head next to an eye, which are not an eye or
    a fang."""
    out = []
    for x, y, face, col, row in pixels(HEAD, ("right", "front", "left")):
        if (x, y) in EYES or (x, y) in FANGS:
            continue
        if any((x + dx, y + dy) in EYES for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            out.append((x, y))
    return out


def neurotoxin():
    # Blue tips on the fangs: the upper half of each fang is pale blue, the
    # tip is bright blue, and a blue drop hangs under each fang on the bottom
    # of the head.
    pale = Mark("#A8E0FF")
    tip = Mark("#40A0FF")
    pale.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19), (49, 4), (54, 4)])
    return [pale, tip]


def hemotoxin():
    # Dark red fangs: the fangs are dark red, darker at the tip, with a dark
    # red stain at the root of each fang.
    red = Mark("#C82020")
    tip = Mark("#7A0A0A")
    red.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19)])
    red.put([(40, 18), (47, 18)], 170)
    return [red, tip]


def toxic_cloud():
    # Green spots on the abdomen: round spots of 2x2 pixels, bright green,
    # with a dark green rim, on the top and the sides of the abdomen.
    spot = Mark("#7AE050")
    rim = Mark("#3A8A2A")
    spots = [(12 + c, 12 + r) for c, r in ((1, 1), (6, 2), (3, 5), (7, 7), (1, 9))]
    spots += [(x, 24 + r) for x, r in ((2, 2), (7, 4), (24, 3), (29, 1), (37, 3))]
    for x, y in spots:
        spot.put([(x, y), (x + 1, y), (x, y + 1), (x + 1, y + 1)])
        rim.put([(x - 1, y), (x - 1, y + 1), (x + 2, y), (x + 2, y + 1), (x, y - 1), (x + 1, y - 1),
                 (x, y + 2), (x + 1, y + 2)])
    return [rim, spot]


def venom_spray():
    # Green pores on the body: on each face of the body, three dark pores,
    # each with a bright green rim.
    pore = Mark("#0A140A")
    rim = Mark("#5CD040")
    for face in ("top", "right", "front", "left", "back"):
        x0, y0, w, h = BODY[face]
        for col, row in ((1, 1), (4, 2), (2, 4)):
            pore.put((x0 + col, y0 + row))
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                c, r = col + dx, row + dy
                if 0 <= c < w and 0 <= r < h and (c, r) not in ((1, 1), (4, 2), (2, 4)):
                    rim.put((x0 + c, y0 + r))
    return [rim, pore]


def lingering_venom():
    # Green drool from the fangs: green fang tips, green drool along the
    # bottom row of the face between and beside the fangs, and drops under
    # the front of the head. The drool keeps to the bottom row, so that it
    # does not look like tears.
    drool = Mark("#5CD040")
    pale = Mark("#B8F0A0")
    drool.put([(41, 19), (46, 19), (42, 19), (43, 19), (44, 19), (45, 19), (40, 19), (47, 19)])
    pale.put([(41, 18), (46, 18)])
    drool.put([(49, 4), (54, 4), (50, 4), (53, 4), (49, 5), (54, 5)])
    pale.put([(51, 5), (52, 6)])
    return [drool, pale]


def corrosive():
    # Yellow fangs: the fangs are yellow, darker at the tip, with pits of
    # dull yellow on the face beside them.
    yellow = Mark("#E8D040")
    tip = Mark("#B89A20")
    pit = Mark("#7A6A20")
    yellow.put([(41, 18), (46, 18)])
    tip.put([(41, 19), (46, 19)])
    pit.put([(40, 19), (47, 19), (42, 17), (45, 17)])
    return [yellow, tip, pit]


def lightsnuffer():
    # Black around the eyes: each pixel of the head next to an eye is black,
    # so that the red eyes glow in a black mask.
    black = Mark("#050505")
    black.put(eye_rim())
    return [black]


def shade_skin():
    # Dark blue patches on the body: patches of deep blue, with paler blue
    # edges, on all the faces of the body, and on the front of the abdomen
    # and the first rows of its top next to the body.
    deep = Mark("#1A2A70")
    edge = Mark("#30449A")
    for x, y, face, col, row in pixels(BODY, ALL):
        n = noise100(x // 2, y // 2, 611)
        if n < 45:
            deep.put((x, y))
        elif n < 60:
            edge.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("front", "top")):
        if face == "top" and row < 9:
            continue
        n = noise100(x // 2, y // 2, 613)
        if n < 40:
            deep.put((x, y))
        elif n < 55:
            edge.put((x, y))
    return [deep, edge]


def echo_sense():
    # Pale, blind eyes: a milky, pale rim round the eyes, as a film over
    # them. The red eyes glow dim in the pale rim.
    milk = Mark("#8A9C9C")
    milk.put(eye_rim())
    return [milk]


def night_eyes():
    # Green eyes: a bright green rim round the eyes, pale green under the
    # middle eyes.
    green = Mark("#40E060")
    pale = Mark("#B0F8B0")
    for x, y in eye_rim():
        (pale if y == 17 else green).put((x, y))
    return [green, pale]


def dread():
    # A dark mist pattern on the back: waves of gray violet mist that run
    # across the top of the abdomen, with a paler crest on each wave.
    mist = Mark("#3A3448")
    crest = Mark("#6A6280")
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        wave = (row + (0, 1, 1, 0, -1, -1, 0, 1, 1, 0)[col]) % 4
        if wave == 0:
            crest.put((x, y))
        elif wave == 1:
            mist.put((x, y))
    return [mist, crest]


def cave_ghost():
    # Faded, pale legs: the legs are pale gray teal, and paler to the tip,
    # as if they fade into the dark.
    root = Mark("#7A9490")
    middle = Mark("#9AB4B0")
    tip = Mark("#C0D4D0")
    for x, y, face, t, row in leg_pixels():
        (tip if t <= 5 else middle if t <= 10 else root).put((x, y))
    return [root, middle, tip]

def dash():
    # Thin blue stripes on the legs: a line of short blue dashes along the
    # top row of the front and the back of each leg, pale blue at the tip.
    blue = Mark("#40A0FF")
    pale = Mark("#A8E0FF")
    for x, y, face, t, row in leg_pixels(("front", "back")):
        if row == 0 and t % 4 != 3:
            (pale if t <= 2 else blue).put((x, y))
    return [blue, pale]


def juke():
    # A zigzag pattern on the body: a pale zigzag line across each face of
    # the body, that goes down from row 1 to row 4 and up again.
    zig = Mark("#C8E8D8")
    for x, y, face, col, row in pixels(BODY, ALL):
        if row == (1, 2, 3, 4, 3, 2)[col]:
            zig.put((x, y))
    return [zig]


def gap_runner():
    # A smaller, flat body: a pale line along the middle of the sides of the
    # body and the abdomen, and dark bands on the top and the bottom rows of
    # the sides, so that the body looks thin and flat.
    pale = Mark("#5A8A80")
    dark = Mark("#061210")
    for part, mid, edges in ((BODY, (3,), (0, 5)), (ABDOMEN, (3, 4), (0, 7))):
        for x, y, face, col, row in pixels(part, ("right", "left", "back")):
            if row in mid:
                pale.put((x, y))
            elif row in edges:
                dark.put((x, y))
    return [pale, dark]


def nimble():
    # Pale joints: on each joint, 5 and 10 pixels from the tip, a pale band,
    # with a softer band on each side of it.
    pale = Mark("#C8E0D8")
    soft = Mark("#7A9A94")
    for x, y, face, t, row in leg_pixels():
        if t in (5, 10):
            pale.put((x, y))
        elif t in (4, 6, 9, 11):
            soft.put((x, y))
    return [pale, soft]


def frenzy_bite():
    # Chipped fangs: the right fang lacks its tip, and the left fang lacks
    # its root, so that both look broken. A bright chip edge shows where each
    # fang broke.
    gone = Mark("#0B1B18")
    chip = Mark("#F0ECE0")
    gone.put([(41, 19), (46, 18)])
    chip.put([(41, 18), (46, 19)])
    return [gone, chip]


def ceiling_crawler():
    # The claws are the gene model ceiling_crawler_claws.
    return []


def ceiling_crawler_model():
    # Hooked claws at the tip of each leg: a short claw that goes on from
    # the tip of the leg, and a hook that bends down from it, pale with a
    # dark point. On a leg bone, the tip of a right leg is at x -15, and the
    # tip of a left leg is at x 15.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def claw(face, x, y):
        return "#D8D0C0" if face in ("top", "front", "left") else "#A89C88"

    def hook(face, x, y):
        return "#3A3028" if y == 1 or face == "bottom" else claw(face, x, y)

    # The claw: 2x1x1 at uv (0, 0). The hook: 1x2x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 1, 1, claw)
    paint_box(im, 8, 0, 1, 2, 1, hook)
    parts = []
    for leg in ("hind_leg", "middle_hind_leg", "middle_front_leg", "front_leg"):
        for side, x, hx, turn in (("right", -16.5, -17, 35), ("left", 14.5, 16, -35)):
            bone = side + "_" + leg
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, -0.5, -0.5], "size": [2, 1, 1],
                          "mirror": side == "left"})
            parts.append({"bone": bone, "uv": [8, 0], "from": [hx, 0, -0.5], "size": [1, 2, 1],
                          "mirror": side == "left", "pivot": [hx + 0.5, 0.2, 0], "rotation": [0, 0, turn]})
    save_model("ceiling_crawler_claws", im, parts)


def brood_call():
    # Red hairs on the back: short red hairs of 2 pixels, with bright red
    # tips, in rows across the top of the abdomen.
    hair = Mark("#A01818")
    tip = Mark("#F04040")
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        if row % 2 == 0 and row < 11 and (col + 3 * (row // 2)) % 4 == 0:
            tip.put((x, y))
            hair.put((x, y + 1))
    return [hair, tip]


def nest_weaver():
    # A web pattern on the abdomen: on the top of the abdomen, pale silk
    # lines that run out from the middle (column 4, row 6) in eight
    # directions, and two square rings of silk round the middle.
    silk = Mark("#D8D8D0")
    shade = Mark("#9AA8A4")
    cx, cy = 4, 6
    for x, y, face, col, row in pixels(ABDOMEN, ("top",)):
        dx, dy = col - cx, row - cy
        ring = max(abs(dx), abs(dy)) in (2, 4)
        spoke = dx == 0 or dy == 0 or abs(dx) == abs(dy)
        if spoke:
            silk.put((x, y))
        elif ring:
            shade.put((x, y))
    return [shade, silk]


def egg_layer():
    # The eggs are the gene model egg_layer_eggs.
    return []


def egg_layer_model():
    # White eggs on the back: a cluster of round, white eggs on the top of
    # the abdomen, some large and some small, with one egg on the others.
    # On the abdomen bone, the top is at y -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def egg(face, x, y):
        if face == "top":
            return "#FFFFF4"
        return "#D8D4C4" if face in ("bottom", "back", "left") else "#F0EEE2"

    # A large egg: 2x2x2 at uv (0, 0). A small egg: 1x1x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 2, 2, egg)
    paint_box(im, 8, 0, 1, 1, 1, egg)
    parts = []
    for x, z in ((-2.5, -1), (0.5, 0), (-1, 2.5)):
        parts.append({"bone": "abdomen", "uv": [0, 0], "from": [x, -5.6, z], "size": [2, 2, 2], "inflate": -0.15})
    for x, y, z in ((2.8, -4.9, 2.5), (-3.6, -4.9, 1.8), (1.6, -4.9, 3.6), (-0.6, -6.6, 0.6)):
        parts.append({"bone": "abdomen", "uv": [8, 0], "from": [x, y, z], "size": [1, 1, 1], "inflate": 0.1})
    save_model("egg_layer_eggs", im, parts)


def nest_mend():
    # A pale silk band round the middle of the body: rows 2 and 3 of the top
    # and the bottom of the body, and columns 2 and 3 of its sides, white
    # with a pale gray edge.
    silk = Mark("#E8E8E0")
    edge = Mark("#A8B4B0")
    for x, y, face, col, row in pixels(BODY, ("top", "bottom", "right", "left")):
        k = row if face in ("top", "bottom") else col
        if k in (2, 3):
            silk.put((x, y))
        elif k in (1, 4):
            edge.put((x, y))
    return [edge, silk]


def swarm_mind():
    # A pale crown of hairs on the head: pale hairs round the edge of the top
    # of the head, with a gap at every third pixel, and pale tips on the top
    # row of the sides and the back of the head.
    hair = Mark("#D0E4DC")
    tip = Mark("#8AA8A0")
    for x, y, face, col, row in pixels(HEAD, ("top",)):
        if (col in (0, 7) or row in (0, 7)) and (col + row) % 3 != 2:
            hair.put((x, y))
    for x, y, face, col, row in pixels(HEAD, ("right", "left", "back")):
        if row == 0 and col % 2 == 0 and (x, y) not in EYES:
            tip.put((x, y))
    return [hair, tip]


def spawner_bond():
    # Iron gray bands on the legs: three bands of iron round each leg, 3 and
    # 4, 8 and 9, and 12 and 13 pixels from the tip, with a bright upper
    # edge on the top row.
    iron = Mark("#8A8E92")
    bright = Mark("#C8CCD0")
    for x, y, face, t, row in leg_pixels():
        if t in (3, 4, 8, 9, 12, 13):
            (bright if row == 0 and face != "top" else iron).put((x, y))
    return [iron, bright]


AMETHYST = ("#9A5AD8", "#D8B0FF", "#5A2A8A")


def amethyst_shell():
    # Purple glints on the top of the abdomen, round the crystals of the
    # gene model amethyst_shell_crystals.
    glint = Mark(AMETHYST[0])
    pale = Mark(AMETHYST[1])
    glint.put([(13, 15), (15, 18), (19, 14), (20, 19), (16, 21), (14, 22)])
    pale.put([(17, 16), (19, 20)])
    return [glint, pale]


def amethyst_shell_model():
    # Purple crystals that grow from the top of the abdomen: five prisms of
    # amethyst, tall and short, that lean in different directions. Each has
    # its root in the abdomen, so that none floats. On the abdomen bone, the
    # top is at y -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def crystal(face, x, y):
        if face == "top" or face in ("front", "left") and x == 0:
            return AMETHYST[1]
        return AMETHYST[2] if face in ("back", "right", "bottom") else AMETHYST[0]

    # A large crystal: 2x4x2 at uv (0, 0). A small crystal: 1x3x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 4, 2, crystal)
    paint_box(im, 8, 0, 1, 3, 1, crystal)
    parts = []
    for x, z, rot in ((-3, -3, [10, 0, 15]), (1.5, 1.5, [-10, 0, -15])):
        parts.append({"bone": "abdomen", "uv": [0, 0], "from": [x, -7.5, z], "size": [2, 4, 2],
                      "pivot": [x + 1, -4, z + 1], "rotation": rot})
    for x, z, rot in ((0, -1, [0, 0, -20]), (-2.5, 2.5, [15, 0, 10]), (3, -2.5, [-15, 0, -10])):
        parts.append({"bone": "abdomen", "uv": [8, 0], "from": [x, -6.5, z], "size": [1, 3, 1],
                      "pivot": [x + 0.5, -4, z + 0.5], "rotation": rot})
    save_model("amethyst_shell_crystals", im, parts)


def shatter():
    # Dark cracks with pale edges on the top of the abdomen, where the
    # shards of the gene model shatter_shards break out.
    crack = Mark("#081010")
    edge = Mark("#A8D8E0")
    path = ((4, 0), (4, 1), (5, 2), (5, 3), (4, 4), (3, 5), (4, 6), (5, 7), (6, 8), (5, 9), (5, 10), (2, 4),
            (1, 3), (7, 6), (8, 5))
    for col, row in path:
        crack.put((12 + col, 12 + row))
    edge.put([(12 + c + 1, 12 + r) for c, r in path if (c + 1, r) not in path and c < 9][::2])
    return [crack, edge]


def shatter_model():
    # Sharp crystal shards along the middle of the back: thin, pale shards
    # of crystal that stick out of the top of the abdomen at sharp angles,
    # two by two, from the rear to the front. Each has its root in the
    # abdomen. On the abdomen bone, the top is at y -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def shard(face, x, y):
        if y == 0:
            return "#F0FCFF"
        return "#C8F0F8" if face in ("front", "left", "top") else "#80C0D0"

    # A shard: 1x4x1 at uv (0, 0).
    paint_box(im, 0, 0, 1, 4, 1, shard)
    parts = []
    for z, lean in ((-4, 1), (-1, -1), (2, 1), (4.5, -1)):
        for side in (-1, 1):
            x = 1 if side > 0 else -2
            parts.append({"bone": "abdomen", "uv": [0, 0], "from": [x, -7.5, z], "size": [1, 4, 1],
                          "pivot": [x + 0.5, -4, z + 0.5], "rotation": [25 * lean, 0, -30 * side]})
    save_model("shatter_shards", im, parts)


def chime():
    # Thin crystal lines on the body: pale violet lines that cross each face
    # of the body on the two diagonals, with a bright point where they meet.
    line = Mark("#B8A0F0")
    bright = Mark("#E8DCFF")
    for x, y, face, col, row in pixels(BODY, ALL):
        if col == row or col + row == 5:
            (bright if col in (2, 3) and row in (2, 3) else line).put((x, y))
    return [line, bright]


def ore_skin():
    # Flecks of iron ore on the body: tan flecks with pale points on all the
    # faces of the body, and a few on the sides of the abdomen.
    tan = Mark("#C8A080")
    pale = Mark("#E8D0B8")
    for x, y, face, col, row in pixels(BODY, ALL):
        n = noise100(x, y, 701)
        if n < 12:
            pale.put((x, y))
        elif n < 32:
            tan.put((x, y))
    for x, y, face, col, row in pixels(ABDOMEN, ("right", "left", "back")):
        n = noise100(x, y, 703)
        if n < 5:
            pale.put((x, y))
        elif n < 14:
            tan.put((x, y))
    return [tan, pale]


def stone_jaws():
    # Gray stone fangs: the fangs and the whole bottom row of the face are
    # pale gray stone, with dark specks, and the fang tips are dark stone.
    # The jaw of stone also shows on the front row under the head.
    stone = Mark("#B4B4B8")
    dark = Mark("#4A4A4E")
    stone.put([(41, 18), (46, 18)] + [(x, 19) for x in range(40, 48) if x not in (41, 43, 46)])
    dark.put([(41, 19), (46, 19), (43, 19), (40, 18), (47, 18)])
    stone.put([(x, 4) for x in range(48, 56) if x % 3])
    dark.put([(x, 4) for x in range(48, 56) if x % 3 == 0])
    return [stone, dark]


def deepslate_plates():
    # Dark deepslate plates on the body: on each face of the body, tiles of
    # 3x3 pixels in dark gray, with black seams between them and a pale
    # upper edge on each tile.
    plate = Mark("#4A4A52")
    seam = Mark("#16161A")
    edge = Mark("#74747E")
    for x, y, face, col, row in pixels(BODY, ALL):
        if col == 3 or row == 3:
            seam.put((x, y))
        elif row in (0, 4):
            edge.put((x, y))
        else:
            plate.put((x, y))
    return [plate, seam, edge]



GENES = {
    "neurotoxin": neurotoxin,
    "hemotoxin": hemotoxin,
    "toxic_cloud": toxic_cloud,
    "venom_spray": venom_spray,
    "lingering_venom": lingering_venom,
    "corrosive": corrosive,
    "lightsnuffer": lightsnuffer,
    "shade_skin": shade_skin,
    "echo_sense": echo_sense,
    "night_eyes": night_eyes,
    "dread": dread,
    "cave_ghost": cave_ghost,
    "dash": dash,
    "juke": juke,
    "gap_runner": gap_runner,
    "nimble": nimble,
    "frenzy_bite": frenzy_bite,
    "ceiling_crawler": ceiling_crawler,
    "brood_call": brood_call,
    "nest_weaver": nest_weaver,
    "egg_layer": egg_layer,
    "nest_mend": nest_mend,
    "swarm_mind": swarm_mind,
    "spawner_bond": spawner_bond,
    "amethyst_shell": amethyst_shell,
    "shatter": shatter,
    "chime": chime,
    "ore_skin": ore_skin,
    "stone_jaws": stone_jaws,
    "deepslate_plates": deepslate_plates,
}

MODELS = {
    "ceiling_crawler": "originofspawns:ceiling_crawler_claws",
    "egg_layer": "originofspawns:egg_layer_eggs",
    "amethyst_shell": "originofspawns:amethyst_shell_crystals",
    "shatter": "originofspawns:shatter_shards",
}

MODEL_MAKERS = [ceiling_crawler_model, egg_layer_model, amethyst_shell_model, shatter_model]


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
    print("pixels that two or more cave spider genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
