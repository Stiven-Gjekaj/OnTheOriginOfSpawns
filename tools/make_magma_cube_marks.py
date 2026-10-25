"""Make the gene visuals of the magma cube, from design/species/magma_cube.md.

Usage: python3 tools/make_magma_cube_marks.py (from the root of the repository)

The magma cube texture is 64x64, and it uses the magma cube region map. The
parts:
- eight thin layers of 8x1x8, cube0 (the top) to cube7 (the bottom). Layers
  0 to 3 are at uv (0, 9i), and layers 4 to 7 at uv (32, 9i - 36). On each
  layer, only one row of each side shows. The top of layer 0 and the bottom
  of layer 7 are the top and the bottom of the cube.
- the core, inside_cube: 4x4x4 at uv (24, 40). The core shows between the
  layers when the cube hops.
The face is the front of layers 1 to 5. The eyes are on the front of layers
2 and 3, in columns 1, 2, 5, and 6. A mouth goes on the front of layers 4
and 5.
The game draws the magma cube with full light, so all of it glows a little
in the dark. The white-hot core of Heat Wave and the mouth of Spit Fire are
bright colors, and need no glow of their own.
The gene models use the bones cube0, cube1, cube7, and inside_cube.

Pixel plan for the Infernos.
- Flame Touch has bright orange cracks: cracks that zigzag down the sides.
- Ember Trail: glowing embers at the bottom, the gene model ember_trail_embers.
- Heat Wave has a white-hot core.
- Spit Fire has a glowing mouth: an orange and yellow mouth.
- Soul Burn has blue soul fire cracks: cracks that zigzag down the sides.
- Magma Blood has a core that drips lava: lava lines down the core.

Pixel plan for the Ironclads.
- Basalt Skin has gray basalt plates on the sides and the top.
- Obsidian Core has a black core with purple flecks.
- Iron Crust has dark iron flecks on the sides and the top.
- Heavy Cube has a thick, dark bottom: the low three layers are dark.
- Crust has a cracked black crust: black sides and top, with orange cracks.
- Shield Slam: a hard brow over the eyes, the gene model shield_slam_brow.

Pixel plan for the Erupters.
- Blast Core has a bright, pulsing core: a yellow core with orange rings.
- Lava Pool has lava at the bottom: the bottom layer and the bottom are lava.
- Ash Cloud has gray ash on the top and on the top layer.
- Split Burst has many small cracks: short, bright cracks on all the sides.
- Scatter Shot has wide eyes: tall, pale eyes with dark centers.
- Molten Core: a large, molten core, the gene model molten_core_core.

Pixel plan for the Lavaborn.
- Lava Swim has smooth, wet layers: an even red with a shine on each face.
- Lava Heal has a deep red core.
- Steam Body: white steam marks on the sides, and puffs of steam on the top,
  the gene model steam_body_puffs.
- Lava Leap has a long, open mouth: a wide, dark mouth with lava in it.
- Fire Drink has a flame inside the core.
- Blaze Heart has a gold, burning heart: a gold core.

Pixel plan for the Springers.
- Sky Jump has bright yellow streaks up the sides, not on the face.
- Far Jump has long, thin cracks across the sides and the top.
- Ground Pound has a heavy, black bottom with cracks above it.
- Quick Jump has narrow, bright eyes: the low row of the eyes is bright, and
  the high row is dark.
- Rebound: a coil inside, round the core, the gene model rebound_coil.
- Iron Pounce has iron bands round layers 0 and 6, with rivets.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/magma_cube.png"

SIDES = ("right", "front", "left", "back")
# The place of each side round the cube, so that a pattern goes round it.
RING = {"front": 0, "left": 8, "back": 16, "right": 24}
FACE_LAYERS = (1, 2, 3, 4, 5)
EYE_LAYERS = (2, 3)
EYE_COLUMNS = (1, 2, 5, 6)


def layer_uv(i):
    return (0, 9 * i) if i < 4 else (32, 9 * i - 36)


def side(i, face, col):
    """Return the texture pixel of column col of a side of layer i."""
    u, v = layer_uv(i)
    x0 = {"right": u, "front": u + 8, "left": u + 16, "back": u + 24}[face]
    return x0 + col, v + 8


def top(col, row):
    """Return the texture pixel of the top of the cube, the top of layer 0."""
    return 8 + col, row


def bottom(col, row):
    """Return the texture pixel of the bottom of the cube, the bottom of layer 7."""
    u, v = layer_uv(7)
    return u + 16 + col, v + row


def sides(faces=SIDES, layers=range(8)):
    """Yield (x, y, face, col, layer) for each side pixel of the layers."""
    for i in layers:
        for face in faces:
            for col in range(8):
                yield (*side(i, face, col), face, col, i)


def is_eye(face, col, i):
    return face == "front" and i in EYE_LAYERS and col in EYE_COLUMNS


def is_face(face, i):
    return face == "front" and i in FACE_LAYERS


CORE = {"top": (28, 40, 4, 4), "bottom": (32, 40, 4, 4), "right": (24, 44, 4, 4), "front": (28, 44, 4, 4),
        "left": (32, 44, 4, 4), "back": (36, 44, 4, 4)}
CORE_SIDES = SIDES
CORE_ALL = ("top", "bottom") + SIDES


def core(faces=CORE_ALL):
    """Yield (x, y, face, col, row) for each pixel of the core."""
    for face in faces:
        x0, y0, w, h = CORE[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def zigzag_cracks(bright, hot, starts, seed):
    # Cracks that zigzag down the sides: on each side, a crack starts at a
    # column and moves one column left or right on each layer. The face stays
    # clear.
    for face, start in starts:
        col = start
        for i in range(8):
            if not is_face(face, i):
                (hot if noise100(col, i, seed) < 35 else bright).put(side(i, face, col))
            col = max(0, min(7, col + (1 if noise100(i, RING[face], seed) < 50 else -1)))


# The Infernos.

def flame_touch():
    bright = Mark("#FF8A1E")
    hot = Mark("#FFD040")
    zigzag_cracks(bright, hot, (("front", 0), ("front", 7), ("right", 3), ("left", 4), ("back", 2), ("back", 6)),
                  3101)
    for col, row in ((2, 1), (3, 2), (3, 3), (4, 4), (5, 5), (5, 6)):
        bright.put(top(col, row))
    return [bright, hot]


def ember_trail():
    # Glowing embers in the bottom layer, and the gene model
    # ember_trail_embers at its edges.
    ember = Mark("#FF8A1E")
    hot = Mark("#FFE070")
    for x, y, face, col, i in sides(layers=(6, 7)):
        n = noise100(x, y, 3121)
        if i == 7 and n < 45 or i == 6 and n < 15:
            (hot if n < 12 else ember).put((x, y))
    return [ember, hot]


def ember_trail_model():
    # Glowing embers at the bottom: small coals that sit at the low edges of
    # the bottom layer, half out of it. On the bone cube7, the layer is from
    # y 23 to 24 and from x and z -4 to 4.
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 1, 1, 1, lambda face, x, y: "#FFE070" if face == "top" else "#FF7A18")
    spots = ((-4.5, -3), (-4.5, 1.5), (3.5, -1.5), (3.5, 2.5), (-2.5, -4.5), (1.5, -4.5), (-1, 3.5), (2, 3.5))
    parts = [{"bone": "cube7", "uv": [0, 0], "from": [x, 23, z], "size": [1, 1, 1], "inflate": -0.1}
             for x, z in spots]
    save_model("ember_trail_embers", im, parts)


def heat_wave():
    # A white-hot core: white at the middle of each face, pale yellow at the
    # edges.
    white = Mark("#FFFBEA")
    edge = Mark("#FFE680")
    for x, y, face, col, row in core():
        (white if 0 < col < 3 and 0 < row < 3 else edge).put((x, y))
    # White-hot lines between the layers: short white lines on layers 1, 3,
    # 5, and 7, two pixels on and two off, round the sides. The face stays
    # clear.
    line = Mark("#FFF4C8")
    for x, y, face, col, i in sides(layers=(1, 3, 5, 7)):
        if not is_face(face, i) and (col + RING[face] + i) % 4 < 2:
            line.put((x, y))
    return [white, edge, line]


def spit_fire():
    # A glowing mouth: an orange mouth on layer 4, columns 2 to 5, with a
    # yellow middle on layer 5, columns 3 and 4.
    orange = Mark("#FF7A18")
    yellow = Mark("#FFE050")
    orange.put([side(4, "front", c) for c in (2, 3, 4, 5)] + [side(5, "front", c) for c in (2, 5)])
    yellow.put([side(5, "front", c) for c in (3, 4)] + [side(4, "front", c) for c in (3, 4)])
    return [orange, yellow]


def soul_burn():
    blue = Mark("#30C8E0")
    pale = Mark("#A8F4FF")
    zigzag_cracks(blue, pale, (("front", 4), ("right", 1), ("right", 6), ("left", 2), ("back", 5)), 3203)
    for col, row in ((5, 1), (4, 2), (4, 3), (3, 4), (2, 5)):
        blue.put(top(col, row))
    return [blue, pale]


def magma_blood():
    # A core that drips lava: lava lines down columns 0 and 2 of each side of
    # the core, bright at the low end, on a dark red core.
    dark = Mark("#5A0C04")
    lava = Mark("#FF5A10")
    drop = Mark("#FFB030")
    for x, y, face, col, row in core():
        if face in CORE_SIDES and col in ((0, 2) if face in ("front", "back") else (1, 3)) and row > 0:
            (drop if row == 3 else lava).put((x, y))
        elif face == "bottom" and (col + row) % 2 == 0:
            drop.put((x, y))
        else:
            dark.put((x, y))
    # Lava drips down the sides: from the top, drips of lava of different
    # lengths go down the sides, with a bright drop at the end of each.
    for face in SIDES:
        for col in (0, 7) if face == "front" else (1, 4, 6):
            length = 3 + noise100(col, RING[face], 3111) % 5
            for i in range(length):
                (drop if i == length - 1 else lava).put(side(i, face, col))
    return [dark, lava, drop]


# The Ironclads.

def basalt_skin():
    # Gray basalt plates: plates of 3 columns and 2 layers, with dark edges,
    # on most of the sides and the top. The face stays clear.
    plate = Mark("#5E5C60")
    edge = Mark("#3A383C")
    for x, y, face, col, i in sides():
        if is_face(face, i):
            continue
        block = (col + RING[face] + (i // 2) * 2) // 3
        if noise100(block, i // 2, 3301) < 70:
            (edge if (col + RING[face] + (i // 2) * 2) % 3 == 2 or i % 2 == 1 else plate).put((x, y))
    for col in range(8):
        for row in range(8):
            if noise100(col // 3, row // 3, 3302) < 70:
                (edge if col % 3 == 2 or row % 3 == 2 else plate).put(top(col, row))
    return [plate, edge]


def obsidian_core():
    black = Mark("#120A1C")
    purple = Mark("#7A30B0")
    for x, y, face, col, row in core():
        (purple if noise100(x, y, 3401) < 25 else black).put((x, y))
    # Black obsidian flecks with purple glints on the sides and the top.
    glint = Mark("#9A48D8")
    for x, y, face, col, i in sides():
        if is_eye(face, col, i):
            continue
        n = noise100(x, y, 3403)
        if n < 22:
            black.put((x, y))
        elif n < 28:
            glint.put((x, y))
    for col in range(8):
        for row in range(8):
            n = noise100(col, row, 3404)
            if n < 22:
                black.put(top(col, row))
            elif n < 28:
                glint.put(top(col, row))
    return [black, purple, glint]


def iron_crust():
    iron = Mark("#5C6066")
    shine = Mark("#A4AAB2")
    for x, y, face, col, i in sides():
        if is_eye(face, col, i):
            continue
        n = noise100(x, y, 3501)
        if n < 16:
            iron.put((x, y))
        elif n < 22:
            shine.put((x, y))
    for col in range(8):
        for row in range(8):
            n = noise100(col, row, 3502)
            if n < 16:
                iron.put(top(col, row))
            elif n < 22:
                shine.put(top(col, row))
    return [iron, shine]


def heavy_cube():
    # A thick, dark bottom: layers 5 to 7 are a dark gray brown, darker at
    # the bottom, and so is the bottom of the cube.
    mid = Mark("#4A3A34")
    low = Mark("#2E2420")
    for x, y, face, col, i in sides(layers=(5, 6, 7)):
        if not is_face(face, i):
            (mid if i == 5 else low).put((x, y))
    low.put([bottom(c, r) for c in range(8) for r in range(8)])
    return [mid, low]


def crust():
    # A cracked black crust: the sides and the top are black, with thin
    # orange cracks between the pieces. The eyes stay clear.
    black = Mark("#120806")
    crack = Mark("#E0501A")
    for x, y, face, col, i in sides():
        if is_eye(face, col, i):
            continue
        ring = col + RING[face]
        (crack if (ring + 3 * (i // 3)) % 5 == 0 or (i == 3 and ring % 3 == 1) else black).put((x, y))
    for col in range(8):
        for row in range(8):
            (crack if col == (row // 2) + 2 or row == 5 else black).put(top(col, row))
    return [black, crack]


def shield_slam():
    # The brow is the gene model shield_slam_brow.
    return []


def shield_slam_model():
    # A hard brow: a ridge of dark basalt over the eyes, on the front of
    # layer 1, with a pale edge on its top. On the bone cube1, the layer is
    # from y 17 to 18, and its front is at z -4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 8, 1, 1, lambda face, x, y: "#8A8890" if face == "top" else "#3E3C42")
    parts = [{"bone": "cube1", "uv": [0, 0], "from": [-4, 17, -5], "size": [8, 1, 1], "inflate": 0.05}]
    save_model("shield_slam_brow", im, parts)


# The Erupters.

def blast_core():
    # A bright, pulsing core: a yellow middle with an orange ring round it.
    yellow = Mark("#FFE840")
    ring = Mark("#FF8A20")
    for x, y, face, col, row in core():
        (yellow if 0 < col < 3 and 0 < row < 3 else ring).put((x, y))
    # Bright yellow rings around the layers: solid rings round layers 2 and
    # 5, and a ring round the edge of the top. The face stays clear.
    band = Mark("#FFE840")
    for x, y, face, col, i in sides(layers=(2, 5)):
        if not is_face(face, i):
            band.put((x, y))
    band.put([top(c, r) for c in range(8) for r in range(8) if c in (0, 7) or r in (0, 7)])
    return [yellow, ring, band]


def lava_pool():
    # Lava at the bottom: the bottom layer and the bottom are bright lava,
    # and lava drips up into layer 6.
    lava = Mark("#FF6A10")
    hot = Mark("#FFB828")
    for x, y, face, col, i in sides(layers=(6, 7)):
        if i == 7:
            (hot if noise100(x, y, 3601) < 35 else lava).put((x, y))
        elif noise100(x, y, 3602) < 30:
            lava.put((x, y))
    for c in range(8):
        for r in range(8):
            (hot if noise100(c, r, 3603) < 35 else lava).put(bottom(c, r))
    return [lava, hot]


def ash_cloud():
    # Gray ash on the top: pale gray ash in patches on the top, and on the
    # top layer.
    ash = Mark("#A8A4A0")
    dark = Mark("#6E6A68")
    for col in range(8):
        for row in range(8):
            n = noise100(col, row, 3701)
            if n < 55:
                ash.put(top(col, row))
            elif n < 75:
                dark.put(top(col, row))
    for x, y, face, col, i in sides(layers=(0,)):
        if noise100(x, y, 3702) < 60:
            ash.put((x, y))
    return [ash, dark]


def split_burst():
    # Many small cracks: short cracks of one or two pixels, all over the
    # sides and the top.
    crack = Mark("#FF9A2A")
    for x, y, face, col, i in sides():
        if not is_face(face, i) and noise100(col + RING[face], i // 2, 3801) < 18:
            crack.put((x, y))
    for col in range(8):
        for row in range(8):
            if noise100(col, row // 2, 3802) < 14:
                crack.put(top(col, row))
    return [crack]


def scatter_shot():
    # Wide eyes: tall, pale eyes on layers 1 to 3, with dark centers on
    # layer 2.
    pale = Mark("#FFF4B8")
    dark = Mark("#2A0800")
    for i in (1, 2, 3):
        for col in EYE_COLUMNS:
            (dark if i == 2 and col in (2, 5) else pale).put(side(i, "front", col))
    return [pale, dark]


def molten_core():
    # The core is the gene model molten_core_core.
    # Orange molten seams: seams that go up the sides in short steps, as
    # between bricks, and across the top. The face stays clear. The core is
    # the gene model molten_core_core.
    seam = Mark("#FF8A20")
    hot = Mark("#FFC040")
    for x, y, face, col, i in sides():
        if is_face(face, i):
            continue
        if (col + RING[face] + 3 * (i // 2)) % 6 == 0:
            (hot if i % 2 else seam).put((x, y))
    seam.put([top(c, 3) for c in range(8)] + [top(5, r) for r in range(8)])
    return [seam, hot]


def molten_core_model():
    # A molten, orange core: a core that is larger than the core of the cube,
    # orange with yellow spots. On the bone inside_cube, the core is from
    # y 18 to 22 and from x and z -2 to 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 6, 6, 6, lambda face, x, y: "#FFD040" if noise100(x, y, len(face)) < 30 else "#FF7A1A")
    parts = [{"bone": "inside_cube", "uv": [0, 0], "from": [-3, 17, -3], "size": [6, 6, 6]}]
    save_model("molten_core_core", im, parts)


# The Lavaborn.

def lava_swim():
    # Smooth, wet layers: an even dark red on the sides and the top, with a
    # shine in columns 1 and 2 of each side.
    red = Mark("#5A0E06")
    shine = Mark("#D86040")
    for x, y, face, col, i in sides():
        if is_eye(face, col, i):
            continue
        (shine if col in (1, 2) and face != "front" else red).put((x, y))
    for col in range(8):
        for row in range(8):
            (shine if (col, row) in ((1, 1), (2, 1), (1, 2)) else red).put(top(col, row))
    return [red, shine]


def lava_heal():
    deep = Mark("#9A0012")
    dark = Mark("#5A0008")
    for x, y, face, col, row in core():
        (dark if col in (0, 3) or row in (0, 3) else deep).put((x, y))
    # Deep red veins on the layers: bright red veins that zigzag down the
    # sides, and across the top.
    vein = Mark("#D01828")
    pale = Mark("#FF4050")
    zigzag_cracks(vein, pale, (("front", 0), ("front", 7), ("right", 2), ("right", 6), ("left", 3), ("back", 1),
                               ("back", 5)), 3901)
    vein.put([top(c, r) for c, r in ((1, 1), (2, 2), (3, 2), (4, 3), (4, 4), (5, 5), (6, 6), (2, 5), (3, 4))])
    return [deep, dark, vein, pale]


def steam_body():
    # White steam marks: pale streaks on the sides, which rise to the top.
    # The puffs are the gene model steam_body_puffs.
    steam = Mark("#E4E4E4")
    for x, y, face, col, i in sides():
        if not is_face(face, i) and (col + RING[face]) % 5 == 1 and i < 5:
            steam.put((x, y))
    return [steam]


def steam_body_model():
    # Puffs of steam on the top: two low puffs that sit on the top of the
    # cube, and a small puff on each. On the bone cube0, the top is at y 16.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 2, 1, 2, lambda face, x, y: "#F4F4F4" if face == "top" else "#D0D0D0")
    paint_box(im, 8, 0, 1, 1, 1, lambda face, x, y: "#FFFFFF")
    parts = [{"bone": "cube0", "uv": [0, 0], "from": [-3, 15, -2], "size": [2, 1, 2]},
             {"bone": "cube0", "uv": [0, 0], "from": [1, 15, 0.5], "size": [2, 1, 2]},
             {"bone": "cube0", "uv": [8, 0], "from": [-2.5, 14, -1.5], "size": [1, 1, 1]},
             {"bone": "cube0", "uv": [8, 0], "from": [1.5, 14, 1], "size": [1, 1, 1]}]
    save_model("steam_body_puffs", im, parts)


def lava_leap():
    # A long, open mouth: a black mouth across layer 5, columns 1 to 6, with
    # a rim of lava on layer 4 and at its ends.
    dark = Mark("#000000")
    lava = Mark("#FF7A20")
    lava.put([side(4, "front", c) for c in range(8)] + [side(5, "front", c) for c in (0, 7)])
    dark.put([side(5, "front", c) for c in range(1, 7)])
    return [dark, lava]


def fire_drink():
    # A flame inside the core: on each side of the core, a flame rises from
    # the bottom, orange at its base and pale yellow at its tip, on a dark
    # core.
    dark = Mark("#300800")
    orange = Mark("#FF6A10")
    yellow = Mark("#FFB028")
    tip = Mark("#FFF0A0")
    flame = {(0, 3): orange, (1, 3): orange, (2, 3): orange, (3, 3): orange, (1, 2): yellow, (2, 2): yellow,
             (1, 1): tip, (2, 1): yellow, (2, 0): tip}
    for x, y, face, col, row in core():
        (flame.get((col, row), dark) if face in CORE_SIDES else orange if face == "bottom" else dark).put((x, y))
    # Small flames above the eyes: on the front of layer 1 over each eye an
    # orange flame, and on layer 0 a yellow tip.
    orange.put([side(1, "front", c) for c in EYE_COLUMNS])
    yellow.put([side(0, "front", c) for c in (1, 2, 5, 6)])
    tip.put([side(0, "front", c) for c in (2, 5)])
    return [dark, orange, yellow, tip]


def blaze_heart():
    # A gold, burning heart: a gold core with a pale middle and deep orange
    # corners.
    gold = Mark("#FFC020")
    pale = Mark("#FFF080")
    deep = Mark("#D06010")
    for x, y, face, col, row in core():
        corner = col in (0, 3) and row in (0, 3)
        (deep if corner else pale if 0 < col < 3 and 0 < row < 3 else gold).put((x, y))
    # Gold flecks on the layers: gold and pale gold flecks on the sides and
    # the top. The eyes stay clear.
    fleck = Mark("#FFC020")
    shine = Mark("#FFF080")
    for x, y, face, col, i in sides():
        if is_eye(face, col, i):
            continue
        n = noise100(x, y, 4101)
        if n < 18:
            fleck.put((x, y))
        elif n < 24:
            shine.put((x, y))
    for col in range(8):
        for row in range(8):
            n = noise100(col, row, 4102)
            if n < 18:
                fleck.put(top(col, row))
            elif n < 24:
                shine.put(top(col, row))
    return [gold, pale, deep, fleck, shine]


# The Springers.

def sky_jump():
    # Bright yellow streaks: streaks up columns 1 and 5 of the right, left,
    # and back sides, and two on the top. The face stays clear.
    streak = Mark("#FFE040")
    for x, y, face, col, i in sides(("right", "left", "back")):
        if col in (1, 5):
            streak.put((x, y))
    streak.put([top(c, r) for c in (1, 6) for r in range(1, 7)])
    return [streak]


def far_jump():
    # Long, thin cracks: on the right, left, and back sides, a crack goes
    # down one column on each layer. A long crack goes across the top.
    crack = Mark("#FF7020")
    for face, start in (("right", 0), ("left", 0), ("back", 7)):
        for i in range(8):
            col = start + i if start == 0 else start - i
            crack.put(side(i, face, col))
    crack.put([top(c, c // 2 + 2) for c in range(8)])
    return [crack]


def ground_pound():
    # A heavy, black bottom: the bottom layer and the bottom are black, and
    # cracks rise from it into layer 6.
    black = Mark("#080404")
    crack = Mark("#C83A10")
    for x, y, face, col, i in sides(layers=(6, 7)):
        if i == 7:
            black.put((x, y))
        elif (col + RING[face]) % 4 == 1:
            crack.put((x, y))
    black.put([bottom(c, r) for c in range(8) for r in range(8)])
    return [black, crack]


def quick_jump():
    # Narrow, bright eyes: the low row of the eyes is white-yellow, and the
    # high row is dark, so the eyes look narrow.
    bright = Mark("#FFFFC8")
    dark = Mark("#200400")
    bright.put([side(3, "front", c) for c in EYE_COLUMNS])
    dark.put([side(2, "front", c) for c in EYE_COLUMNS])
    return [bright, dark]


def rebound():
    # The coil is the gene model rebound_coil.
    # Gray coil bands on the outer layers: bands three pixels wide that turn
    # round the cube as they go down, as a spring. The face stays clear. The
    # coil inside is the gene model rebound_coil.
    band = Mark("#9A9EA6")
    edge = Mark("#C8CCD2")
    for x, y, face, col, i in sides():
        if is_face(face, i):
            continue
        step = (col + RING[face] - i) % 10
        if step < 3:
            (edge if step == 0 else band).put((x, y))
    return [band, edge]


def rebound_model():
    # A coil inside: three thin rings of gray metal round the core, as a
    # spring. On the bone inside_cube, the core is from y 18 to 22 and from x
    # and z -2 to 2, and the layers are from -4 to 4.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    paint_box(im, 0, 0, 6, 1, 6, lambda face, x, y: "#B8BCC4" if face == "top" else "#80848C")
    parts = [{"bone": "inside_cube", "uv": [0, 0], "from": [-3, y, -3], "size": [6, 1, 6], "inflate": -0.3}
             for y in (17.5, 19.5, 21.5)]
    save_model("rebound_coil", im, parts)


def iron_pounce():
    # Iron bands: gray bands round layers 0 and 6, with pale rivets.
    band = Mark("#7E8288")
    rivet = Mark("#D0D4DA")
    for x, y, face, col, i in sides(layers=(0, 6)):
        (rivet if col in (1, 6) else band).put((x, y))
    return [band, rivet]


GENES = {
    "flame_touch": flame_touch,
    "ember_trail": ember_trail,
    "heat_wave": heat_wave,
    "spit_fire": spit_fire,
    "soul_burn": soul_burn,
    "magma_blood": magma_blood,
    "basalt_skin": basalt_skin,
    "obsidian_core": obsidian_core,
    "iron_crust": iron_crust,
    "heavy_cube": heavy_cube,
    "crust": crust,
    "shield_slam": shield_slam,
    "blast_core": blast_core,
    "lava_pool": lava_pool,
    "ash_cloud": ash_cloud,
    "split_burst": split_burst,
    "scatter_shot": scatter_shot,
    "molten_core": molten_core,
    "lava_swim": lava_swim,
    "lava_heal": lava_heal,
    "steam_body": steam_body,
    "lava_leap": lava_leap,
    "fire_drink": fire_drink,
    "blaze_heart": blaze_heart,
    "sky_jump": sky_jump,
    "far_jump": far_jump,
    "ground_pound": ground_pound,
    "quick_jump": quick_jump,
    "rebound": rebound,
    "iron_pounce": iron_pounce,
}

# Each gene with a model: the maker of the model and its name.
MODEL_GENES = {
    "ember_trail": (ember_trail_model, "ember_trail_embers"),
    "shield_slam": (shield_slam_model, "shield_slam_brow"),
    "molten_core": (molten_core_model, "molten_core_core"),
    "steam_body": (steam_body_model, "steam_body_puffs"),
    "rebound": (rebound_model, "rebound_coil"),
}

MODELS = {gene: "originofspawns:" + name for gene, (_, name) in MODEL_GENES.items()}


def main():
    os.makedirs(MARK_DIR, exist_ok=True)
    for make, _ in MODEL_GENES.values():
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
    print("pixels that two or more magma cube genes share:", sum(shared.values()))


if __name__ == "__main__":
    main()
