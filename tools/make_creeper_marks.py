"""Make the marks and the gene models of the creeper genes.

Usage: python3 tools/make_creeper_marks.py

The creeper texture is 64x32, and it has its own region map
(textures/region/creeper.png, from make_creeper_region_map.py). The model
(CreeperModel) has these parts:
- head: 8x8x8 at uv (0, 0), from (-4, -8, -4), on a pivot at y 6.
- body: 8x12x4 at uv (16, 16), from (-4, 0, -2), on a pivot at y 6.
- right_hind_leg, left_hind_leg, right_front_leg, left_front_leg: 4x6x4,
  all at uv (0, 16), from (-2, 0, -2), on pivots at x -2 or 2, y 18, and
  z 4 (hind) or -4 (front). The four legs share one texture.
The face is x 8 to 15, y 8 to 15. Its black pixels are the eyes (x 9 and
10, and x 13 and 14, of y 10 and 11) and the mouth (x 10 to 13, y 12 to
15).

For each gene in GENES, the script writes:
- textures/gene_marks/<gene>_<n>.png, one pattern for each color of the mark.
- gene_visuals/<gene>.json, with one layer for each pattern.
- The gene models in MODELS, with their textures.

The script prints the pixels that two creeper genes share.

Pixel plan for the Blasters.
- Big Bang: a swollen belly with stretched skin on the front of the body,
  the gene model big_bang_belly.
- Shrapnel has shards of iron in the skin of the body: short gray shards
  with dark edges on all the faces of the body.
- Golem Breaker has dark iron-gray spots on the head, but on the eyes and
  the mouth.
- Chain Reaction has bright orange cracks on the front and the back of the
  body.
- Aftershock has cracked, ash-gray feet: leg rows 4 and 5, and the bottom
  of the legs.
- Concussion has wide eyes with pale yellow rings: a ring round each eye,
  over it (y 9), beside it, and under it (y 12, x 9 and 14).

Pixel plan for the Rushers.
- Sprinter has pale stripes down the legs: one column of each side face of
  the legs, rows 0 to 5.
- Short Fuse: a fuse that smokes on the top of the head, the gene model
  short_fuse_fuse.
- Leaper: bent, springy back legs, the gene model leaper_haunches.
- Dodger has wide, flat feet: a dark sole on leg row 5 and on the bottom of
  the legs, with pale toe lines on row 4.
- Tracker has a wide, open mouth: the mouth is 2 pixels wider (x 9 and 14,
  y 13 and 14).
- Wallcrawler: hooked claws on the feet, the gene model wallcrawler_claws.

Pixel plan for the Ambushers.
- Silent Fuse has a mouth that is sewn shut: the mouth is skin, with a dark
  seam across it (y 13) and pale stitches over the seam.
- Leafskin has a pattern of leaves on the skin of the body: dark green leaf
  shapes with a pale vein, on all the side faces of the body.
- Gloom has dark gray patches on the body.
- Veiled has a faded, pale face: a pale wash over the skin of the face, and
  dark gray eyes and mouth in place of black.
- Patient: roots that grow round the feet and climb a little up the legs,
  the gene model patient_roots.
- Surprise has a wider grin: the corners of the mouth go up and out (x 9
  and 14, y 12 and 13).

Pixel plan for the Fumers.
- Toxic Blast has green-yellow blisters on the body: round blisters with a
  pale top and a dark rim, on all the side faces of the body.
- Smoke Bomb has short puffs of gray smoke over the eyes (y 8 and 9) and on
  the front edge of the top of the head.
- Stink has dark green stains on the body, with short drips under them.
- Wither Gas has black drool from the mouth: short drips from the corners of
  the mouth on the bottom of the head, and drops on the top of the chest (y
  20 to 22).
- Gas Leak has holes that leak gas on the back of the body: dark holes with
  puffs of pale green gas round them.
- Spore Blast: small brown mushrooms on the head, the gene model
  spore_blast_mushrooms.

Pixel plan for the Thunderheads.
- Lightning Rod: a copper spike on the top of the head, the gene model
  lightning_rod_spike.
- Static has small blue sparks on the skin of the body: crosses of bright
  blue with a white middle.
- Overcharge has bright blue veins that branch over the front and the back
  of the body.
- Conductor has forked blue marks on the head: short forks of blue on the
  sides, the back, and the top of the head.
- Grounded has copper bands round the legs: leg rows 2 and 3.
- Thunderstruck has a blue glow on the feet: leg rows 4 and 5 and the
  bottom of the legs, brightest at the bottom.
This script needs Pillow.
"""
import json
import os

from PIL import Image

from make_gene_marks import MARK_DIR, VISUAL_DIR, Mark, noise100, write
from make_husk_marks import paint_box, save_model
from make_stray_marks import changed_pixels

REGION_MAP = "originofspawns:textures/region/creeper.png"

# The top left pixel and the size of each face of each part.
HEAD = {"front": (8, 8, 8, 8), "right": (0, 8, 8, 8), "left": (16, 8, 8, 8), "back": (24, 8, 8, 8),
        "top": (8, 0, 8, 8), "bottom": (16, 0, 8, 8)}
BODY = {"front": (20, 20, 8, 12), "back": (32, 20, 8, 12), "right": (16, 20, 4, 12), "left": (28, 20, 4, 12),
        "top": (20, 16, 8, 4), "bottom": (28, 16, 8, 4)}
LEG = {"right": (0, 20, 4, 6), "front": (4, 20, 4, 6), "left": (8, 20, 4, 6), "back": (12, 20, 4, 6),
       "top": (4, 16, 4, 4), "bottom": (8, 16, 4, 4)}
SIDES = ("right", "front", "left", "back")

EYES = {(9, 10), (10, 10), (9, 11), (10, 11), (13, 10), (14, 10), (13, 11), (14, 11)}
MOUTH = {(11, 12), (12, 12), (10, 13), (11, 13), (12, 13), (13, 13), (10, 14), (11, 14), (12, 14), (13, 14),
         (10, 15), (13, 15)}

SKIN = ("#4AA43A", "#2E7A22", "#8AD86A", "#1E4A16")


def pixels(part, faces):
    """Return each pixel (x, y, face, col, row) of the faces of a part."""
    for face in faces:
        x0, y0, w, h = part[face]
        for row in range(h):
            for col in range(w):
                yield x0 + col, y0 + row, face, col, row


def big_bang():
    # The belly is the gene model big_bang_belly.
    return []


def big_bang_model():
    # A swollen belly with stretched skin: a round bulge on the front of the
    # body, paler than the skin, with pale stretch lines across it. On the
    # body bone, the front is at z -2, and the body is from y 0 to 12.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def belly(face, x, y):
        if face == "front":
            if (x, y) in ((0, 0), (5, 0), (0, 5), (5, 5)):
                return None
            return "#D0F4B8" if (x + y) % 4 == 0 else "#8AD86A" if (x * y) % 3 else "#6EC452"
        return "#6EC452"

    # The belly: 6x6x2 at uv (0, 0).
    paint_box(im, 0, 0, 6, 6, 2, belly)
    parts = [{"bone": "body", "uv": [0, 0], "from": [-3, 3, -3.5], "size": [6, 6, 2], "inflate": 0.25}]
    save_model("big_bang_belly", im, parts)


def shrapnel():
    # Shards of iron in the skin: short shards of gray iron, 2 pixels long at
    # a slant, each with a dark edge, on all the side faces of the body.
    iron = Mark("#B8B8B8")
    dark = Mark("#3A3A3A")
    for x, y, face, col, row in pixels(BODY, SIDES):
        if noise100(x // 2, y // 3, 341) < 18 and col % 3 == 1 and row % 4 == 1:
            iron.put((x, y)).put((x + 1, y + 1) if col + 1 < BODY[face][2] else (x, y + 1))
            dark.put((x, y + 1) if col + 1 < BODY[face][2] else (x - 1, y + 1))
    return [iron, dark]


def golem_breaker():
    # Dark iron-gray spots on the head, as if the head were made to break
    # iron: round spots of dark iron gray with a lighter gray rim, on all
    # the faces of the head, but not on the eyes and the mouth.
    spot = Mark("#4A4E52")
    rim = Mark("#7A7E82")
    for x, y, face, col, row in pixels(HEAD, HEAD):
        if (x, y) in EYES or (x, y) in MOUTH:
            continue
        n = noise100(x // 2, y // 2, 342)
        if n < 22:
            spot.put((x, y))
        elif n < 30:
            rim.put((x, y))
    return [spot, rim]


def chain_reaction():
    # Bright orange cracks on the body, as if the creeper were about to
    # burst: branching lines of orange with a yellow core, on the front and
    # the back of the body.
    crack = Mark("#FF8A20")
    core = Mark("#FFE070")
    for x0 in (20, 32):
        for y, x in ((21, 2), (22, 3), (23, 3), (24, 4), (25, 5), (26, 5), (27, 4), (28, 3), (29, 3),
                     (30, 2), (24, 2), (25, 1), (26, 0), (27, 6), (28, 7)):
            (core if y in (24, 27) else crack).put((x0 + x, y))
    return [crack, core]


def aftershock():
    # Cracked, ash-gray feet: the feet (leg rows 4 and 5, and the bottom of
    # the legs) are an ash gray, with dark cracks across them.
    ash = Mark("#8A8680")
    crack = Mark("#3A3634")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row >= 4:
            (crack if (col + row) % 3 == 0 else ash).put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        (crack if col == row else ash).put((x, y))
    return [ash, crack]


def concussion():
    # Wide eyes with rings, as after a blast: a ring of pale yellow round
    # each black eye, over it (y 9), beside it (the columns next to it), and
    # under it where the mouth leaves room (y 12).
    ring = Mark("#F0E8A0")
    for left, right in ((9, 10), (13, 14)):
        for x in (left, right):
            ring.put((x, 9))
        for y in (10, 11):
            ring.put((left - 1, y)).put((right + 1, y))
    ring.put((9, 12)).put((14, 12))
    return [ring]


def sprinter():
    # Pale stripes down the legs, as the marks of a fast runner: one pale
    # column on each side face of the legs, rows 0 to 5.
    stripe = Mark("#D8F0C8")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if col == 1:
            stripe.put((x, y))
    return [stripe]


def short_fuse():
    # A smoking spot on the top of the head, where the fuse comes out. The
    # fuse is the gene model short_fuse_fuse.
    burn = Mark("#2A2A2A")
    burn.put((11, 3)).put((12, 3)).put((11, 4)).put((12, 4))
    return [burn]


def short_fuse_model():
    # A fuse that smokes on the top of the head: a short, dark cord that
    # sticks up from the middle of the top of the head and bends, a bright
    # spark at its tip, and a puff of gray smoke that sits on the spark. On
    # the head bone, the top of the head is at y -8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def cord(face, x, y):
        return "#3A3A3A" if (y + x) % 2 else "#5A4A3A"

    def puff(face, x, y):
        return "#C8C8C8" if (x + y) % 2 else "#A0A0A0"

    # The cord: 1x3x1 at uv (0, 0). The spark: 1x1x1 at uv (4, 0). The
    # puff: 2x2x2 at uv (8, 0).
    paint_box(im, 0, 0, 1, 3, 1, cord)
    paint_box(im, 4, 0, 1, 1, 1, lambda face, x, y: "#FFB030")
    paint_box(im, 8, 0, 2, 2, 2, puff)
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-0.5, -11, -0.5], "size": [1, 3, 1],
         "pivot": [0, -8, 0], "rotation": [0, 0, 15]},
        {"bone": "head", "uv": [4, 0], "from": [0.2, -12, -0.5], "size": [1, 1, 1]},
        {"bone": "head", "uv": [8, 0], "from": [0, -14, -1], "size": [2, 2, 2]},
    ]
    save_model("short_fuse_fuse", im, parts)


def leaper():
    # The haunches are the gene model leaper_haunches.
    return []


def leaper_model():
    # Bent, springy back legs: on each hind leg, a thick haunch that bends
    # back from the top of the leg, as the leg of a frog, in the greens of
    # the creeper, with a pale line where it bends. On a leg bone, the leg
    # is from x -2 to 2, from y 0 to 6, and from z -2 to 2.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def haunch(face, x, y):
        if face != "top" and y == 2:
            return "#B8E8A0"
        n = (x * 7 + y * 3 + len(face)) % 5
        return SKIN[1] if n == 0 else SKIN[2] if n == 1 else SKIN[0]

    # The haunch: 4x3x2 at uv (0, 0).
    paint_box(im, 0, 0, 4, 3, 2, haunch)
    parts = [{"bone": bone, "uv": [0, 0], "from": [-2, 0, 1], "size": [4, 3, 2],
              "pivot": [0, 0, 1.5], "rotation": [-15, 0, 0]}
             for bone in ("right_hind_leg", "left_hind_leg")]
    save_model("leaper_haunches", im, parts)


def dodger():
    # Wide, flat feet: a dark sole on the lowest row of the legs (row 5) and
    # on the bottom of the legs, and pale toe lines on row 4 that spread the
    # foot.
    sole = Mark(SKIN[3])
    toe = Mark("#C8F0B0")
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row == 5:
            sole.put((x, y))
        elif row == 4 and col in (0, 3):
            toe.put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        sole.put((x, y))
    return [sole, toe]


def tracker():
    # A wide, open mouth, as if the creeper sniffs for its prey: the black
    # mouth is one pixel wider on each side (x 9 and 14, y 13 and 14).
    black = Mark("#0A0A0A")
    for p in ((9, 13), (14, 13), (9, 14), (14, 14)):
        black.put(p)
    return [black]


def wallcrawler():
    # The claws are the gene model wallcrawler_claws.
    return []


def wallcrawler_model():
    # Hooked claws on the feet: on each leg, two dark claws that stick out
    # of the front of the foot and hook down, with pale tips. On a leg bone,
    # the front of the foot is at z -2, and the ground is at y 6.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def claw(face, x, y):
        return "#D8D8C8" if face == "front" or y == 1 else "#2A3A2A"

    # A claw: 1x1x2 at uv (0, 0).
    paint_box(im, 0, 0, 1, 1, 2, claw)
    parts = []
    for bone in ("right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"):
        for x in (-1.5, 0.5):
            parts.append({"bone": bone, "uv": [0, 0], "from": [x, 5, -3.5], "size": [1, 1, 2],
                          "pivot": [x + 0.5, 5.5, -2], "rotation": [-30, 0, 0]})
    save_model("wallcrawler_claws", im, parts)



def silent_fuse():
    # A mouth that is sewn shut: the black mouth is skin, a dark seam runs
    # across it (y 13), and pale stitches cross the seam (x 10, 11, 12, and
    # 13, y 12 and 14).
    skin = Mark(SKIN[0])
    seam = Mark(SKIN[3])
    stitch = Mark("#E0D8C0")
    for p in MOUTH:
        skin.put(p)
    for x in range(10, 14):
        seam.put((x, 13))
        stitch.put((x, 12 if x % 2 else 14))
    return [skin, seam, stitch]


def leafskin():
    # A pattern of leaves on the skin of the body: dark green leaf shapes,
    # 3 pixels long at a slant, each with a pale vein along it, on all the
    # side faces of the body.
    leaf = Mark("#1E4A12")
    vein = Mark("#D8F8C0")
    for x, y, face, col, row in pixels(BODY, SIDES):
        if (col + 2 * row) % 6 == 0 and row % 3 == 0:
            leaf.put((x, y))
            if col + 1 < BODY[face][2]:
                vein.put((x + 1, y))
            if row + 1 < BODY[face][3]:
                leaf.put((x, y + 1))
                if col + 1 < BODY[face][2]:
                    leaf.put((x + 1, y + 1))
    return [leaf, vein]


def gloom():
    # Dark gray patches on the body, as a creeper that hides in the dark:
    # wide patches of dark gray with a softer gray edge, on all the faces of
    # the body.
    patch = Mark("#3A3E3A")
    edge = Mark("#5E625E")
    for x, y, face, col, row in pixels(BODY, BODY):
        n = noise100(x // 2, y // 2, 351)
        if n < 30:
            patch.put((x, y))
        elif n < 40:
            edge.put((x, y))
    return [patch, edge]


def veiled():
    # A faded, pale face: a pale wash over the skin of the face, and dark
    # gray eyes and mouth in place of black, as if the face fades away.
    wash = Mark("#C8D8C0", alpha=190)
    gray = Mark("#5A5E5A")
    for x in range(8, 16):
        for y in range(8, 16):
            if (x, y) in EYES or (x, y) in MOUTH:
                gray.put((x, y))
            else:
                wash.put((x, y))
    return [wash, gray]


ROOT = ("#6A4A2E", "#4A3220", "#A08060")


def patient():
    # The roots are the gene model patient_roots.
    return []


def patient_model():
    # Roots that grow round the feet, as if the creeper waited so long
    # that it took root: on each leg, a band of root round the foot, two
    # roots that climb up the outer side and the back of the leg from it,
    # and short roots that go down from it into the ground. Every root
    # touches the leg. On a leg bone, the leg is from x -2 to 2 and from z
    # -2 to 2, and the ground is at y 6.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def band(face, x, y):
        if face in ("top", "bottom"):
            return None
        return ROOT[0] if (x + y) % 3 else ROOT[1]

    def climb(face, x, y):
        return ROOT[2] if y == 0 else ROOT[0] if y % 2 else ROOT[1]

    # The band: 4x1x4 at uv (0, 0). A climbing root: 1x3x1 at uv (16, 0). A
    # root into the ground: 1x2x1 at uv (20, 0).
    paint_box(im, 0, 0, 4, 1, 4, band)
    paint_box(im, 16, 0, 1, 3, 1, climb)
    paint_box(im, 20, 0, 1, 2, 1, lambda face, x, y: ROOT[1])
    parts = []
    for bone, side in (("right_hind_leg", -1), ("left_hind_leg", 1), ("right_front_leg", -1),
                       ("left_front_leg", 1)):
        outer = -2.6 if side < 0 else 1.6
        parts.append({"bone": bone, "uv": [0, 0], "from": [-2, 4, -2], "size": [4, 1, 4], "inflate": 0.3})
        parts.append({"bone": bone, "uv": [16, 0], "from": [outer, 1.5, -0.5], "size": [1, 3, 1]})
        parts.append({"bone": bone, "uv": [16, 0], "from": [0.5 * side - 0.5, 2.5, 1.6], "size": [1, 3, 1]})
        parts.append({"bone": bone, "uv": [20, 0], "from": [outer, 5, -1.5], "size": [1, 2, 1],
                      "pivot": [outer + 0.5, 5, -1], "rotation": [0, 0, 20 * side]})
        parts.append({"bone": bone, "uv": [20, 0], "from": [-0.5, 5, -2.6], "size": [1, 2, 1],
                      "pivot": [0, 5, -2.1], "rotation": [-20, 0, 0]})
    save_model("patient_roots", im, parts)


def surprise():
    # A wider grin: the corners of the mouth go up and out, so the creeper
    # grins (x 9 and 14 of y 13, and x 9 and 14 of y 12).
    black = Mark("#0A0A0A")
    for p in ((9, 13), (14, 13), (9, 12), (14, 12)):
        black.put(p)
    return [black]


def toxic_blast():
    # Green-yellow blisters on the body: round blisters of 2 by 2 pixels,
    # each with a pale top, a green-yellow body, and a dark rim under it, on
    # all the side faces of the body.
    top = Mark("#F0F8A0")
    blister = Mark("#C8E040")
    rim = Mark("#4A5A10")
    for x, y, face, col, row in pixels(BODY, SIDES):
        if (col % 3 == 0 and row % 4 == 1 and noise100(x, y, 361) < 55
                and col + 1 < BODY[face][2] and row + 2 < BODY[face][3]):
            top.put((x, y))
            blister.put((x + 1, y)).put((x, y + 1)).put((x + 1, y + 1))
            rim.put((x, y + 2)).put((x + 1, y + 2))
    return [top, blister, rim]


def smoke_bomb():
    # Short puffs of gray smoke over the eyes: a puff over each eye (y 8
    # and 9), and smoke on the front edge of the top of the head, as if the
    # smoke rises from the eyes. The puffs are short, so they do not make
    # streaks down the face.
    smoke = Mark("#B0B0B0")
    light = Mark("#D8D8D8")
    for left, right in ((9, 10), (13, 14)):
        smoke.put((left, 9)).put((right, 9))
        light.put((left, 8))
        smoke.put((right, 8))
    for x in range(9, 15):
        (light if x % 2 else smoke).put((x, 7))
    return [smoke, light]


def stink():
    # Dark green stains on the body: blotches of dark, dull green, each
    # with a short drip under it, on all the side faces of the body.
    stain = Mark("#2A4A1A")
    drip = Mark("#3A5A22")
    for x, y, face, col, row in pixels(BODY, SIDES):
        n = noise100(x // 2, y // 3, 362)
        if n < 22 and row < 10:
            stain.put((x, y))
            if noise100(x, y, 363) < 30:
                drip.put((x, y + 1)).put((x, y + 2))
    return [stain, drip]


def wither_gas():
    # Black drool from the mouth: short black drips from the corners of the
    # mouth, on the front edge of the bottom of the head, and drops of it on
    # the top of the chest (y 20 to 22), with a dark gray edge.
    drool = Mark("#0A0A0A")
    edge = Mark("#3A3436")
    for x in (18, 21):
        drool.put((x, 7))
        edge.put((x, 6))
    for x, y in ((22, 20), (22, 21), (25, 20), (25, 21), (25, 22)):
        drool.put((x, y))
    edge.put((23, 20)).put((24, 20))
    return [drool, edge]


def gas_leak():
    # Holes that leak gas on the back of the body: dark holes with puffs of
    # pale green gas round them, on the back face (x 32 to 39).
    hole = Mark("#0E1A0A")
    gas = Mark("#C8F0A8", alpha=210)
    for x, y in ((34, 22), (37, 24), (33, 27), (36, 29)):
        hole.put((x, y))
        for n in ((x, y - 1), (x + 1, y - 1), (x - 1, y), (x + 1, y)):
            gas.put(n)
    return [hole, gas]


def spore_blast():
    # The mushrooms are the gene model spore_blast_mushrooms.
    return []


def spore_blast_model():
    # Small brown mushrooms on the head: three small mushrooms that grow on
    # the top of the head and on its side, each a pale stem and a brown cap
    # with pale spots. On the head bone, the top of the head is at y -8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def cap(face, x, y):
        if face == "top" and (x + y) % 2 == 0:
            return "#D8C8A8"
        return "#8A6040" if face != "bottom" else "#6A4A2E"

    # A cap: 2x1x2 at uv (0, 0). A stem: 1x1x1 at uv (8, 0).
    paint_box(im, 0, 0, 2, 1, 2, cap)
    paint_box(im, 8, 0, 1, 1, 1, lambda face, x, y: "#E8E0C8")
    parts = []
    for x, z in ((-3, -2), (1, 1), (-1, 2.5)):
        parts.append({"bone": "head", "uv": [8, 0], "from": [x + 0.5, -9, z + 0.5], "size": [1, 1, 1]})
        parts.append({"bone": "head", "uv": [0, 0], "from": [x, -10, z], "size": [2, 1, 2]})
    parts.append({"bone": "head", "uv": [8, 0], "from": [4, -5.5, 0.5], "size": [1, 1, 1]})
    parts.append({"bone": "head", "uv": [0, 0], "from": [4.5, -6, 0], "size": [2, 1, 2]})
    save_model("spore_blast_mushrooms", im, parts)

COPPER = ("#C86A3A", "#E89060", "#8A4422", "#5AA08A")
BLUE = ("#40A0FF", "#A8E0FF", "#FFFFFF", "#2A60C0")


def lightning_rod():
    # A ring of copper where the spike goes into the head (x 11 and 12, y 3
    # and 4 of the top of the head). The spike is the gene model
    # lightning_rod_spike.
    ring = Mark(COPPER[2])
    for p in ((11, 3), (12, 3), (11, 4), (12, 4)):
        ring.put(p)
    return [ring]


def lightning_rod_model():
    # A copper spike on the top of the head, as a lightning rod: a wide base
    # on the head, a thin rod that rises from it, and a point, in copper
    # with a bright edge and a few green spots of old copper. On the head
    # bone, the top of the head is at y -8.
    im = Image.new("RGBA", (32, 16), (0, 0, 0, 0))

    def copper(face, x, y):
        if (x + y + len(face)) % 5 == 0:
            return COPPER[3]
        return COPPER[1] if face in ("front", "right") else COPPER[0]

    # The base: 2x1x2 at uv (0, 0). The rod: 1x4x1 at uv (8, 0). The point:
    # 1x1x1 at uv (12, 0).
    paint_box(im, 0, 0, 2, 1, 2, copper)
    paint_box(im, 8, 0, 1, 4, 1, copper)
    paint_box(im, 12, 0, 1, 1, 1, lambda face, x, y: COPPER[1])
    parts = [
        {"bone": "head", "uv": [0, 0], "from": [-1, -9, -1], "size": [2, 1, 2]},
        {"bone": "head", "uv": [8, 0], "from": [-0.5, -13, -0.5], "size": [1, 4, 1]},
        {"bone": "head", "uv": [12, 0], "from": [-0.5, -14, -0.5], "size": [1, 1, 1], "inflate": -0.2},
    ]
    save_model("lightning_rod_spike", im, parts)


def static():
    # Small blue sparks on the skin: crosses of bright blue, 3 pixels wide,
    # with a white middle, on all the side faces of the body.
    spark = Mark(BLUE[1])
    core = Mark(BLUE[2])
    for x, y, face, col, row in pixels(BODY, SIDES):
        w, h = BODY[face][2], BODY[face][3]
        if 0 < col < w - 1 and 0 < row < h - 1 and noise100(x // 3, y // 3, 371) < 30 and (col + row) % 3 == 0:
            core.put((x, y))
            spark.put((x - 1, y)).put((x + 1, y)).put((x, y - 1)).put((x, y + 1))
    return [spark, core]


def overcharge():
    # Bright blue veins over the body, as if the power runs under the skin:
    # lines of bright blue that branch, on the front and the back of the
    # body, with paler blue where they branch.
    vein = Mark(BLUE[0])
    pale = Mark(BLUE[1])
    path = ((3, 0), (3, 1), (4, 2), (4, 3), (3, 4), (2, 5), (2, 6), (5, 4), (6, 5), (6, 6), (5, 7),
            (2, 7), (1, 8), (1, 9), (5, 8), (4, 9), (4, 10), (4, 11))
    for x0 in (20, 32):
        for x, y in path:
            (pale if (x, y) in ((4, 3), (2, 7), (5, 8)) else vein).put((x0 + x, 20 + y))
    return [vein, pale]


def conductor():
    # Forked blue marks on the head, as the marks of lightning: short forks
    # of bright blue, each a stem that splits in two, on the sides, the
    # back, and the top of the head. They keep off the face.
    fork = Mark(BLUE[0])
    tip = Mark(BLUE[1])
    for x0, y0 in ((2, 9), (18, 9), (26, 10), (9, 1)):
        for dx, dy in ((2, 0), (2, 1), (1, 2), (3, 2), (0, 3), (4, 3)):
            (tip if dy == 3 else fork).put((x0 + dx, y0 + dy))
    return [fork, tip]


def grounded():
    # Copper bands round the legs, to carry the power to the ground: a band
    # of copper on leg rows 2 and 3 of all the faces, with a bright upper
    # edge and a few green spots of old copper.
    band = Mark(COPPER[0])
    bright = Mark(COPPER[1])
    old = Mark(COPPER[3])
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row == 2:
            bright.put((x, y))
        elif row == 3:
            (old if (col + len(face)) % 4 == 0 else band).put((x, y))
    return [band, bright, old]


def thunderstruck():
    # A blue glow on the feet, as if the creeper stands in a strike: the
    # feet (leg rows 4 and 5) and the bottom of the legs are blue, pale blue
    # at the bottom, and deep blue specks on row 3 where the glow fades.
    glow = Mark(BLUE[0])
    pale = Mark(BLUE[1])
    fade = Mark(BLUE[3])
    for x, y, face, col, row in pixels(LEG, SIDES):
        if row == 5:
            pale.put((x, y))
        elif row == 4:
            glow.put((x, y))
        elif row == 3 and (col + len(face)) % 2:
            fade.put((x, y))
    for x, y, face, col, row in pixels(LEG, ("bottom",)):
        pale.put((x, y))
    return [glow, pale, fade]



GENES = {
    "big_bang": big_bang,
    "shrapnel": shrapnel,
    "golem_breaker": golem_breaker,
    "chain_reaction": chain_reaction,
    "aftershock": aftershock,
    "concussion": concussion,
    "sprinter": sprinter,
    "short_fuse": short_fuse,
    "leaper": leaper,
    "dodger": dodger,
    "tracker": tracker,
    "wallcrawler": wallcrawler,
    "silent_fuse": silent_fuse,
    "leafskin": leafskin,
    "gloom": gloom,
    "veiled": veiled,
    "patient": patient,
    "surprise": surprise,
    "toxic_blast": toxic_blast,
    "smoke_bomb": smoke_bomb,
    "stink": stink,
    "wither_gas": wither_gas,
    "gas_leak": gas_leak,
    "spore_blast": spore_blast,
    "lightning_rod": lightning_rod,
    "static": static,
    "overcharge": overcharge,
    "conductor": conductor,
    "grounded": grounded,
    "thunderstruck": thunderstruck,
}

MODELS = {
    "big_bang": "originofspawns:big_bang_belly",
    "short_fuse": "originofspawns:short_fuse_fuse",
    "leaper": "originofspawns:leaper_haunches",
    "wallcrawler": "originofspawns:wallcrawler_claws",
    "patient": "originofspawns:patient_roots",
    "spore_blast": "originofspawns:spore_blast_mushrooms",
    "lightning_rod": "originofspawns:lightning_rod_spike",
}

MODEL_MAKERS = [big_bang_model, short_fuse_model, leaper_model, wallcrawler_model, patient_model,
                spore_blast_model, lightning_rod_model]


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
    print("pixels that two or more creeper genes share:", sum(shared.values()))
    for key, count in sorted(shared.items()):
        print(" ", key + ":", count)


if __name__ == "__main__":
    main()
